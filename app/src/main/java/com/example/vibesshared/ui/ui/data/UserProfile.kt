package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val userName: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val email: String = "",
    val dob: String = "",
    val profilePictureUrl: String? = null, // Keep it nullable
    val badges: List<String> = emptyList(), // Add badges field
    val badgeDates: Map<String, Timestamp>? = emptyMap(),
    val postCount: Int = 0,
    val friends: List<String> = emptyList()
)