package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    val postsFlow: Flow<List<Post>> = flow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                val querySnapshot = firebaseFirestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val posts = querySnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val caption = document.getString("caption") ?: ""
                    val imageUrl = document.getString("imageUrl")
                    val timestamp = document.getLong("timestamp") ?: 0L

                    Post(id, userId, caption, imageUrl, timestamp)
                }
                emit(posts)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching posts", e)
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }

    private val _posts = mutableStateListOf<Post>()
    val posts: List<Post> = _posts

    private val _selectedImages = mutableStateListOf<Uri>()
    val selectedImages: List<Uri> = _selectedImages

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    fun addPost(text: String, images: List<Uri>, context: Context) {
        viewModelScope.launch {
            try {
                val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch

                val imageUrl = if (images.isNotEmpty()) {
                    uploadImageToFirebase(images.first(), context)
                } else null

                val newPost = Post(
                    userId = currentUserId,
                    postText = text,
                    postImage = imageUrl
                )

                firebaseFirestore.collection("posts")
                    .document(newPost.postId)
                    .set(newPost)
                    .await()

                _posts.add(0, newPost)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error uploading post", e)
            }
        }
    }


    private suspend fun uploadImageToFirebase(uri: Uri, context: Context): String {
        val storageRef = firebaseStorage.reference
        val imageFileName = "postImages/${UUID.randomUUID()}"
        val imageRef = storageRef.child(imageFileName)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open input stream for $uri")

        return try {
            val uploadTask = imageRef.putStream(inputStream)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                _uploadProgress.value = progress
            }.await()

            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d("PostViewModel", "Image uploaded, download URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("PostViewModel", "Error uploading image", e)
            throw e
        }
    }

    fun addImage(uri: Uri) {
        _selectedImages.add(uri)
    }

    fun removeImage(uri: Uri) {
        _selectedImages.remove(uri)
    }

    fun clearSelectedImages() {
        _selectedImages.clear()
    }
}