package com.example.vibesshared.ui.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.Message
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ChatWithUserInfo(
    val chatId: String,
    val otherUser: UserProfile,
    val lastMessage: String,
    val lastMessageTimestamp: Timestamp?
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val auth: FirebaseAuth,
    private val dispatchers: DispatcherProvider

) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatWithUserInfo>>(emptyList()) // Use ChatWithUserInfo
    val chats = _chats.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun getUserChats() {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            repository.getUserChatsFlow(currentUserId)
                .map { chatList -> // Transform the Flow<List<Chat>>
                    chatList.mapNotNull { chat ->
                        val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return@mapNotNull null
                        when (val userResult = repository.getUserProfile(otherUserId)) { // Corrected call
                            is Result.Success<UserProfile> -> {
                                ChatWithUserInfo(
                                    chatId = chat.chatId,
                                    otherUser = userResult.data,
                                    lastMessage = chat.lastMessage,
                                    lastMessageTimestamp = chat.lastMessageTimestamp
                                )
                            }
                            is Result.Failure -> {
                                Log.e("ChatsViewModel", "Failed to get user details: ${userResult.exception}")
                                null // Skip this chat if user details can't be fetched
                            }

                            is Result.Loading -> TODO()
                        }
                    }
                }
                .collect { chatWithUserInfoList ->
                    _chats.value = chatWithUserInfoList
                }
        }
    }



    fun sendMessage(chatId: String, text: String, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            val message = Message(
                chatId = chatId,
                senderId = currentUserId,
                text = text,
                timestamp = Timestamp.now(),
                type = "text"
            )

            when (val result = repository.sendMessage(message)) {
                is Result.Success<Unit> -> updateLastMessage(chatId, text, currentUserId)
                is Result.Failure -> _errorMessage.emit("Send failed: ${result.exception.message}")
                is Result.Loading -> TODO()
            }
        }
    }

    fun getMessages(chatId: String) {
        viewModelScope.launch(dispatchers.io) {
            repository.getMessagesFlow(chatId).collect { messages ->
                _messages.value = messages.sortedBy { it.timestamp }
            }
        }
    }
    fun deleteChat(chatId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val deleteMessage = Message(
                    chatId = chatId,
                    senderId = currentUserId,
                    text = "Chat deleted",
                    type = "system",
                    timestamp = Timestamp.now()
                )

                repository.sendMessage(deleteMessage)
                repository.getChatReference(chatId).delete().await()
            } catch (e: Exception) {
                _errorMessage.emit("Delete failed: ${e.message}")
            }
        }
    }

    fun sendImageMessage(chatId: String, imageUri: Uri, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            when (val uploadResult = repository.uploadChatImage(chatId, imageUri)) {
                is Result.Success<String> -> {
                    val message = Message(
                        chatId = chatId,
                        senderId = currentUserId,
                        imageUrl = uploadResult.data,
                        type = "image",
                        timestamp = Timestamp.now()
                    )

                    when (val sendResult = repository.sendMessage(message)) {
                        is Result.Success<Unit> -> updateLastMessage(chatId, "[image]", currentUserId)
                        is Result.Failure -> _errorMessage.emit("Failed to send image message")
                        is Result.Loading -> TODO()
                    }
                }
                is Result.Failure -> _errorMessage.emit("Image upload failed: ${uploadResult.exception.message}")
                is Result.Loading -> TODO()
            }
        }
    }

    fun markMessagesAsRead(chatId: String, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            when (val result = repository.markMessagesAsRead(chatId, currentUserId)) {
                is Result.Success<Unit> -> Unit
                is Result.Failure -> _errorMessage.emit("Mark read failed: ${result.exception.message}")
                is Result.Loading -> TODO()
            }
        }
    }

    private suspend fun updateLastMessage(chatId: String, lastMessage: String, currentUserId: String) {
        try {
            repository.getChatReference(chatId).update(
                mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "lastMessageSender" to currentUserId
                )
            ).await()
        } catch (e: Exception) {
            Log.e("ChatsViewModel", "Error updating last message", e)
            _errorMessage.emit("Failed to update last message: ${e.message}")
        }
    }
}