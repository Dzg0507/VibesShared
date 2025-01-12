package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(private val context: Context) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()


    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userRef = database.reference.child("users").child(userId)
                    val snapshot = userRef.get().await()

                    if (snapshot.exists()) {
                        _userProfile.value = snapshot.getValue(UserProfile::class.java)
                    } else {
                        _userProfile.value = null // Or handle new user
                    }
                }
            } catch (e: Exception) {
                _userProfile.value = null
                // Handle exceptions (log, show error message, etc.)
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
            val userProfile = UserProfile(
                firstName = firstName,
                lastName = lastName,
                email = email,
                bio = bio,
                profilePictureUri = profilePictureUri
            )

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val userRef = database.reference.child("users").child(userId)
                userRef.setValue(userProfile) // No need for await() here if not using the result
            }
        }
    }


    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                        return ProfileViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}