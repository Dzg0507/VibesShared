package com.example.vibesshared.ui.ui.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) {

    suspend fun uploadImage(imageUri: Uri, destinationPath: String): String? = withContext(dispatchers.io) {
        Log.d("StorageRepository", "Starting image upload to path: $destinationPath, expecting auto-created folder: chatMedia")
        try {
            val fileExtension = imageUri.getMimeType(context)?.substringAfterLast("/") ?: "jpg"
            // Ensure no leading slash in the path
            val cleanDestinationPath = destinationPath.trimStart('/')
            val imageRef = storage.reference.child(cleanDestinationPath).child("${UUID.randomUUID()}.$fileExtension")
            Log.d("StorageRepository", "Uploading image to reference: ${imageRef.path}, bucket: ${storage.reference.bucket}, expecting auto-creation of chatMedia folder, clean path: $cleanDestinationPath")

            val uploadTask = imageRef.putFile(imageUri)
            val snapshot = uploadTask.await()
            val downloadUrl = snapshot.storage.downloadUrl.await().toString()
            Log.d("StorageRepository", "Image uploaded successfully, URL: $downloadUrl, to auto-created folder: chatMedia")
            downloadUrl
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error uploading image to path: $destinationPath: ${e.message}", e)
            null
        }
    }

    suspend fun uploadVideo(videoUri: Uri, destinationPath: String): String? = withContext(dispatchers.io) {
        Log.d("StorageRepository", "Starting video upload to path: $destinationPath, expecting auto-created folder: chatMedia")
        try {
            val fileExtension = videoUri.getMimeType(context)?.substringAfterLast("/") ?: "mp4"
            // Ensure no leading slash in the path
            val cleanDestinationPath = destinationPath.trimStart('/')
            val videoRef = storage.reference.child(cleanDestinationPath).child("${UUID.randomUUID()}.$fileExtension")
            Log.d("StorageRepository", "Uploading video to reference: ${videoRef.path}, bucket: ${storage.reference.bucket}, expecting auto-creation of chatMedia folder, clean path: $cleanDestinationPath")

            val uploadTask = videoRef.putFile(videoUri)
            val snapshot = uploadTask.await()
            val downloadUrl = snapshot.storage.downloadUrl.await().toString()
            Log.d("StorageRepository", "Video uploaded successfully, URL: $downloadUrl, to auto-created folder: chatMedia")
            downloadUrl
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error uploading video to path: $destinationPath: ${e.message}", e)
            null
        }
    }

    private fun Uri.getMimeType(context: Context): String? {
        return context.contentResolver.getType(this)
    }
}