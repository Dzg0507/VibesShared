package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    @DocumentId val postId: String = "",
    val userId: String = "",
    val postText: String = "",
    val postImage: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val userProfilePicture: String? = null, // Ensure this field exists if you're using it
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val postVideo: String = "",
    val profilePictureUrl: String = "",
    val userName: String = "",
    val commentCount: Int = 0
)