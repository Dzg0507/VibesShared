package com.example.vibesshared.ui.ui.repository

import android.util.Log
import com.example.vibesshared.ui.ui.data.Badge
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    val firestore: FirebaseFirestore,
    val dispatchers: DispatcherProvider
) {

    private val badgesCollection = firestore.collection("badges")

    suspend fun addBadge(badge: Badge): Result<Unit> = withContext(dispatchers.io) {
        try {
            badgesCollection.document(badge.badgeId).set(badge).await()
            Log.d("BadgeRepository", "Added badge ${badge.badgeId} with acquiredDate ${badge.acquiredDate}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BadgeRepository", "Error adding badge: ${badge.badgeId}", e)
            Result.Failure(e)
        }
    }

    suspend fun getBadge(badgeId: String): Badge? = withContext(dispatchers.io) {
        try {
            val doc = badgesCollection.document(badgeId).get().await()
            doc.toObject(Badge::class.java)
        } catch (e: Exception) {
            Log.e("BadgeRepository", "Error getting badge: $badgeId", e)
            null
        }
    }

    suspend fun getAllBadges(): List<Badge> = withContext(dispatchers.io) {
        try {
            val snapshot = badgesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Badge::class.java) }
        } catch (e: Exception) {
            Log.e("BadgeRepository", "Error getting all badges", e)
            emptyList()
        }
    }
}