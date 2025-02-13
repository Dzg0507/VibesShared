package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Timestamp? = null // Add missing field and annotation
)

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null, // Add @ServerTimestamp annotation
    val type: String = "text"
) {
    // Helper function for creating Firestore document
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "messageId" to messageId,
            "chatId" to chatId,
            "senderId" to senderId,
            "text" to text,
            "imageUrl" to (imageUrl ?: ""),
            "timestamp" to FieldValue.serverTimestamp(), // Keep FieldValue for writes
            "type" to type
        )
    }
}