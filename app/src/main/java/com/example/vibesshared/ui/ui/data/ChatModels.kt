package com.example.vibesshared.ui.ui.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Chat(
    val id: String = "",
    val userIds: List<String> = emptyList(),
    var lastMessage: String = "",
    val chatId: String = "",
    @ServerTimestamp var lastMessageTimestamp: Date? = null,
    var otherUserName: String? = null,
    val deletedBy: List<String> = emptyList(),
    var statusMessage: String? = null, // Add this field
    var unreadCount: Int = 0, // Add this field
    val participants: List<String> = emptyList() // Add this line
)





data class Message(
    val id: String = "",
    val userIds: List<String> = emptyList(),
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val type: String = "text",
    val imageUrl: String? = null, // Add this field for image messages
    var read: Boolean = false // Add this for marking messages as read
)