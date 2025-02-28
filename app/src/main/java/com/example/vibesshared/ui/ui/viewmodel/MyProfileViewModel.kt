package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.Badge
import com.example.vibesshared.ui.ui.data.User
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.data.toUserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.BadgeRepository
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.LoginTracker
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.utils.Result.Success
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    val badgeRepository: BadgeRepository,
    private val dispatchers: DispatcherProvider,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loadingStatus = MutableStateFlow<LoadingStatus>(LoadingStatus.Idle)
    val loadingStatus: StateFlow<LoadingStatus> = _loadingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _newlyAwardedBadge = MutableStateFlow<Badge?>(null)
    val newlyAwardedBadge: StateFlow<Badge?> = _newlyAwardedBadge.asStateFlow()

    private var hasCheckedFirstLogin = false // Flag to prevent repeated checks

    init {
        val userId = repository.getCurrentUser()?.uid
        if (userId != null) {
            loadUserProfile(userId)
            checkAndAwardFirstLogin(userId)
        }
    }

    // Updated function to check login and award badge only once
    fun checkAndAwardFirstLogin(userId: String) {
        if (hasCheckedFirstLogin) return // Skip if already checked
        viewModelScope.launch(dispatchers.io) {
            try {
                val count = LoginTracker.getLoginCount(context).first() // Get initial count once
                Log.d("MyProfileViewModel", "Login count: $count")
                if (count == 0) { // First login
                    awardFirstLoginBadge(userId)
                }
                LoginTracker.incrementLoginCount(context) // Increment only once
                hasCheckedFirstLogin = true // Mark as checked
            } catch (e: Exception) {
                Log.e("MyProfileViewModel", "Error checking login count: ${e.message}")
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch(dispatchers.io) {
            _loadingStatus.value = LoadingStatus.Loading
            _errorMessage.value = null

            repository.getUserFlow(userId).collectLatest { userProfile ->
                _userProfile.value = userProfile
                if (userProfile != null) {
                    Log.d("MyProfileViewModel", "Loaded user profile: badges=${userProfile.badges}, badgeDates=${userProfile.badgeDates}")
                    checkPostBadges(userId)
                    _loadingStatus.value = LoadingStatus.Success
                } else {
                    _loadingStatus.value = LoadingStatus.Error
                }
            }
        }
    }

    fun saveUserProfile(
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        profilePictureUri: Uri?
    ) {
        viewModelScope.launch(dispatchers.io) {
            _loadingStatus.value = LoadingStatus.Loading
            _errorMessage.value = null

            val profilePictureUrlResult = if (profilePictureUri != null) {
                repository.updateProfilePicture(userId, profilePictureUri)
            } else {
                Success(userProfile.value?.profilePictureUrl ?: "")
            }

            when (profilePictureUrlResult) {
                is Success -> {
                    val profilePictureUrl = profilePictureUrlResult.data
                    val updatedUser = User(
                        userId = userId,
                        userName = userName,
                        firstName = firstName,
                        lastName = lastName,
                        profilePictureUrl = profilePictureUrl,
                        email = userProfile.value?.email ?: "",
                        dob = userProfile.value?.dob ?: ""
                    )

                    when (val updateResult = repository.updateUserProfile(updatedUser.toUserProfile())) {
                        is Success -> _loadingStatus.value = LoadingStatus.Success
                        is Result.Failure -> {
                            _errorMessage.value = "Failed to update profile: ${updateResult.exception.message}"
                            _loadingStatus.value = LoadingStatus.Error
                        }
                        is Result.Loading -> {} // No action needed during loading
                    }
                }
                is Result.Failure -> {
                    _loadingStatus.value = LoadingStatus.Error
                    _errorMessage.value = "Profile picture upload failed: ${profilePictureUrlResult.exception.message}"
                }
                is Result.Loading -> {} // No action needed during loading
            }
        }
    }

    private suspend fun awardFirstLoginBadge(userId: String) {
        val badge = badgeRepository.getBadge("first_login")
        if (badge != null && !(_userProfile.value?.badges?.contains(badge.badgeId) ?: false)) {
            val currentDate = Timestamp.now()
            Log.d("MyProfileViewModel", "Awarding First Login badge for user $userId with date $currentDate")

            val updatedBadge = badge.copy(acquiredDate = currentDate)
            badgeRepository.addBadge(updatedBadge)

            val updatedBadges = listOf(badge.badgeId)
            val badgeData = mapOf(
                "badges" to updatedBadges,
                "badgeDates" to mapOf(badge.badgeId to currentDate)
            )
            when (val result = repository.updateUserBadgesWithDate(userId, badgeData)) {
                is Success -> {
                    _userProfile.value = _userProfile.value?.copy(
                        badges = updatedBadges,
                        badgeDates = mapOf(badge.badgeId to currentDate)
                    )
                    _newlyAwardedBadge.value = updatedBadge // Trigger pop-up
                    Log.d("MyProfileViewModel", "Successfully awarded First Login badge")
                }
                is Result.Failure -> {
                    _errorMessage.value = "Failed to award First Login badge: ${result.exception.message}"
                }
                is Result.Loading -> {} // No action needed during loading
            }
        }
    }

    suspend fun awardFirstPostBadge(userId: String) {
        val badge = badgeRepository.getBadge("first_post")
        if (badge != null && _userProfile.value?.badges?.contains(badge.badgeId) != true) {
            val currentDate = Timestamp.now()
            Log.d("MyProfileViewModel", "Awarding First Post badge for user $userId with date $currentDate")

            val updatedBadge = badge.copy(acquiredDate = currentDate)
            badgeRepository.addBadge(updatedBadge)

            val updatedBadges = (_userProfile.value?.badges ?: emptyList()) + badge.badgeId
            val badgeData = mapOf(
                "badges" to updatedBadges,
                "badgeDates" to (_userProfile.value?.badgeDates ?: emptyMap()) + mapOf(badge.badgeId to currentDate)
            )
            when (val result = repository.updateUserBadgesWithDate(userId, badgeData)) {
                is Success -> {
                    _userProfile.value = _userProfile.value?.copy(
                        badges = updatedBadges,
                        badgeDates = (_userProfile.value?.badgeDates ?: emptyMap()) + mapOf(badge.badgeId to currentDate)
                    )
                    _newlyAwardedBadge.value = updatedBadge
                    Log.d("MyProfileViewModel", "Successfully awarded First Post badge")
                }
                is Result.Failure -> {
                    _errorMessage.value = "Failed to award First Post badge: ${result.exception.message}"
                }
                is Result.Loading -> {} // No action needed during loading
            }
        }
    }

    suspend fun awardFivePostsBadge(userId: String) {
        val badge = badgeRepository.getBadge("five_posts")
        if (badge != null && _userProfile.value?.badges?.contains(badge.badgeId) != true) {
            val postCount = repository.getUserPostCount(userId)
            if (postCount >= 5) {
                val currentDate = Timestamp.now()
                Log.d("MyProfileViewModel", "Awarding 5 Posts badge for user $userId with date $currentDate")

                val updatedBadge = badge.copy(acquiredDate = currentDate)
                badgeRepository.addBadge(updatedBadge)

                val updatedBadges = (_userProfile.value?.badges ?: emptyList()) + badge.badgeId
                val badgeData = mapOf(
                    "badges" to updatedBadges,
                    "badgeDates" to (_userProfile.value?.badgeDates ?: emptyMap()) + mapOf(badge.badgeId to currentDate)
                )
                when (val result = repository.updateUserBadgesWithDate(userId, badgeData)) {
                    is Success -> {
                        _userProfile.value = _userProfile.value?.copy(
                            badges = updatedBadges,
                            badgeDates = (_userProfile.value?.badgeDates ?: emptyMap()) + mapOf(badge.badgeId to currentDate)
                        )
                        _newlyAwardedBadge.value = updatedBadge
                        Log.d("MyProfileViewModel", "Successfully awarded 5 Posts badge")
                    }
                    is Result.Failure -> {
                        _errorMessage.value = "Failed to award 5 Posts badge: ${result.exception.message}"
                    }
                    is Result.Loading -> {} // No action needed during loading
                }
            }
        }
    }

    private suspend fun checkPostBadges(userId: String) {
        awardFirstPostBadge(userId)
        awardFivePostsBadge(userId)
    }

    fun clearNewlyAwardedBadge() {
        _newlyAwardedBadge.value = null
    }

    sealed class LoadingStatus {
        object Idle : LoadingStatus()
        object Loading : LoadingStatus()
        object Success : LoadingStatus()
        object Error : LoadingStatus()
    }
}