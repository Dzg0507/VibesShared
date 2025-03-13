package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.vibesshared.ui.ui.data.Post
import com.example.vibesshared.ui.ui.data.PostWithUser
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel.PostCreationStatus.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

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

    // Cache for vote states (postId to hasVoted), persisted using SharedPreferences
    private val voteCache = mutableMapOf<String, Boolean>()

    // Real-time states for likes and comment counts
    private val _likeState = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val likeState: StateFlow<Map<String, List<String>>> = _likeState.asStateFlow()

    private val _commentCountState = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCountState: StateFlow<Map<String, Int>> = _commentCountState.asStateFlow()

    // Store listeners to clean up
    private var likesListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
    private var commentsListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()

    init {
        viewModelScope.launch(dispatchers.io) {
            fetchPosts()
            fetchCurrentUserProfile()
            loadVoteCache() // Load cached votes on initialization
            listenForLikesAndComments() // Start real-time listeners
        }
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
        viewModelScope.launch(dispatchers.io + CoroutineExceptionHandler { _, throwable ->
            Log.e("PostViewModel", "fetchPosts: Coroutine failed: ${throwable.message}", throwable)
            if (throwable is CancellationException) {
                Log.w("PostViewModel", "Job cancelled, retrying...")
                viewModelScope.launch(dispatchers.io) { // Launch a new coroutine for retry
                    delay(500) // Delay before retrying
                    fetchPosts() // Retry fetchPosts
                }
            } else {
                _error.value = throwable.message ?: "Unknown error"
            }
            _isLoading.value = false
            _postsFlow.value = emptyList() // Fallback to empty list
        }) {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUserId = Firebase.auth.currentUser?.uid
                if (currentUserId == null) {
                    _postsFlow.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                repository.getPostsWithUsersFlow()
                    .collect { postsWithUsers ->
                        val postsWithCommentCounts = postsWithUsers.map { postWithUser ->
                            val commentCountResult = withContext(dispatchers.io) { // Ensure comment count is fetched in IO context
                                repository.getCommentCount(postWithUser.post.postId)
                            }
                            val commentCount = when (commentCountResult) {
                                is Result.Success -> commentCountResult.data
                                is Result.Failure -> {
                                    Log.e("PostViewModel", "Failed to fetch comment count: ${commentCountResult.exception.message}")
                                    0
                                }
                                is Result.Loading -> {
                                    Log.d("PostViewModel", "Comment count still loading for post: ${postWithUser.post.postId}")
                                    0 // Default to 0 while loading
                                }
                            }
                            // Use cached vote state or query Firestore if not cached
                            val hasVoted = voteCache[postWithUser.post.postId] ?: checkUserVotedFromFirestore(postWithUser.post.postId, currentUserId)
                            postWithUser.copy(
                                post = postWithUser.post.copy(
                                    commentCount = commentCount,
                                    hasUserVoted = hasVoted
                                )
                            )
                        }
                        _postsFlow.value = postsWithCommentCounts
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("PostViewModel", "fetchPosts: Exception: ${e.message}", e)
                _isLoading.value = false
                _postsFlow.value = emptyList() // Fallback to empty list
            }
        }
    }

    private suspend fun checkUserVotedFromFirestore(postId: String, userId: String): Boolean = withContext(dispatchers.io) {
        val firestore = Firebase.firestore
        try {
            val voteDoc = firestore.collection("posts").document(postId)
                .collection("votes").document(userId).get().await()
            val hasVoted = voteDoc.exists()
            if (hasVoted) {
                voteCache[postId] = true // Update cache
                saveVoteCache() // Persist cache
            }
            hasVoted
        } catch (e: Exception) {
            Log.w("PostViewModel", "Failed to check vote for post $postId: ${e.message}")
            false // Default to false if query fails
        }
    }

    // Persist vote cache using SharedPreferences
    private fun saveVoteCache() {
        viewModelScope.launch(dispatchers.io) {
            val prefs = context.getSharedPreferences("vote_cache", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            voteCache.forEach { (postId, hasVoted) ->
                editor.putBoolean("vote_$postId", hasVoted)
            }
            editor.apply()
        }
    }

    private fun loadVoteCache() {
        viewModelScope.launch(dispatchers.io) {
            val prefs = context.getSharedPreferences("vote_cache", Context.MODE_PRIVATE)
            val allEntries = prefs.all
            allEntries.forEach { (key, value) ->
                if (key.startsWith("vote_") && value is Boolean) {
                    val postId = key.removePrefix("vote_")
                    voteCache[postId] = value
                }
            }
        }
    }

    fun addPost(
        postText: String,
        images: List<Uri>,
        context: Context,
        videoUri: Uri? = null,
        imageDescription: String = "",
        videoDescription: String = ""
    ) {
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

                var videoUrl: String? = null
                var thumbnailUrl: String? = null
                if (videoUri != null) {
                    val thumbnailBitmap = generateThumbnailBitmap(videoUri)
                    val thumbnailUri = saveBitmapToTempFile(thumbnailBitmap)
                    val thumbnailDeferred = async { uploadImageToFirebase(thumbnailUri) }
                    val videoUrlDeferred = async { uploadVideoToFirebase(videoUri) }
                    thumbnailUrl = thumbnailDeferred.await()
                    videoUrl = videoUrlDeferred.await()
                }

                val userProfile = _currentUserProfile.value
                val userName = userProfile?.userName ?: ""
                val profilePictureUrl = userProfile?.profilePictureUrl

                val newPost = Post(
                    userId = currentUserId,
                    postText = postText,
                    internalPostImages = imageUrls,
                    postVideo = videoUrl,
                    profilePictureUrl = profilePictureUrl,
                    userName = userName,
                    timestamp = Timestamp.now(),
                    commentCount = 0,
                    imageDescription = imageDescription,
                    videoDescription = videoDescription,
                    thumbnailUrl = thumbnailUrl
                )

                when (val result = repository.createPost(
                    userId = currentUserId,
                    postText = postText,
                    postImages = imageUrls,
                    postVideo = videoUrl,
                    imageDescription = imageDescription,
                    videoDescription = videoDescription,
                    thumbnailUrl = thumbnailUrl
                )) {
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

    @androidx.annotation.OptIn(UnstableApi::class)
    fun savePollVote(postId: String, userId: String, option: String) {
        val firestore = Firebase.firestore
        firestore.collection("posts").document(postId)
            .collection("votes").document(userId)
            .set(mapOf("selectedOption" to option, "userId" to userId)) // Ensure userId is stored
            .addOnSuccessListener { Log.d("PostViewModel", "Vote saved for post $postId by user $userId") }
            .addOnFailureListener { e -> Log.e("PostViewModel", "Failed to save vote: ${e.message}") }
            .addOnSuccessListener {
                // Update local cache after saving vote
                voteCache[postId] = true
                saveVoteCache() // Persist cache in a coroutine
                // Update postsFlow to reflect the vote
                val updatedPosts = _postsFlow.value.map { postWithUser ->
                    if (postWithUser.post.postId == postId) {
                        postWithUser.copy(
                            post = postWithUser.post.copy(hasUserVoted = true)
                        )
                    } else postWithUser
                }
                _postsFlow.value = updatedPosts
            }
    }

    suspend fun getPollVote(postId: String, userId: String): String? = withContext(EmptyCoroutineContext) {
        val firestore = Firebase.firestore
        val snapshot = firestore.collection("posts").document(postId)
            .collection("votes").document(userId).get().await()
        snapshot.getString("selectedOption")
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
                    postImages = emptyList(),
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
                        savePollVote(postId, userId, option) // Save vote and update cache/UI
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
                    // Update local state immediately
                    val currentLikes = _likeState.value[postId] ?: emptyList()
                    _likeState.value = _likeState.value + (postId to if (currentLikes.contains(userId)) currentLikes - userId else currentLikes + userId)
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
                    val bytesTransferred = taskSnapshot.bytesTransferred
                    val totalByteCount = taskSnapshot.totalByteCount

                    if (totalByteCount > 0) {
                        val progress = (100.0 * bytesTransferred / totalByteCount).toFloat()
                        Log.d("PostViewModel", "Upload progress: $progress%")
                        _uploadProgress.value = progress
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

    private suspend fun generateThumbnailBitmap(videoUri: Uri): Bitmap {
        return withContext(dispatchers.io) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap ?: throw IllegalStateException("Failed to generate thumbnail")
        }
    }

    private suspend fun saveBitmapToTempFile(bitmap: Bitmap): Uri {
        return withContext(dispatchers.io) {
            val file = File.createTempFile("thumbnail_", ".jpg", context.cacheDir)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(file)
        }
    }

    private fun listenForLikesAndComments() {
        viewModelScope.launch(dispatchers.io) {
            repository.getPostsWithUsersFlow().collect { postsWithUsers ->
                postsWithUsers.forEach { postWithUser ->
                    val postId = postWithUser.post.postId
                    val firestore = Firebase.firestore
                    val postRef = firestore.collection("posts").document(postId)

                    // Real-time listener for likes
                    val likesListener = postRef.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PostViewModel", "Error listening for likes on post $postId: ${error.message}")
                            return@addSnapshotListener
                        }
                        val likes = snapshot?.get("likes") as? List<String> ?: emptyList()
                        _likeState.value = _likeState.value + (postId to likes)
                    }
                    likesListeners[postId] = likesListener

                    // Real-time listener for comment count
                    val commentsRef = postRef.collection("comments")
                    val commentsListener = commentsRef.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PostViewModel", "Error listening for comments on post $postId: ${error.message}")
                            return@addSnapshotListener
                        }
                        val commentCount = snapshot?.size() ?: 0
                        _commentCountState.value = _commentCountState.value + (postId to commentCount)
                    }
                    commentsListeners[postId] = commentsListener
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up all listeners when the ViewModel is cleared
        likesListeners.values.forEach { it.remove() }
        commentsListeners.values.forEach { it.remove() }
        likesListeners.clear()
        commentsListeners.clear()
    }

    sealed class PostCreationStatus {
        object Idle : PostCreationStatus()
        object Creating : PostCreationStatus()
        object Success : PostCreationStatus()
        data class Error(val message: String) : PostCreationStatus()
    }
}