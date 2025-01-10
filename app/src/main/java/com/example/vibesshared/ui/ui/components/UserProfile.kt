package com.example.vibesshared.ui.ui.components

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val profilePictureUri: String? = null
)