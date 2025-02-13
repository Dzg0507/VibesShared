package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.Post
import com.example.vibesshared.ui.ui.data.PostWithUser
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel.PostCreationStatus.*
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    @ApplicationContext private val context: Context, // Inject Context
    private val dispatchers: DispatcherProvider // Inject dispatcher
) : ViewModel() {

    private val _postsFlow = MutableStateFlow<List<PostWithUser>>(emptyList())
    val postsFlow: StateFlow<List<PostWithUser>> = _postsFlow.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _postCreationStatus = MutableStateFlow<PostCreationStatus>(PostCreationStatus.Idle)
    val postCreationStatus: StateFlow<PostCreationStatus> = _postCreationStatus.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        fetchPosts() // Call fetchPosts to load the data
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        viewModelScope.launch(dispatchers.io) { // Use injected dispatcher
            val userId = repository.getCurrentUser()?.uid
            if (userId != null) {
                repository.getUserFlow(userId).collect { userProfile ->
                    _currentUserProfile.value = userProfile
                }
            }
        }
    }

    fun fetchPosts() {
        viewModelScope.launch(dispatchers.io) {
            _isLoading.value = true // Start loading
            _error.value = null // Reset error state
            try {
                repository.getPostsWithUsersFlow()
                    .collect { postsWithUsers ->
                        // Fetch comment counts for each post
                        val postsWithCommentCounts = postsWithUsers.map { postWithUser ->
                            val commentCountResult = repository.getCommentCount(postWithUser.post.postId)
                            val commentCount = when (commentCountResult) {
                                is Result.Success<*> -> commentCountResult.data
                                is Result.Failure -> 0 // Default to 0 if there's an error
                                else -> {
                                    Log.e("PostViewModel", "Error fetching comment count for post: ${postWithUser.post.postId}")
                                }
                            }
                            postWithUser.copy(post = postWithUser.post.copy(commentCount = commentCount as Int))
                        }
                        _postsFlow.value = postsWithCommentCounts // Update the _postsFlow
                        _isLoading.value = false // Set loading to false after fetching
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("PostViewModel", "fetchPosts: Exception: ${e.message}", e)
                _isLoading.value = false // Ensure loading is stopped on error
            }
        }
    }

    fun addPost(postText: String, images: List<Uri>, context: Context) {
        viewModelScope.launch(dispatchers.io) {
            _postCreationStatus.value = PostCreationStatus.Creating
            _isLoading.value = true
            _uploadProgress.value = 0f

            val currentUserId = repository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _postCreationStatus.value = PostCreationStatus.Error("User not logged in")
                _isLoading.value = false
                return@launch
            }

            try {
                val imageUrls = images.map { image ->
                    async { uploadImageToFirebase(image) }
                }.awaitAll()

                val newPost = Post(
                    userId = currentUserId,
                    postText = postText,
                    postImage = imageUrls.firstOrNull() ?: "", // Handle empty list
                    timestamp = Timestamp.now(),
                    commentCount = 0 // Initialize comment count
                )

                when (val result = repository.createPost(newPost)) {
                    is Result.Success -> {
                        _postCreationStatus.value = PostCreationStatus.Success
                    }
                    is Result.Failure -> {
                        _postCreationStatus.value = Error(result.exception.message ?: "Failed to create post")
                    }

                    is Result.Loading -> TODO()
                }

            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post: ${e.message}", e)
                _postCreationStatus.value = PostCreationStatus.Error("Failed to create post")
            } finally {
                _isLoading.value = false
                clearImages()
            }
        }
    }

    private suspend fun uploadImageToFirebase(uri: Uri): String {
        val imageFileName = "postImages/${UUID.randomUUID()}"
        val imageRef = repository.getStorageReference().child(imageFileName)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open input stream for $uri")

        return inputStream.use { stream ->
            try {
                Log.d("PostViewModel", "Uploading image: $imageFileName")
                val uploadTask = imageRef.putStream(stream)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val bytesTransferred: Long = taskSnapshot.bytesTransferred
                    val totalByteCount: Long = taskSnapshot.totalByteCount

                    if (totalByteCount > 0) {
                        val progress = (100.0 * bytesTransferred / totalByteCount).toFloat()
                        Log.d("PostViewModel", "Upload progress: $progress%")
                        _uploadProgress.value = progress
                    } else {
                        Log.w("PostViewModel", "Total byte count is zero or negative, cannot calculate progress.")
                    }
                }.await()

                val downloadUrl = imageRef.downloadUrl.await().toString()
                Log.d("PostViewModel", "Image uploaded successfully: $downloadUrl")
                downloadUrl
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error uploading image: ${e.message}", e)
                throw e // Re-throw the exception to be caught in the outer try-catch
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch(dispatchers.io) {
            val userId = repository.getCurrentUser()?.uid
            if (userId == null) {
                Log.e("PostViewModel", "User ID is null, cannot like post")
                return@launch
            }

            when (val result = repository.toggleLike(postId, userId)) {
                is Result.Success -> {
                    // No need to reload everything, addSnapshotListener will be triggered
                }
                is Result.Failure -> {
                    Log.e("PostViewModel", "Error liking/unliking post: ${result.exception.message}")
                }

                is Result.Loading -> TODO()
            }
        }
    }

    fun addImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    sealed class PostCreationStatus {
        object Idle : PostCreationStatus()
        object Creating : PostCreationStatus()
        object Success : PostCreationStatus()
        data class Error(val message: String) : PostCreationStatus()
    }
}