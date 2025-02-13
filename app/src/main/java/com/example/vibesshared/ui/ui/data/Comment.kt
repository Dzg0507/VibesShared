package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    @DocumentId val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now(),
    val userFirstName: String? = "", // Add user's first name
    val userLastName: String? = ""// Add user's last name
)
