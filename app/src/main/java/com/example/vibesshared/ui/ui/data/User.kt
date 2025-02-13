package com.example.vibesshared.ui.ui.data

data class User(
    val userId: String = "",
    val userName: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val profilePictureUrl: String? = null, // Make profilePictureUrl nullable (String?)
    val email: String = "",
    val dob: String = "",
)

data class FirestoreUser(
    val userId: String = "",
    val userName: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val profilePictureUrl: String? = "",
    val lastPost: String? = null,

    val userPost: String = "", // Or 'post' if you renamed it
    val isOnline: Boolean = false,
)


