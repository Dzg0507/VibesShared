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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
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

    private val _chats = MutableStateFlow<List<ChatWithUserInfo>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        getUserChats()
    }

    fun updateMessages(messages: List<Message>) {
        _messages.value = messages.sortedBy { it.timestamp?.seconds ?: 0 }
    }

    fun getUserChats() {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            Log.d("ChatsViewModel", "Fetching user chats for userId: $currentUserId")
            repository.getUserChatsFlow(currentUserId)
                .map { chatList ->
                    chatList.mapNotNull { chat ->
                        val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return@mapNotNull null
                        when (val userResult = repository.getUserProfile(otherUserId)) {
                            is Result.Success -> ChatWithUserInfo(
                                chatId = chat.chatId,
                                otherUser = userResult.data,
                                lastMessage = chat.lastMessage,
                                lastMessageTimestamp = chat.lastMessageTimestamp
                            )
                            is Result.Failure -> {
                                Log.e("ChatsViewModel", "Failed to get user details for chat: ${userResult.exception}")
                                null
                            }
                            is Result.Loading -> {
                                Log.d("ChatsViewModel", "Loading user profile for chat with otherUserId: $otherUserId")
                                null
                            }
                        }
                    }.sortedByDescending { it.lastMessageTimestamp?.seconds ?: 0 }
                }
                .collect { _chats.value = it }
        }
    }

    fun sendMessage(chatId: String, text: String, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.emit("User not authenticated")
                Log.e("ChatsViewModel", "User not authenticated for text message, chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Sending text message for chatId: $chatId, userId: $currentUserId")
            _isLoading.value = true
            val message = Message(
                chatId = chatId,
                senderId = currentUserId,
                text = text,
                timestamp = Timestamp.now(),
                type = "text"
            )
            _messages.value = (_messages.value + message).sortedBy { it.timestamp?.seconds ?: 0 }

            when (val result = repository.sendMessage(message)) {
                is Result.Success -> {
                    updateLastMessage(chatId, text, currentUserId)
                    _isLoading.value = false
                    Log.d("ChatsViewModel", "Text message sent successfully for chatId: $chatId, Firestore path: chats/$chatId/messages/${message.messageId}")
                }
                is Result.Failure -> {
                    _errorMessage.emit("Send failed: ${result.exception.message}")
                    _messages.value = _messages.value.filter { it != message }
                    _isLoading.value = false
                    Log.e("ChatsViewModel", "Failed to send text message for chatId: $chatId: ${result.exception.message}")
                }
                is Result.Loading -> {
                    Log.d("ChatsViewModel", "Sending text message in progress for chatId: $chatId")
                }
            }
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> {
        Log.d("ChatsViewModel", "Getting messages for chatId: $chatId")
        return repository.getMessagesFlow(chatId)
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            Log.d("ChatsViewModel", "Deleting chat with chatId: $chatId, userId: $currentUserId")
            try {
                val deleteMessage = Message(
                    chatId = chatId,
                    senderId = currentUserId,
                    text = "Chat deleted",
                    type = "system",
                    timestamp = Timestamp.now()
                )
                repository.sendMessage(deleteMessage)
                repository.getChatReference(chatId).delete().await()
                Log.d("ChatsViewModel", "Chat deleted successfully for chatId: $chatId")
            } catch (e: Exception) {
                _errorMessage.emit("Delete failed: ${e.message}")
                Log.e("ChatsViewModel", "Failed to delete chat for chatId: $chatId: ${e.message}")
            }
        }
    }

    fun sendImageMessage(chatId: String, imageUri: Uri, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.emit("User not authenticated")
                _isLoading.value = false
                Log.e("ChatsViewModel", "User not authenticated for image upload, chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Starting image upload for chatId: $chatId, userId: $currentUserId, imageUri: $imageUri, expecting auto-created folder: chatMedia")
            _isLoading.value = true
            val messageId = UUID.randomUUID().toString() // Generate message ID here

            // Verify chat participation before upload
            if (!verifyChatParticipation(chatId, currentUserId)) {
                _errorMessage.emit("User is not a participant in chat: $chatId")
                _isLoading.value = false
                Log.e("ChatsViewModel", "User $currentUserId is not a participant in chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Verified user participation, proceeding with image upload for chatId: $chatId, expecting auto-created folder: chatMedia")
            when (val uploadResult = repository.uploadChatImage(chatId, messageId, imageUri)) { // Pass messageId
                is Result.Success -> {
                    Log.d("ChatsViewModel", "Image uploaded successfully, URL: ${uploadResult.data}, for chatId: $chatId, messageId: $messageId, to auto-created folder: chatMedia")
                    val message = Message(
                        chatId = chatId,
                        senderId = currentUserId,
                        messageId = messageId, // Add messageId to the message object
                        imageUrl = uploadResult.data,
                        type = "image",
                        timestamp = Timestamp.now()
                    )
                    _messages.value = (_messages.value + message).sortedBy { it.timestamp?.seconds ?: 0 }
                    when (val sendResult = repository.sendMessage(message)) {
                        is Result.Success -> {
                            updateLastMessage(chatId, "[image]", currentUserId)
                            _isLoading.value = false
                            Log.d("ChatsViewModel", "Image message sent successfully for chatId: $chatId, messageId: $messageId, Firestore path: chats/$chatId/messages/$messageId")
                        }
                        is Result.Failure -> {
                            _errorMessage.emit("Failed to send image: ${sendResult.exception.message}")
                            _messages.value = _messages.value.filter { it != message }
                            _isLoading.value = false
                            Log.e("ChatsViewModel", "Failed to send image message for chatId: $chatId, messageId: $messageId: ${sendResult.exception.message}")
                        }
                        is Result.Loading -> {
                            Log.d("ChatsViewModel", "Sending image message in progress for chatId: $chatId, messageId: $messageId")
                        }
                    }
                }
                is Result.Failure -> {
                    _errorMessage.emit("Image upload failed: ${uploadResult.exception.message}")
                    _isLoading.value = false
                    Log.e("ChatsViewModel", "Image upload failed for chatId: $chatId, messageId: $messageId: ${uploadResult.exception.message}")
                }
                is Result.Loading -> {
                    Log.d("ChatsViewModel", "Uploading image in progress for chatId: $chatId, messageId: $messageId")
                }
            }
        }
    }

    fun sendVideoMessage(chatId: String, videoUri: Uri, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.emit("User not authenticated")
                _isLoading.value = false
                Log.e("ChatsViewModel", "User not authenticated for video upload, chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Starting video upload for chatId: $chatId, userId: $currentUserId, videoUri: $videoUri, expecting auto-created folder: chatMedia")
            _isLoading.value = true
            val messageId = UUID.randomUUID().toString() // Generate message ID here

            // Verify chat participation before upload
            if (!verifyChatParticipation(chatId, currentUserId)) {
                _errorMessage.emit("User is not a participant in chat: $chatId")
                _isLoading.value = false
                Log.e("ChatsViewModel", "User $currentUserId is not a participant in chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Verified user participation, proceeding with video upload for chatId: $chatId, expecting auto-created folder: chatMedia")
            when (val uploadResult = repository.uploadChatVideo(chatId, messageId, videoUri)) { // Pass messageId
                is Result.Success -> {
                    Log.d("ChatsViewModel", "Video uploaded successfully, URL: ${uploadResult.data}, for chatId: $chatId, messageId: $messageId, to auto-created folder: chatMedia")
                    val message = Message(
                        chatId = chatId,
                        senderId = currentUserId,
                        messageId = messageId, // Add messageId to the message object
                        videoUrl = uploadResult.data,
                        type = "video",
                        timestamp = Timestamp.now()
                    )
                    _messages.value = (_messages.value + message).sortedBy { it.timestamp?.seconds ?: 0 }
                    when (val sendResult = repository.sendMessage(message)) {
                        is Result.Success -> {
                            updateLastMessage(chatId, "[video]", currentUserId)
                            _isLoading.value = false
                            Log.d("ChatsViewModel", "Video message sent successfully for chatId: $chatId, messageId: $messageId, Firestore path: chats/$chatId/messages/$messageId")
                        }
                        is Result.Failure -> {
                            _errorMessage.emit("Failed to send video: ${sendResult.exception.message}")
                            _messages.value = _messages.value.filter { it != message }
                            _isLoading.value = false
                            Log.e("ChatsViewModel", "Failed to send video message for chatId: $chatId, messageId: $messageId: ${sendResult.exception.message}")
                        }
                        is Result.Loading -> {
                            Log.d("ChatsViewModel", "Sending video message in progress for chatId: $chatId, messageId: $messageId")
                        }
                    }
                }
                is Result.Failure -> {
                    _errorMessage.emit("Video upload failed: ${uploadResult.exception.message}")
                    _isLoading.value = false
                    Log.e("ChatsViewModel", "Video upload failed for chatId: $chatId, messageId: $messageId: ${uploadResult.exception.message}")
                }
                is Result.Loading -> {
                    Log.d("ChatsViewModel", "Uploading video in progress for chatId: $chatId, messageId: $messageId")
                }
            }
        }
    }

    fun markMessagesAsRead(chatId: String, currentUserId: String) {
        viewModelScope.launch(dispatchers.io) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.emit("User not authenticated")
                _isLoading.value = false
                Log.e("ChatsViewModel", "User not authenticated for marking messages as read, chatId: $chatId")
                return@launch
            }

            Log.d("ChatsViewModel", "Marking messages as read for chatId: $chatId, userId: $currentUserId")
            _isLoading.value = true
            when (val result = repository.markMessagesAsRead(chatId, currentUserId)) {
                is Result.Success -> {
                    _isLoading.value = false
                    Log.d("ChatsViewModel", "Messages marked as read successfully for chatId: $chatId")
                }
                is Result.Failure -> {
                    _errorMessage.emit("Mark read failed: ${result.exception.message}")
                    _isLoading.value = false
                    Log.e("ChatsViewModel", "Failed to mark messages as read for chatId: $chatId: ${result.exception.message}")
                }
                is Result.Loading -> {
                    Log.d("ChatsViewModel", "Marking messages as read in progress for chatId: $chatId")
                }
            }
        }
    }

    private suspend fun updateLastMessage(chatId: String, lastMessage: String, currentUserId: String) {
        try {
            Log.d("ChatsViewModel", "Updating last message for chatId: $chatId, message: $lastMessage, userId: $currentUserId")
            repository.getChatReference(chatId).update(
                mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "lastMessageSender" to currentUserId
                )
            ).await()
            Log.d("ChatsViewModel", "Last message updated successfully for chatId: $chatId")
        } catch (e: Exception) {
            Log.e("ChatsViewModel", "Error updating last message for chatId: $chatId: ${e.message}")
            _errorMessage.emit("Failed to update last message: ${e.message}")
        }
    }

    private suspend fun verifyChatParticipation(chatId: String, userId: String): Boolean {
        return try {
            val chatDoc = repository.getChatReference(chatId).get().await()
            if (!chatDoc.exists()) {
                Log.e("ChatsViewModel", "Chat document not found for chatId: $chatId")
                return false
            }
            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val isParticipant = userId in participants
            Log.d("ChatsViewModel", "User $userId is${if (isParticipant) "" else " not"} a participant in chatId: $chatId, participants: $participants")
            isParticipant
        } catch (e: Exception) {
            Log.e("ChatsViewModel", "Error verifying chat participation for chatId: $chatId, userId: $userId: ${e.message}", e)
            false
        }
    }
}