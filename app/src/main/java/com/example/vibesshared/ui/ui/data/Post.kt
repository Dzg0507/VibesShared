package com.example.vibesshared.ui.ui.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    @DocumentId val postId: String = "",
    val userId: String = "",
    val postText: String? = null,
    @PropertyName("postImages")
    private val internalPostImages: List<String?> = emptyList(),
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val userProfilePicture: String? = null,
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val postVideo: String? = null,
    val profilePictureUrl: String? = null,
    val userName: String? = null,
    val commentCount: Int = 0,
    val pollData: Map<String, Any?>? = null,
    val hasUserVoted: Boolean = false,
    @PropertyName("postImage")
    private val legacyPostImage: String? = null
) {
    // Getter for postImages that prioritizes internalPostImages but falls back to legacyPostImage
    val postImages: List<String?>
        get() = if (internalPostImages.isNotEmpty()) internalPostImages else listOfNotNull(legacyPostImage)

    fun getPollOptions(): List<String> {
        return (pollData?.get("options") as? List<String>) ?: emptyList()
    }

    fun getPollVotes(): Map<String, Int> {
        val rawVotes = pollData?.get("votes") as? Map<String, *>
        return rawVotes?.mapValues { entry ->
            when (val value = entry.value) {
                is Int -> value
                is Long -> value.toInt() // Convert Long to Int
                else -> 0 // Default to 0 for unexpected types
            }
        } ?: emptyMap()
    }

    fun getPollDescription(): String {
        return (pollData?.get("description") as? String) ?: ""
    }
}