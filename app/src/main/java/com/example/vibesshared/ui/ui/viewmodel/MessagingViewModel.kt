/*
package com.example.vibesshared.ui.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.DataManager
import com.example.vibesshared.ui.ui.data.Result
import com.example.vibesshared.ui.ui.data.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                dataManager.loadMessages(chatId).collectLatest {
                    _messages.value = it
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            val currentUserId = dataManager.auth.currentUser?.uid ?: return@launch
            dataManager.sendMessage(chatId, text, currentUserId)
        }
    }

    fun sendImageMessage(chatId: String, imageUri: Uri) {
        viewModelScope.launch {
            val currentUserId = dataManager.auth.currentUser?.uid ?: return@launch
            when (val result = dataManager.sendImageMessage(chatId, imageUri, currentUserId)) {
                is Result.Success -> {
                    // Image message sent successfully
                }

                is Result.Error -> {
                    // Handle error sending image message
                }
            }
        }
    }

    fun markMessagesAsRead(chatId: String) {
        viewModelScope.launch {
            val currentUserId = dataManager.auth.currentUser?.uid ?: return@launch
            when (val result = dataManager.markMessagesAsRead(chatId, currentUserId)) {
                is Result.Success -> {
                    // Messages marked as read successfully
                }

                is Result.Error -> {
                    // Handle error marking messages as read
                }
            }
        }
    }
}

*/