package com.example.vibesshared.ui.ui.components

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
@Parcelize
data class UserProfile(
    val userId: String = "", // Firebase Authentication UID
    val firstName: String = "",
    val lastName: String = "",
    val email: String? = null,
    val bio: String? = null,
    val profilePictureUri: String? = null
) : Parcelable