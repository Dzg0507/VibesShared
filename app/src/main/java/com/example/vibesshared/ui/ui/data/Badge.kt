package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp
import java.io.Serializable

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val acquiredDate: Timestamp? = null // Change to Timestamp for Firestore compatibility
) : Serializable