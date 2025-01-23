package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.data.Chat
import com.example.vibesshared.ui.ui.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.UUID

class ChatsViewModel(private val context: Context) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _navigateToMessaging = MutableSharedFlow<String>()
    val navigateToMessaging = _navigateToMessaging.asSharedFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users = _users.asStateFlow()

    private val _refreshChats = MutableStateFlow(0)
    val refreshChats = _refreshChats.asStateFlow()

    fun sendMessage(chatId: String, text: String, currentUserId: String) {
        Log.d("ChatsViewModel", "sendMessage called with chatId: $chatId, text: $text, currentUserId: $currentUserId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User: $currentUser")
        viewModelScope.launch {
            try {
                val message = hashMapOf(
                    "senderId" to currentUserId,
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "type" to "text"
                )
                Log.d("ChatsViewModel", "Sending message: $message")

                db.collection("chats").document(chatId).collection("messages").add(message).await()

                db.collection("chats").document(chatId).update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTimestamp" to FieldValue.serverTimestamp()
                    )
                ).await()
                _refreshChats.value += 1
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error sending message", e)
            }
        }
    }

    fun loadMessages(chatId: String) {
        Log.d("ChatsViewModel", "loadMessages called for chatId: $chatId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ChatsViewModel", "Error loading messages: ${e.message}", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val messages = snapshot.documents.mapNotNull {
                                Log.d("ChatsViewModel", "Message document data: ${it.data}")
                                it.toObject(Message::class.java)
                            }
                            _messages.value = messages
                            Log.d("ChatsViewModel", "Messages loaded: ${messages.size}")
                        } else {
                            Log.d("ChatsViewModel", "Snapshot is null")
                            _messages.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error loading messages", e)
            }
        }
    }

    fun loadChats(currentUserId: String) {
        Log.d("ChatsViewModel", "loadChats called for user: $currentUserId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                db.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ChatsViewModel", "Error loading chats: ${e.message}", e)
                            _chats.value = emptyList()
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            viewModelScope.launch {
                                val chats = snapshot.documents.mapNotNull { document ->
                                    Log.d("ChatsViewModel", "Chat document data: ${document.data}")
                                    val participants = document.get("participants") as List<String>
                                    val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""
                                    val deletedBy = document.get("deletedBy") as? List<String> ?: emptyList()

                                    if (otherUserId.isNotEmpty() && !deletedBy.contains(currentUserId)) {
                                        val otherUserProfile = fetchUserProfile(otherUserId)
                                        Chat(
                                            id = document.id,
                                            userIds = listOf(otherUserId),
                                            lastMessage = document.getString("lastMessage") ?: "",
                                            lastMessageTimestamp = document.getTimestamp("lastMessageTimestamp")?.toDate(),
                                            otherUserName = otherUserProfile?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown User",
                                            deletedBy = deletedBy
                                        )
                                    } else {
                                        null
                                    }
                                }
                                _chats.value = chats
                                Log.d("ChatsViewModel", "Chats loaded: ${chats.size}")
                            }
                        } else {
                            Log.d("ChatsViewModel", "Snapshot is null")
                            _chats.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error loading chats", e)
                _chats.value = emptyList()
            }
        }
    }

    private suspend fun fetchUserProfile(userId: String): UserProfile? {
        Log.d("ChatsViewModel", "Fetching user profile for $userId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        try {
            val userRef = db.collection("users").document(userId)
            val snapshot = userRef.get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = userId)
            Log.d("ChatsViewModel", "Fetched profile for user $userId: ${userProfile?.firstName} ${userProfile?.lastName}")
            return userProfile
        } catch (e: Exception) {
            Log.e("ChatsViewModel", "Error fetching user profile for $userId", e)
            return null
        }
    }

    fun createNewChat(userIds: List<String>) {
        Log.d("ChatsViewModel", "createNewChat called with userIds: $userIds")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User: $currentUser")
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("ChatsViewModel", "Current user ID is null")
                return@launch
            }

            try {
                val newChatRef = db.collection("chats").document()
                val chatId = newChatRef.id
                val participants = listOf(currentUserId) + userIds
                val chatData = hashMapOf(
                    "participants" to participants,
                    "lastMessage" to "",
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "deletedBy" to listOf<String>()
                )
                Log.d("ChatsViewModel", "Creating new chat with data: $chatData")

                newChatRef.set(chatData).await()
                Log.d("ChatsViewModel", "Navigating to Messaging screen with chatId: $chatId")
                _navigateToMessaging.emit(chatId)

            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error creating new chat", e)
            }
        }
    }

    fun fetchUsers() {
        Log.d("ChatsViewModel", "fetchUsers called")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                val userDocuments = db.collection("users").get().await()
                Log.d("ChatsViewModel", "Fetched user documents: ${userDocuments.size()}")

                val userProfiles = userDocuments.documents.mapNotNull { document ->
                    Log.d("ChatsViewModel", "Document ID: ${document.id}")
                    Log.d("ChatsViewModel", "Document data: ${document.data}")

                    val userProfile = document.toObject(UserProfile::class.java)?.copy(userId = document.id)
                    if (userProfile != null) {
                        Log.d("ChatsViewModel", "Successfully mapped user: ${userProfile.firstName} ${userProfile.lastName}")
                    } else {
                        Log.d("ChatsViewModel", "Failed to map document to UserProfile")
                    }

                    userProfile
                }
                _users.value = userProfiles
                Log.d("ChatsViewModel", "User profiles fetched: ${userProfiles.size}")
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error fetching users", e)
                _users.value = emptyList()
            }
        }
    }

    fun searchUsers(query: String) {
        Log.d("ChatsViewModel", "searchUsers called with query: $query")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                val userDocuments = if (query.isEmpty()) {
                    db.collection("users").get().await()
                } else {
                    db.collection("users")
                        .whereGreaterThanOrEqualTo("firstName", query)
                        .whereLessThanOrEqualTo("firstName", query + "\uf8ff")
                        .get()
                        .await()
                }

                val userProfiles = userDocuments.documents.mapNotNull { document ->
                    Log.d("ChatsViewModel", "Search Result - Document data: ${document.data}")
                    document.toObject(UserProfile::class.java)?.copy(userId = document.id)
                }
                _users.value = userProfiles
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error searching users", e)
                _users.value = emptyList()
            }
        }
    }

    fun updateMessages(newMessages: List<Message>) {
        _messages.value = newMessages
    }

    fun deleteChat(chatId: String) {
        Log.d("ChatsViewModel", "deleteChat called for chatId: $chatId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    Log.e("ChatsViewModel", "Current user ID is null")
                    return@launch
                }

                val chatRef = db.collection("chats").document(chatId)
                val chatSnapshot = chatRef.get().await()
                val chat = chatSnapshot.toObject(Chat::class.java)
                if (chat == null) {
                    Log.e("ChatsViewModel", "Chat document not found: $chatId")
                    return@launch
                }

                val otherUserId = chat.userIds.firstOrNull { it != currentUserId }

                val deleteMessage = hashMapOf(
                    "senderId" to currentUserId,
                    "text" to "This chat has been deleted by the other user.",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "type" to "system"
                )
                Log.d("ChatsViewModel", "Deleting chat: $chatId, sending delete message: $deleteMessage")

                db.collection("chats").document(chatId).collection("messages").add(deleteMessage).await()
                Log.d("ChatsViewModel", "Delete message sent in chat: $chatId")

                chatRef.update(mapOf(
                    "deletedBy" to FieldValue.arrayUnion(currentUserId),
                    "lastMessage" to "This chat has been deleted by the other user.",
                    "lastMessageTimestamp" to FieldValue.serverTimestamp()
                )).await()

                Log.d("ChatsViewModel", "Chat marked as deleted for user: $currentUserId in chat: $chatId")

                _refreshChats.value += 1
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error deleting chat: $chatId", e)
            }
        }
    }

    fun sendImageMessage(chatId: String, imageUri: Uri, currentUserId: String) {
        Log.d("ChatsViewModel", "sendImageMessage called with chatId: $chatId, imageUri: $imageUri")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User: $currentUser")
        viewModelScope.launch {
            try {
                Log.d("ChatsViewModel", "Image URI: $imageUri")
                val storageRef = FirebaseStorage.getInstance().reference
                val imageFileName = "images/$chatId/${UUID.randomUUID()}"
                val imageRef = storageRef.child(imageFileName)

                // Access image data using ContentResolver
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw IOException("Failed to open input stream for $imageUri")
                Log.d("ChatsViewModel", "InputStream opened successfully")

                // Upload image to Firebase Storage
                Log.d("ChatsViewModel", "Uploading image to: ${imageRef.path}")
                val uploadTask = imageRef.putStream(inputStream)

                try {
                    val uploadResult = uploadTask.await()
                    val downloadUrl = uploadResult.storage.downloadUrl.await().toString()
                    Log.d("ChatsViewModel", "Image uploaded, download URL: $downloadUrl")

                    // Create a new message object with the download URL
                    val message = hashMapOf(
                        "senderId" to currentUserId,
                        "imageUrl" to downloadUrl, // Add the download URL to the message
                        "timestamp" to FieldValue.serverTimestamp(),
                        "type" to "image" // Set the message type to "image"
                    )

                    // Add the message to Firestore
                    db.collection("chats").document(chatId).collection("messages").add(message).await()

                    // Update the last message in the chat
                    db.collection("chats").document(chatId).update(
                        mapOf(
                            "lastMessage" to "[image]", // You might want to display a different message for images
                            "lastMessageTimestamp" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    _refreshChats.value += 1
                } catch (e: Exception) {
                    Log.e("ChatsViewModel", "Error during upload or download URL retrieval", e)
                    // Handle the exception appropriately (e.g., show an error message to the user)
                }
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error sending image message", e)
                // Handle the exception appropriately
            }
        }
    }

    fun markMessagesAsRead(chatId: String, currentUserId: String) {
        Log.d("ChatsViewModel", "markMessagesAsRead called for chatId: $chatId")
        val currentUser = auth.currentUser
        Log.d("ChatsViewModel", "Current User before query: $currentUser")
        viewModelScope.launch {
            try {
                val messagesRef = db.collection("chats").document(chatId).collection("messages")
                val unreadMessages = messagesRef
                    .whereNotEqualTo("senderId", currentUserId)
                    .whereEqualTo("read", false)
                    .get()
                    .await()

                for (doc in unreadMessages.documents) {
                    doc.reference.update("read", true).await()
                    Log.d("ChatsViewModel", "Message marked as read: ${doc.id}")
                }
            } catch (e: Exception) {
                Log.e("ChatsViewModel", "Error marking messages as read for chatId: $chatId", e)
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
                        return ChatsViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}