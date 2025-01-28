package com.example.vibesshared.ui.ui.components

data class UserProfile(
    val userId: String = "",
    val userName: String = "",  // Added this line
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePicture: String? = null,
    val experience: String? = null,
    val favoriteLanguage: String? = null,
    val specialty: String? = null,
    val currentProject: String? = null,
    val learning: String? = null
)
