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
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _postsFlow = MutableStateFlow<List<PostWithUser>>(emptyList())
    val postsFlow: StateFlow<List<PostWithUser>> = _postsFlow.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _postCreationStatus = MutableStateFlow<PostCreationStatus>(Idle)
    val postCreationStatus: StateFlow<PostCreationStatus> = _postCreationStatus.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        fetchPosts()
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        viewModelScope.launch(dispatchers.io) {
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
            _isLoading.value = true
            _error.value = null
            try {
                repository.getPostsWithUsersFlow()
                    .collect { postsWithUsers ->
                        val postsWithCommentCounts = postsWithUsers.map { postWithUser ->
                            val commentCountResult = repository.getCommentCount(postWithUser.post.postId)
                            val commentCount = when (commentCountResult) {
                                is Result.Success -> commentCountResult.data as Int
                                is Result.Failure -> {
                                    Log.e("PostViewModel", "Failed to fetch comment count: ${commentCountResult.exception.message}")
                                    0
                                }
                                is Result.Loading -> {
                                    Log.d("PostViewModel", "Comment count still loading for post: ${postWithUser.post.postId}")
                                    0 // Default to 0 while loading
                                }
                            }
                            postWithUser.copy(post = postWithUser.post.copy(commentCount = commentCount))
                        }
                        _postsFlow.value = postsWithCommentCounts
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("PostViewModel", "fetchPosts: Exception: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }

    fun addPost(postText: String, images: List<Uri>, context: Context, videoUri: Uri? = null) {
        viewModelScope.launch(dispatchers.io) {
            _postCreationStatus.value = Creating
            _isLoading.value = true
            _uploadProgress.value = 0f

            val currentUserId = repository.getCurrentUser()?.uid ?: run {
                _postCreationStatus.value = Error("User not logged in")
                _isLoading.value = false
                return@launch
            }

            try {
                val imageUrls = if (images.isNotEmpty()) {
                    images.map { uploadImageToFirebase(it) }
                } else emptyList()

                val videoUrl = videoUri?.let { uploadVideoToFirebase(it) }

                val userProfile = _currentUserProfile.value
                val userName = userProfile?.userName ?: ""
                val profilePictureUrl = userProfile?.profilePictureUrl

                val newPost = Post(
                    userId = currentUserId,
                    postText = postText,
                    internalPostImages = imageUrls, // Use internalPostImages instead of postImages
                    postVideo = videoUrl,
                    profilePictureUrl = profilePictureUrl,
                    userName = userName,
                    timestamp = Timestamp.now(),
                    commentCount = 0
                )

                when (val result = repository.createPost(currentUserId, postText, imageUrls, videoUrl)) {
                    is Result.Success -> {
                        _postCreationStatus.value = Success
                        repository.checkAndAwardPostBadges(currentUserId)
                    }
                    is Result.Failure -> {
                        _postCreationStatus.value = Error(result.exception.message ?: "Failed to create post")
                        Log.e("PostViewModel", "Post creation failed: ${result.exception.message}")
                    }
                    is Result.Loading -> {
                        Log.d("PostViewModel", "Post creation in progress")
                        // No action needed, already set to Creating
                    }
                }
            } catch (e: Exception) {
                _postCreationStatus.value = Error("Failed to create post: ${e.message}")
            } finally {
                _isLoading.value = false
                clearImages()
            }
        }
    }

    fun addPollPost(pollDescription: String, pollOptions: List<String>) {
        viewModelScope.launch(dispatchers.io) {
            _postCreationStatus.value = Creating
            _isLoading.value = true

            val currentUserId = repository.getCurrentUser()?.uid ?: run {
                _postCreationStatus.value = Error("User not logged in")
                _isLoading.value = false
                return@launch
            }

            try {
                val userProfile = _currentUserProfile.value
                val pollData = mapOf(
                    "description" to pollDescription,
                    "options" to pollOptions,
                    "votes" to pollOptions.associateWith { 0 }
                )

                when (val result = repository.createPost(
                    userId = currentUserId,
                    postText = pollDescription,
                    postImages = emptyList(), // Correctly uses postImages
                    postVideo = null,
                    pollData = pollData
                )) {
                    is Result.Success -> {
                        _postCreationStatus.value = Success
                        repository.checkAndAwardPostBadges(currentUserId)
                    }
                    is Result.Failure -> {
                        _postCreationStatus.value = Error(result.exception.message ?: "Failed to create poll")
                        Log.e("PostViewModel", "Poll creation failed: ${result.exception.message}")
                    }
                    is Result.Loading -> {
                        Log.d("PostViewModel", "Poll creation in progress")
                        // No action needed, already set to Creating
                    }
                }
            } catch (e: Exception) {
                _postCreationStatus.value = Error("Failed to create poll: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun voteOnPoll(postId: String, option: String) {
        viewModelScope.launch(dispatchers.io) {
            val userId = repository.getCurrentUser()?.uid ?: run {
                Log.e("PostViewModel", "User not logged in")
                return@launch
            }

            try {
                when (val result = repository.voteOnPoll(postId, userId, option)) {
                    is Result.Success -> {
                        val updatedPosts = _postsFlow.value.map { postWithUser ->
                            if (postWithUser.post.postId == postId) {
                                postWithUser.copy(
                                    post = postWithUser.post.copy(hasUserVoted = true)
                                )
                            } else postWithUser
                        }
                        _postsFlow.value = updatedPosts
                    }
                    is Result.Failure -> Log.e("PostViewModel", "Error voting: ${result.exception.message}")
                    is Result.Loading -> Log.d("PostViewModel", "Poll voting in progress")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception voting: ${e.message}")
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch(dispatchers.io) {
            val userId = repository.getCurrentUser()?.uid ?: run {
                Log.e("PostViewModel", "User ID is null, cannot like post")
                return@launch
            }

            when (val result = repository.toggleLike(postId, userId)) {
                is Result.Success -> {
                    // Successfully toggled like, no additional action needed here
                }
                is Result.Failure -> Log.e("PostViewModel", "Error liking/unliking post: ${result.exception.message}")
                is Result.Loading -> Log.d("PostViewModel", "Like toggle in progress")
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

    private suspend fun uploadImageToFirebase(uri: Uri): String {
        val imageFileName = "postImages/${UUID.randomUUID()}"
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open input stream for $uri")

        return inputStream.use { stream ->
            try {
                Log.d("PostViewModel", "Uploading image: $imageFileName")
                val currentUserId = repository.getCurrentUser()?.uid ?: throw IOException("User not authenticated")
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setCustomMetadata("userId", currentUserId)
                    .build()

                val uploadTask = repository.getStorageReference().child(imageFileName).putStream(stream, metadata)

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

                val downloadUrl = repository.getStorageReference().child(imageFileName).downloadUrl.await().toString()
                Log.d("PostViewModel", "Image uploaded successfully: $downloadUrl")
                downloadUrl
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error uploading image: ${e.message}", e)
                throw e
            }
        }
    }

    private suspend fun uploadVideoToFirebase(uri: Uri): String {
        val videoFileName = "postVideos/${UUID.randomUUID()}"
        val videoRef = repository.getStorageReference().child(videoFileName)
        val uploadTask = videoRef.putFile(uri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
            _uploadProgress.value = progress
        }.await()
        return videoRef.downloadUrl.await().toString()
    }

    sealed class PostCreationStatus {
        object Idle : PostCreationStatus()
        object Creating : PostCreationStatus()
        object Success : PostCreationStatus()
        data class Error(val message: String) : PostCreationStatus()
    }
}