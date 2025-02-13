package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.FriendRequest
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val auth: FirebaseAuth,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _navigateToMessaging = MutableSharedFlow<String>()
    val navigateToMessaging = _navigateToMessaging.asSharedFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _friendRequestStatus = MutableStateFlow<Result<Unit>?>(null)
    val friendRequestStatus: StateFlow<Result<Unit>?> = _friendRequestStatus.asStateFlow()

    private val _receivedFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedFriendRequests: StateFlow<List<FriendRequest>> = _receivedFriendRequests.asStateFlow()

    fun searchUsers(query: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid?: return@launch
            when (val friendsResult = repository.getFriends(currentUserId)) {
                is Result.Success -> {
                    val friendIds = friendsResult.data.map { it.userId }
                    when (val result = repository.searchUsers(query, currentUserId)) {
                        is Result.Success -> {
                            val filteredUsers = result.data.filter { user ->
                                user.userId!= currentUserId &&!friendIds.contains(user.userId)
                            }
                            _users.value = filteredUsers
                        }
                        is Result.Failure -> _errorMessage.emit("Search failed: ${result.exception.message}")
                        is Result.Loading -> {} // Add Loading branch
                    }
                }
                is Result.Failure -> _errorMessage.emit("Failed to load friends: ${friendsResult.exception.message}")
                is Result.Loading -> {} // Add Loading branch
            }
        }
    }

    fun createOrNavigateToChat(friendUid: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid?: return@launch

            val chatId = repository.findExistingChat(currentUserId, friendUid)
            if (chatId!= null) {
                _navigateToMessaging.emit(chatId)
            } else {
                val newChatId = repository.createChat(currentUserId, friendUid)
                _navigateToMessaging.emit(newChatId)
            }
        }
    }

    fun getFriends() {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid?: return@launch
            when (val result = repository.getFriends(currentUserId)) {
                is Result.Success -> _friends.value = result.data
                is Result.Failure -> _errorMessage.emit("Failed to load friends: ${result.exception.message}")
                is Result.Loading -> {} // Add Loading branch
            }
        }
    }

    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch(dispatchers.io) {
            _friendRequestStatus.value = Result.Loading()
            val result = repository.sendFriendRequest(receiverId)
            _friendRequestStatus.value = result
        }
    }

    fun resetFriendRequestStatus() {
        _friendRequestStatus.value = null
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch(dispatchers.io) {
            when (val result = repository.acceptFriendRequest(request.requestId)) {
                is Result.Success -> {} // Handle success in UI
                is Result.Failure -> _errorMessage.emit("Failed to accept request: ${result.exception.message}")
                is Result.Loading -> {} // Add Loading branch
            }
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch(dispatchers.io) {
            when (val result = repository.rejectFriendRequest(requestId)) {
                is Result.Success -> {} // Handle success in UI
                is Result.Failure -> _errorMessage.emit("Failed to reject request: ${result.exception.message}")
                is Result.Loading -> {} // Add Loading branch
            }
        }
    }

    fun loadReceivedFriendRequests(currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val requests = repository.getReceivedFriendRequests(currentUserId)
                _receivedFriendRequests.value = requests
            } catch (e: Exception) {
                _errorMessage.emit("Failed to load friend requests.")
            }
        }
    }
}