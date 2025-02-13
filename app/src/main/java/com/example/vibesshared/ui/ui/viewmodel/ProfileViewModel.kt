package com.example.vibesshared.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

//Define a data class to model your User Profile
data class UserProfile(
    val userId: String = "",
    val userName: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val profilePictureUrl: String? = "",
    val email: String? = "" // Add other fields as needed
)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userDocRef = firestore.collection("users").document(userId)
                val userSnapshot = userDocRef.get().await()

                if (userSnapshot.exists()) {
                    val userProfile = userSnapshot.toObject(UserProfile::class.java)
                    if (userProfile != null) {
                        _uiState.value = ProfileUiState.Success(userProfile)
                    }else{
                        _uiState.value = ProfileUiState.Error("Failed to parse user data.")
                    }

                } else {
                    _uiState.value = ProfileUiState.Error("User profile not found.")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.value = ProfileUiState.Error("Failed to load profile: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // Sealed interface for UI states
    sealed interface ProfileUiState {
        object Loading : ProfileUiState
        data class Success(val profile: UserProfile) : ProfileUiState
        data class Error(val message: String) : ProfileUiState
    }
}