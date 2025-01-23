
/*
package com.example.vibesshared.ui.ui.data

import android.net.Uri
import android.util.Log
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.data.Chat
import com.example.vibesshared.ui.ui.data.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DataManager {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun sendMessage(chatId: String, text: String, currentUserId: String) {
        Log.d("DataManager", "sendMessage called with chatId: $chatId, text: $text, currentUserId: $currentUserId")
        try {
            val message = hashMapOf(
                "senderId" to currentUserId,
                "text" to text,
                "timestamp" to FieldValue.serverTimestamp(),
                "type" to "text"
            )

            db.collection("chats").document(chatId).collection("messages").add(message).await()

            // Update last message and timestamp in the chat document
            db.collection("chats").document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("DataManager", "Error sending message", e)
        }
    }

    fun loadMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        Log.d("DataManager", "loadMessages called for chatId: $chatId")
        val listener = try {
            db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DataManager", "Error loading messages", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull {
                            Log.d("DataManager", "Message document data: ${it.data}")
                            it.toObject(Message::class.java)
                        }
                        trySend(messages)
                        Log.d("DataManager", "Messages loaded: ${messages.size}")
                    } else {
                        trySend(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e("DataManager", "Error loading messages", e)
            null
        }

        awaitClose {
            Log.d("DataManager", "Removing message listener for chatId: $chatId")
            listener?.remove()
        }
    }

    fun loadChats(currentUserId: String): Flow<List<Chat>> = callbackFlow {
        Log.d("DataManager", "loadChats called for user: $currentUserId")
        val listener = try {
            db.collection("chats")
                .whereArrayContains("userIds", currentUserId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DataManager", "Error loading chats", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val chats = snapshot.documents.mapNotNull { document ->
                            Log.d("DataManager", "Chat document data: ${document.data}")
                            val participants = document.get("userIds") as List<String>
                            val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""
                            val deletedBy = document.get("deletedBy") as? List<String> ?: emptyList()

                            if (otherUserId.isNotEmpty() && !deletedBy.contains(currentUserId)) {
                                // Fetch the UserProfile of the other user
                                val otherUserProfile = fetchUserProfile(otherUserId)

                                Chat(
                                    id = document.id,
                                    userIds = listOf(otherUserId),
                                    lastMessage = document.getString("lastMessage") ?: "",
                                    lastMessageTimestamp = document.getTimestamp("lastMessageTimestamp")?.toDate(),
                                    otherUserName = otherUserProfile?.let { "${it.firstName} ${it.lastName}" }
                                        ?: "Unknown User",
                                    deletedBy = deletedBy
                                )
                            } else {
                                null
                            }
                        }
                        trySend(chats)
                        Log.d("DataManager", "Chats loaded: ${chats.size}")
                    } else {
                        trySend(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e("DataManager", "Error loading chats", e)
            null
        }
        awaitClose {
            Log.d("DataManager", "Removing chat listener for user: $currentUserId")
            listener?.remove()
        }
    }

    private suspend fun fetchUserProfile(userId: String): UserProfile? {
        Log.d("DataManager", "Fetching user profile for $userId")
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = userRef.get().await()
            snapshot.toObject(UserProfile::class.java)?.copy(userId = userId)
        } catch (e: Exception) {
            Log.e("DataManager", "Error fetching user profile for $userId", e)
            null
        }
    }

    suspend fun createNewChat(userIds: List<String>): Result<String> {
        Log.d("DataManager", "createNewChat called with userIds: $userIds")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("DataManager", "Current user ID is null")
            return Result.Error(Exception("Current user ID is null"))
        }

        return try {
            // Create a new chat document with a unique ID
            val newChatRef = db.collection("chats").document()
            val chatId = newChatRef.id

            // Add the current user and the specified user IDs to the userIds list
            val participants = listOf(currentUserId) + userIds

            // Create the initial chat data
            val chatData = hashMapOf(
                "userIds" to participants,
                "lastMessage" to "", // You can set an initial message here if needed
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "deletedBy" to listOf<String>() // Initialize with an empty list
            )

            // Set the data in the new chat document
            newChatRef.set(chatData).await()

            // Return the chatId as a success result
            Log.d("DataManager", "New chat created with chatId: $chatId")
            Result.Success(chatId)
        } catch (e: Exception) {
            Log.e("DataManager", "Error creating new chat", e)
            Result.Error(e)
        }
    }

    fun fetchUsers(): Flow<List<UserProfile>> = callbackFlow {
        Log.d("DataManager", "fetchUsers called")
        val listener = try {
            db.collection("users")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DataManager", "Error fetching users", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val userProfiles = snapshot.documents.mapNotNull { document ->
                            Log.d("DataManager", "Document ID: ${document.id}")
                            Log.d("DataManager", "Document data: ${document.data}")

                            document.toObject(UserProfile::class.java)?.copy(userId = document.id)
                        }
                        trySend(userProfiles)
                        Log.d("DataManager", "User profiles fetched: ${userProfiles.size}")
                    } else {
                        trySend(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e("DataManager", "Error fetching users", e)
            null
        }
        awaitClose {
            Log.d("DataManager", "Removing user listener")
            listener?.remove()
        }
    }

    fun searchUsers(query: String): Flow<List<UserProfile>> = callbackFlow {
        Log.d("DataManager", "searchUsers called with query: $query")
        val listener = try {
            db.collection("users")
                .orderBy("firstName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DataManager", "Error searching users", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val userProfiles = snapshot.documents.mapNotNull { document ->
                            Log.d("DataManager", "Search Result - Document data: ${document.data}")
                            document.toObject(UserProfile::class.java)?.copy(userId = document.id)
                        }
                        trySend(userProfiles)
                    } else {
                        trySend(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e("DataManager", "Error searching users", e)
            null
        }
        awaitClose {
            Log.d("DataManager", "Removing search users listener")
            listener?.remove()
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        Log.d("DataManager", "deleteChat called for chatId: $chatId")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("DataManager", "Current user ID is null")
            return Result.Error(Exception("Current user ID is null"))
        }

        return try {
            val chatRef = db.collection("chats").document(chatId)

            // Fetch the chat document to get the participants
            val chatSnapshot = chatRef.get().await()
            val chat = chatSnapshot.toObject(Chat::class.java)
            if (chat == null) {
                Log.e("DataManager", "Chat document not found: $chatId")
                return Result.Error(Exception("Chat document not found"))
            }

            // Update the 'deletedBy' field
            chatRef.update(mapOf(
                "deletedBy" to FieldValue.arrayUnion(currentUserId)
            )).await()

            Log.d("DataManager", "Chat marked as deleted for user: $currentUserId in chat: $chatId")

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error deleting chat: $chatId", e)
            Result.Error(e)
        }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUserId: String): Result<Unit> {
        Log.d("DataManager", "markMessagesAsRead called for chatId: $chatId")
        return try {
            val messagesRef = db.collection("chats").document(chatId).collection("messages")
            val unreadMessages = messagesRef
                .whereNotEqualTo("senderId", currentUserId)
                .whereEqualTo("read", false)
                .get()
                .await()

            for (doc in unreadMessages.documents) {
                doc.reference.update("read", true).await()
                Log.d("DataManager", "Message marked as read: ${doc.id}")
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error marking messages as read for chatId: $chatId", e)
            Result.Error(e)
        }
    }

    suspend fun sendImageMessage(chatId: String, imageUri: Uri, currentUserId: String): Result<Unit> {
        Log.d("DataManager", "sendImageMessage called with chatId: $chatId, imageUri: $imageUri")
        return try {
            // Upload image to Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val imageFileName = "images/$chatId/${UUID.randomUUID()}"
            val imageRef = storageRef.child(imageFileName)
            val uploadTask = imageRef.putFile(imageUri)

            // Get the download URL
            val downloadUrl = uploadTask.await().storage.downloadUrl.await().toString()

            // Create a message with the image URL
            val message = hashMapOf(
                "senderId" to currentUserId,
                "text" to "", // You can add text to image messages if needed
                "imageUrl" to downloadUrl,
                "timestamp" to FieldValue.serverTimestamp(),
                "type" to "image" // Message type to indicate an image message
            )

            // Add the message to the 'messages' subcollection
            db.collection("chats").document(chatId).collection("messages").add(message).await()

            // Update last message and timestamp in the chat document
            db.collection("chats").document(chatId).update(
                mapOf(
                    "lastMessage" to "[Image]",
                    "lastMessageTimestamp" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error sending image message", e)
            Result.Error(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        Log.d("DataManager", "login called with email: $email")
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error logging in", e)
            Result.Error(e)
        }
    }

    suspend fun createAccount(email: String, password: String, userProfile: UserProfile): Result<Unit> {
        Log.d("DataManager", "createAccount called with email: $email")
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId != null) {
                // Set the userId in the UserProfile
                val updatedUserProfile = userProfile.copy(userId = userId)

                // Use Firestore to store the user profile
                db.collection("users").document(userId).set(updatedUserProfile).await()
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error creating account", e)
            Result.Error(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        Log.d("DataManager", "logout called")
        return try {
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DataManager", "Error logging out", e)
            Result.Error(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserProfile?> {
        Log.d("DataManager", "getCurrentUser called")
        val userId = auth.currentUser?.uid
        if (userId != null) {
            return try {
                val userRef = db.collection("users").document(userId)
                val snapshot = userRef.get().await()
                if (snapshot.exists()) {
                    Result.Success(snapshot.toObject(UserProfile::class.java)?.copy(userId = userId))
                } else {
                    Result.Error(Exception("User document not found"))
                }
            } catch (e: Exception) {
                Log.e("DataManager", "Error getting current user", e)
                Result.Error(e)
            }
        } else {
            Log.d("DataManager", "No current user found")
            return Result.Success(null)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        Log.d("DataManager", "getUserProfile called for userId: $userId")
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = userRef.get().await()
            if (snapshot.exists()) {
                Result.Success(snapshot.toObject(UserProfile::class.java)?.copy(userId = userId))
            } else {
                Result.Error(Exception("User document not found"))
            }
        } catch (e: Exception) {
            Log.e("DataManager", "Error getting user profile for userId: $userId", e)
            Result.Error(e)
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

*/