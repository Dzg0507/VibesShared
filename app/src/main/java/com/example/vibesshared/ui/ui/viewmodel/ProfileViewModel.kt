package com.example.vibesshared.ui.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel: ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    private val _profileError = MutableStateFlow<String?>(null) // Added error state

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore


    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch // Handle null userId
                val userRef = db.collection("users").document(userId)
                val snapshot = userRef.get().await()
                _userProfile.value =
                    snapshot.toObject(UserProfile::class.java)?.copy(userId = userId)
                _profileError.value = null // Clear any previous error
            } catch (e: Exception) {
                _userProfile.value = null
                _profileError.value =
                    "Error fetching user profile: ${e.message}" // More informative error message
            }
        }
    }

    fun saveUserProfile(
        firstName: String,
        lastName: String,
        email: String,
        bio: String,
        profilePictureUri: String?
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch // Handle null userId
            val userProfile = UserProfile(
                userId = userId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                bio = bio,
                profilePictureUri = profilePictureUri
            )
            try {
                db.collection("users").document(userId).set(userProfile).await()
                _userProfile.value = userProfile
                _profileError.value = null // Clear any previous error
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving user profile", e)
                _profileError.value =
                    "Error saving user profile: ${e.message}" // More informative error message
            }
        }
    }

    fun clearUserProfile() {
        _userProfile.value = null
    }



    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val storageRef =
                FirebaseStorage.getInstance().reference.child("profilePictures/$userId")
            try {
                val uploadTask = storageRef.putFile(imageUri)
                val downloadUrl = uploadTask.await().storage.downloadUrl.await().toString()
                saveUserProfile(
                    firstName = _userProfile.value?.firstName ?: "",
                    lastName = _userProfile.value?.lastName ?: "",
                    email = _userProfile.value?.email ?: "",
                    bio = _userProfile.value?.bio ?: "",
                    profilePictureUri = downloadUrl
                )
                _userProfile.value = _userProfile.value?.copy(profilePictureUri = downloadUrl)
                _profileError.value = null // Clear any previous error
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading image", e)
                _profileError.value =
                    "Error uploading image: ${e.message}" // More informative error message
            }
        }
    }
}



