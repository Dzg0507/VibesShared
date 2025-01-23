/*
package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.DataManager
import com.example.vibesshared.ui.ui.data.Result
import com.example.vibesshared.ui.ui.data.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                dataManager.loadChats(dataManager.auth.currentUser?.uid ?: "")
                    .collectLatest {
                        _chats.value = it
                    }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun createNewChat(userIds: List<String>) {
        viewModelScope.launch {
            when (val result = dataManager.createNewChat(userIds)) {
                is Result.Success -> {
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToRoute(
                            "messaging/${result.data}"
                        )
                    )
                }
                is Result.Error -> {
                    // Handle the error
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            when (val result = dataManager.deleteChat(chatId)) {
                is Result.Success -> {
                    // Chat deleted successfully
                }
                is Result.Error -> {
                    // Handle the error
                }
            }
        }
    }
}package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.data.DataManager
import com.example.vibesshared.data.Result
import com.example.vibesshared.ui.ui.data.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                dataManager.loadChats(dataManager.auth.currentUser?.uid ?: "")
                    .collectLatest {
                        _chats.value = it
                    }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun createNewChat(userIds: List<String>) {
        viewModelScope.launch {
            when (val result = dataManager.createNewChat(userIds)) {
                is Result.Success -> {
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToRoute(
                            "messaging/${result.data}"
                        )
                    )
                }

                is Result.Error -> {
                    // Handle the error
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            when (val result = dataManager.deleteChat(chatId)) {
                is Result.Success -> {
                    // Chat deleted successfully
                }

                is Result.Error -> {
                    // Handle the error
                }
            }
        }
    }
}
*/