package com.example.vibesshared.ui.ui.data

import com.google.firebase.firestore.PropertyName


data class FriendRequest(
    val requestId: String = "", // Add a unique ID for the request
    val senderId: String = "",
    val receiverId: String = "",
    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "", // "pending", "accepted", "rejected"
    val participants: List<String> = listOf() // Add this line
)

