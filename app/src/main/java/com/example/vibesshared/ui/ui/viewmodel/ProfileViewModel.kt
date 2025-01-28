package com.example.vibesshared.ui.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    fun loadProfile(userId: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val targetUserId = userId ?: auth.currentUser?.uid
                ?: throw Exception("No user ID provided")

                val userDoc = firestore.collection("users")
                    .document(targetUserId)
                    .get()
                    .await()

                val profile = userDoc.toObject(UserProfile::class.java)
                    ?: throw Exception("Profile not found")

                _profileState.value = ProfileState.Success(
                    profile = profile,
                    isCurrentUser = targetUserId == auth.currentUser?.uid
                )
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        bio: String,
        profilePicture: Uri?,
        experience: String,
        favoriteLanguage: String,
        specialty: String,
        currentProject: String,
        learning: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                    ?: throw Exception("No authenticated user")

                val profilePictureUrl = profilePicture?.let { uploadProfilePicture(it) }
                    ?: ((_profileState.value as? ProfileState.Success)?.profile?.profilePicture)

                val userProfile = UserProfile(
                    userId = currentUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = currentUser.email ?: "",
                    bio = bio,
                    profilePicture = profilePictureUrl,
                    experience = experience,
                    favoriteLanguage = favoriteLanguage,
                    specialty = specialty,
                    currentProject = currentProject,
                    learning = learning
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(userProfile)
                    .await()

                _profileState.value = ProfileState.Success(userProfile, isCurrentUser = true)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }



    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun searchUsers(query: String): List<UserProfile> {
        return try {
            val usersRef = firestore.collection("users")
            val querySnapshot = usersRef
                .orderBy("firstName")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun uploadProfilePicture(imageUri: Uri): String? {
        return try {
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("profilePictures/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error uploading image: ${e.message}")
            null
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _profileState.value = ProfileState.Loading
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile, val isCurrentUser: Boolean) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
