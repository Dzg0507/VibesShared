package com.example.vibesshared.ui.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.User
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.data.toUserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.utils.Result.Success
import com.example.vibesshared.ui.ui.viewmodel.MyProfileViewModel.LoadingStatus.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val dispatchers: DispatcherProvider // Inject dispatcher
) : ViewModel() {

    private val _loadingStatus = MutableStateFlow<LoadingStatus>(Idle) //Use StateFlow
    val loadingStatus: StateFlow<LoadingStatus> = _loadingStatus.asStateFlow() //Use StateFlow

    private val _errorMessage = MutableStateFlow<String?>(null) //Use StateFlow
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow() //Use StateFlow

    private val _userProfile = MutableStateFlow<UserProfile?>(null) // Use UserProfile?, not User?
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow() // And here

    fun loadUserProfile(userId: String) {
        viewModelScope.launch(dispatchers.io) {
            _loadingStatus.value = Loading
            _errorMessage.value = null

            repository.getUserFlow(userId).collectLatest { userProfile -> // Use collectLatest
                _userProfile.value = userProfile // Directly assign UserProfile?
                if (userProfile != null) {
                    _loadingStatus.value = Success
                } else {
                    _loadingStatus.value = Error // You might want a more specific error
                }
            }
        }
    }



    // Save user profile
    fun saveUserProfile(
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        profilePictureUri: Uri?
    ) {
        viewModelScope.launch(dispatchers.io) { // Use injected dispatcher
            _loadingStatus.value = Loading
            _errorMessage.value = null

            // Update profile picture if provided
            val profilePictureUrlResult = if (profilePictureUri != null) {
                repository.updateProfilePicture(userId, profilePictureUri)
            } else {
                // If no new image, keep the existing URL.  Important!
                Success(userProfile.value?.profilePictureUrl ?: "") // Keep existing URL
            }


            when (profilePictureUrlResult) {
                is Success -> {
                    val profilePictureUrl = profilePictureUrlResult.data
                    //Prepare User
                    val updatedUser = User(
                        userId = userId,
                        userName = userName,
                        firstName = firstName,
                        lastName = lastName,
                        profilePictureUrl = profilePictureUrl,
                        email = userProfile.value?.email ?: "", // Keep existing email
                        dob = userProfile.value?.dob ?: "" // Keep existing dob
                    )

                    // Update user profile in Firestore
                    when (val updateResult = repository.updateUserProfile(updatedUser.toUserProfile())) {
                        is Success -> _loadingStatus.value = Success
                        is Result.Failure -> {
                            _errorMessage.value = "Failed to update profile: ${updateResult.exception.message}"
                            _loadingStatus.value = Error
                        }

                        is Result.Loading -> TODO()
                    }
                }
                is Result.Failure -> {
                    _loadingStatus.value = Error
                    _errorMessage.value = "Profile picture upload failed: ${profilePictureUrlResult.exception.message}"
                }

                is Result.Loading -> TODO()
            }
        }
    }


    sealed class LoadingStatus {
        object Idle : LoadingStatus()
        object Loading : LoadingStatus()
        object Success : LoadingStatus()
        object Error : LoadingStatus()
    }
}