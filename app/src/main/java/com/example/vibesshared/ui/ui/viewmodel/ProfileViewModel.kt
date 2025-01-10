package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val auth = Firebase.auth
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.reference.child("users").child(auth.currentUser?.uid ?: "")

    init {
        fetchUserProfile()
    }


    private fun fetchUserProfile() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                _userProfile.value = profile
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    companion object { // Add this companion object
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


    fun saveUserProfile(
        firstName: String,
        lastName: String,
        email: String,
        bio: String,
        profilePictureUri: String? // Add profilePictureUri parameter
    ) {
        viewModelScope.launch {
            val userProfile = UserProfile(
                firstName = firstName,
                lastName = lastName,
                email = email,
                bio = bio,
                profilePictureUri = profilePictureUri
            )
            userRef.setValue(userProfile)
        }
    }
}