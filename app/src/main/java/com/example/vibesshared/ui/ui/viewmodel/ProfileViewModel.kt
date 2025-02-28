package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.Badge
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.BadgeRepository
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel.ProfileUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//Define a data class to model your User Profile
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val badgeRepository: BadgeRepository, // Add BadgeRepository
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = Loading
            when (val result = firebaseRepository.getUserProfile(userId)) {
                is Result.Success -> {
                    val userProfile = result.data
                    // Fetch badges for the user
                    val badges = userProfile.badges.mapNotNull { badgeId ->
                        badgeRepository.getBadge(badgeId)
                    }
                    _uiState.value = Success(userProfile, badges)
                }
                is Result.Failure -> {
                    _uiState.value = Error(result.exception.message ?: "Unknown error")
                }
                else -> {
                    _uiState.value = Error("Unknown error")
                } // No need to handle Result.Loading here
            }
        }
    }

    sealed class ProfileUiState {
        object Loading : ProfileUiState()
        data class Success(val profile: UserProfile, val badges: List<Badge>) : ProfileUiState() // Include badges
        data class Error(val message: String) : ProfileUiState()
    }
}