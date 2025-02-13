package com.example.vibesshared.ui.ui.data

data class UserProfile(
    val userId: String = "",
    val userName: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val email: String = "",
    val dob: String = "",
    val profilePictureUrl: String? = null // Keep it nullable
)