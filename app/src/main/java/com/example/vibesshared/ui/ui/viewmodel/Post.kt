package com.example.vibesshared.ui.ui.viewmodel

data class Post(
    val postId: String = "",
    val postImage : String? = null,
    val postText: String = "",
    val postVideo: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val caption: String? = null,
    val userName: String = "",
    val userProfilePicture: String? = null,
    val image: String? = null,
)
