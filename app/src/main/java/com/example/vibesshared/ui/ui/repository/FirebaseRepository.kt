package com.example.vibesshared.ui.ui.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vibesshared.ui.ui.data.*
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.utils.Result
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val dispatchers: DispatcherProvider,
    private val storageRepository: StorageRepository // Inject StorageRepository
) {
    private val usersCollection = firestore.collection("users")
    private val friendRequestsCollection = firestore.collection("friendRequests")
    private val chatsCollection = firestore.collection("chats")
    private val postsCollection = firestore.collection("posts")

    // region Authentication
    fun getCurrentUser() = auth.currentUser

    suspend fun signIn(email: String, pass: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun updateUserBadgesWithDate(userId: String, badgeData: Map<String, Any>): Result<Unit> = withContext(dispatchers.io) {
        try {
            Log.d("FirebaseRepository", "Updating badges and dates for user $userId: $badgeData")
            firestore.collection("users").document(userId).update(badgeData).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating user badges with date: $userId", e)
            Result.Failure(e)
        }
    }

    suspend fun updateUserBadges(userId: String, badges: List<String>): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                usersCollection.document(userId).update("badges", badges).await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    suspend fun signUp(email: String, pass: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun signOut() = auth.signOut()
    // endregion

    // region User Profile
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                usersCollection.document(userProfile.userId)
                    .set(userProfile, SetOptions.merge())
                    .await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    suspend fun updateProfilePicture(userId: String, imageUri: Uri): Result<String> =
        withContext(dispatchers.io) {
            try {
                val contentResolver = context.contentResolver
                val fileExtension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(contentResolver.getType(imageUri))
                    ?: "jpg"

                val imageRef = storage.reference.child(userId).child("profilePictures").child("${UUID.randomUUID()}.$fileExtension")

                val uploadTask = imageRef.putFile(imageUri)
                val snapshot = uploadTask.await()
                val downloadUrl = snapshot.storage.downloadUrl.await().toString()

                usersCollection.document(userId)
                    .update("profilePictureUrl", downloadUrl)

                Result.Success(downloadUrl)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Error uploading profile picture", e)
                Result.Failure(e)
            }
        }

    fun getUserFlow(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }.flowOn(dispatchers.io)

    private val userProfileCache = mutableMapOf<String, UserProfile>()

    // Updated getUserProfile with caching
    suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(dispatchers.io) {
        try {
            // Check cache first
            userProfileCache[userId]?.let {
                return@withContext Result.Success(it)
            }

            val snapshot = firestore.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                val userProfile = snapshot.toObject(UserProfile::class.java) ?: throw Exception("User profile not found")
                userProfileCache[userId] = userProfile // Cache the result
                Result.Success(userProfile)
            } else {
                Result.Failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getFriends(userId: String): Result<List<UserProfile>> = withContext(dispatchers.io) {
        try {
            val currentUserDoc = usersCollection.document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            val friendIds = currentUserDoc["friends"] as? List<String> ?: listOf()

            val friends = friendIds.mapNotNull { friendId ->
                val userResult = getUserProfile(friendId)
                if (userResult is Result.Success) {
                    userResult.data
                } else {
                    null
                }
            }
            Result.Success(friends)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun sendFriendRequest(receiverId: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            val senderId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            if (senderId == receiverId) throw Exception("Cannot send request to yourself")

            val existingRequest = friendRequestsCollection
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get().await().documents.toList()

            if (existingRequest.isNotEmpty()) {
                throw Exception("Friend request already exists")
            }

            val requestId = friendRequestsCollection.document().id
            val request = FriendRequest(
                requestId = requestId,
                senderId = senderId,
                receiverId = receiverId,
                status = "pending",
                participants = listOf(senderId, receiverId)
            )

            friendRequestsCollection.document(requestId).set(request).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun acceptFriendRequest(requestId: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val request = requestDoc.toObject(FriendRequest::class.java)
                ?: throw Exception("Friend request not found")

            if (request.status == "accepted") {
                throw Exception("Friend request already accepted")
            }
            val senderFriendsRef = usersCollection.document(request.senderId).get().await()
            val senderFriends = if (senderFriendsRef["friends"] is MutableList<*>) {
                @Suppress("UNCHECKED_CAST")
                senderFriendsRef["friends"] as? MutableList<String> ?: mutableListOf()
            } else {
                mutableListOf()
            }

            val receiverFriendsRef = usersCollection.document(request.receiverId).get().await()
            val receiverFriends = if (receiverFriendsRef["friends"] is MutableList<*>) {
                @Suppress("UNCHECKED_CAST")
                (receiverFriendsRef["friends"] as? MutableList<String>) ?: mutableListOf()
            } else {
                mutableListOf()
            }

            firestore.runTransaction { transaction ->
                transaction.update(requestDoc.reference, "status", "accepted")
                senderFriends.add(request.receiverId)
                transaction.update(senderFriendsRef.reference, "friends", senderFriends)
                receiverFriends.add(request.senderId)
                transaction.update(receiverFriendsRef.reference, "friends", receiverFriends)
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun rejectFriendRequest(requestId: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            friendRequestsCollection.document(requestId).update("status", "rejected").await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getReceivedFriendRequests(userId: String): List<FriendRequest> = withContext(dispatchers.io) {
        try {
            val querySnapshot = friendRequestsCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(FriendRequest::class.java)?.copy(requestId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting received friend requests", e)
            emptyList()
        }
    }

    suspend fun getCommentCount(postId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .get()
                .await()
            val commentCount = snapshot.size()
            Result.Success(commentCount)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun searchUsers(query: String, currentUserId: String): Result<List<UserProfile>> =
        withContext(dispatchers.io) {
            try {
                val users = usersCollection
                    .whereGreaterThanOrEqualTo("userName", query)
                    .whereLessThanOrEqualTo("userName", query + "\uf8ff")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(UserProfile::class.java)
                    }
                Result.Success(users)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }
    // endregion

    // region Posts & Comments
    suspend fun createPost(
        userId: String,
        postText: String,
        postImages: List<String?> = emptyList(),
        postVideo: String? = null,
        pollData: Map<String, Any?>? = null,
        imageDescription: String = "",
        videoDescription: String = "",
        thumbnailUrl: String? = null // Add thumbnail URL parameter
    ): Result<String> = withContext(dispatchers.io) {
        try {
            Log.d("FirebaseRepository", "Creating post for user $userId, postImages: $postImages, postVideo: $postVideo, pollData: $pollData, imageDescription: $imageDescription, videoDescription: $videoDescription, thumbnailUrl: $thumbnailUrl")
            val postData = mapOf(
                "userId" to userId,
                "postText" to postText,
                "postImages" to postImages,
                "postVideo" to postVideo,
                "profilePictureUrl" to null,
                "userName" to null,
                "timestamp" to Timestamp.now(),
                "pollData" to pollData,
                "imageDescription" to imageDescription, // Add image description
                "videoDescription" to videoDescription, // Add video description
                "thumbnailUrl" to thumbnailUrl // Add thumbnail URL
            )
            val postRef = firestore.collection("posts").add(postData).await()
            incrementUserPostCount(userId)
            checkAndAwardPostBadges(userId)
            Log.d("FirebaseRepository", "Post created, incrementing postCount for user $userId")
            Result.Success(postRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error creating post for user $userId", e)
            Result.Failure(e)
        }
    }
    private suspend fun incrementUserPostCount(userId: String) = withContext(dispatchers.io) {
        try {
            firestore.collection("users").document(userId).update(
                "postCount", FieldValue.increment(1)
            ).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error incrementing post count for user $userId", e)
        }
    }

    suspend fun getUserPostCount(userId: String): Int = withContext(dispatchers.io) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userProfile = userDoc.toObject(UserProfile::class.java)
            userProfile?.postCount ?: 0
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user post count: $userId", e)
            0
        }
    }

    suspend fun decrementUserPostCount(userId: String) = withContext(dispatchers.io) {
        try {
            firestore.collection("users").document(userId).update(
                "postCount", FieldValue.increment(-1)
            ).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error decrementing post count for user $userId", e)
        }
    }

    private suspend fun awardBadge(userId: String, badgeId: String) {
        val currentDate = Timestamp.now()
        val badgeData = mapOf(
            "badges" to FieldValue.arrayUnion(badgeId),
            "badgeDates" to mapOf(badgeId to currentDate)
        )
        when (val result = updateUserBadgesWithDate(userId, badgeData)) {
            is Result.Success -> {
                Log.d("FirebaseRepository", "Successfully awarded badge $badgeId for user $userId")
            }
            is Result.Failure -> {
                Log.e("FirebaseRepository", "Failed to award badge $badgeId for user $userId: ${result.exception.message}")
            }
            is Result.Loading -> {
                Log.d("FirebaseRepository", "Awarding badge $badgeId for user $userId in progress")
            }
        }
    }

    suspend fun checkAndAwardPostBadges(userId: String) = withContext(dispatchers.io) {
        try {
            val postCount = getUserPostCount(userId)
            Log.d("FirebaseRepository", "Checking badges for user $userId, postCount: $postCount")

            if (postCount == 1) {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userProfile = userDoc.toObject(UserProfile::class.java)
                if (userProfile != null && !userProfile.badges.contains("first_post")) {
                    awardBadge(userId, "first_post")
                }
            }

            if (postCount >= 5) {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userProfile = userDoc.toObject(UserProfile::class.java)
                if (userProfile != null && !userProfile.badges.contains("five_posts")) {
                    awardBadge(userId, "five_posts")
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error checking/awarding post badges for user $userId", e)
        }
    }

    suspend fun voteOnPoll(postId: String, userId: String, option: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val pollDataRaw = snapshot.get("pollData")

                check(pollDataRaw is Map<*, *>) { "No poll data found or invalid format" }
                val pollData = pollDataRaw.entries
                    .filter { it.key is String }
                    .associate { it.key as String to it.value }

                val optionsRaw = pollData["options"]
                check(optionsRaw is List<*>) { "No poll options found or invalid format" }
                val options = optionsRaw.filterIsInstance<String>()

                val votesRaw = pollData["votes"]
                val votes = if (votesRaw is Map<*, *>) {
                    votesRaw.entries
                        .filter { it.key is String && it.value is Number }
                        .associate { it.key as String to (it.value as Number).toInt() }
                        .toMutableMap()
                } else {
                    mutableMapOf<String, Int>()
                }

                require(option in options) { "Invalid poll option: $option" }

                votes[option] = (votes[option] ?: 0) + 1

                transaction.update(postRef, mapOf(
                    "pollData" to mapOf(
                        "description" to pollData["description"],
                        "options" to options,
                        "votes" to votes
                    ),
                    "hasUserVoted" to true
                ))
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error voting on poll: $e")
            Result.Failure(e)
        }
    }
    fun getPostsWithUsersFlow(): Flow<List<PostWithUser>> = callbackFlow {
        var lastEmitTime = 0L
        val debounceDelayMs = 500L // Debounce to prevent rapid updates

        val listener = postsCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRepository", "Snapshot listener error: ${error.message}")
                    trySend(emptyList()) // Fallback to empty list on error
                    return@addSnapshotListener
                }

                CoroutineScope(dispatchers.io + CoroutineExceptionHandler { _, throwable ->
                    Log.e("FirebaseRepository", "Coroutine failed in snapshot listener: ${throwable.message}", throwable)
                    trySend(emptyList()) // Fallback on coroutine failure
                }).launch {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastEmitTime < debounceDelayMs) return@launch

                    val posts = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Post::class.java)?.copy()
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Error converting document to Post: ${e.message}", e)
                            null
                        }
                    } ?: emptyList()

                    val currentUserId = Firebase.auth.currentUser?.uid
                    if (currentUserId == null) {
                        trySend(emptyList())
                        return@launch
                    }

                    val deferredPosts = posts.map { post ->
                        async {
                            try {
                                val userResult = getUserProfile(post.userId)
                                val result: PostWithUser? = when (userResult) {
                                    is Result.Success -> {
                                        val user = userResult.data
                                        PostWithUser(post, user)
                                    }
                                    is Result.Failure -> {
                                        Log.e(
                                            "FirebaseRepository",
                                            "Failed to get user for post: ${userResult.exception.message}"
                                        )
                                        null
                                    }
                                    is Result.Loading -> {
                                        Log.d(
                                            "FirebaseRepository",
                                            "User profile loading for post: ${post.postId}"
                                        )
                                        null
                                    }
                                }
                                result
                            } catch (e: Exception) {
                                Log.e("FirebaseRepository", "Error getting user: ${e.message}", e)
                                null
                            }
                        }
                    }

                    val postsWithUsers = deferredPosts.awaitAll().filterNotNull()
                    lastEmitTime = currentTime
                    trySend(postsWithUsers)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(dispatchers.io).distinctUntilChanged().catch { exception ->
        Log.e("FirebaseRepository", "Flow error: ${exception.message}", exception)
        emit(emptyList()) // Emit empty list on any flow error
    }

    // Helper function to check if the user has voted
    private suspend fun checkUserVoted(postId: String, userId: String): Boolean = withContext(dispatchers.io) {
        val firestore = Firebase.firestore
        val voteDoc = firestore.collection("posts").document(postId)
            .collection("votes").document(userId).get().await()
        voteDoc.exists() // Returns true if the user has a vote document for this post
    }

    suspend fun toggleLike(postId: String, userId: String): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                val postRef = postsCollection.document(postId)
                firestore.runTransaction { transaction ->
                    val post = transaction[postRef].toObject(Post::class.java)
                        ?: throw Exception("Post not found")

                    val newLikes = if (post.likes.contains(userId)) {
                        post.likes - userId
                    } else {
                        post.likes + userId
                    }

                    transaction.update(postRef, "likes", newLikes)
                }.await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    suspend fun addComment(postId: String, comment: Comment): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                postsCollection.document(postId)
                    .collection("comments")
                    .add(comment)
                    .await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    fun getCommentsFlow(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = postsCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull {
                    it.toObject(Comment::class.java)?.copy(commentId = it.id)
                } ?: emptyList()

                trySend(comments)
            }

        awaitClose { listener.remove() }
    }
    // endregion

    // region Chats
    suspend fun createChat(currentUserUid: String, friendUid: String): String {
        val chatData = hashMapOf(
            "participants" to listOf(currentUserUid, friendUid),
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )

        val chatRef = chatsCollection.add(chatData).await()
        val chatId = chatRef.id

        val batch = firestore.batch()

        val currentUserChatData = hashMapOf(
            "otherUserId" to friendUid,
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )
        batch[usersCollection.document(currentUserUid).collection("userChats").document(chatId)] =
            currentUserChatData

        val friendChatData = hashMapOf(
            "otherUserId" to currentUserUid,
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )
        batch[usersCollection.document(friendUid).collection("userChats").document(chatId)] =
            friendChatData

        batch.commit().await()
        return chatId
    }

    suspend fun findExistingChat(userId: String, friendId: String): String? {
        val userChatsRef = usersCollection.document(userId).collection("userChats")
        val querySnapshot = userChatsRef.whereEqualTo("otherUserId", friendId).get().await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id
        } else null
    }

    fun getUserChatsFlow(userId: String): Flow<List<Chat>> = callbackFlow {
        val listenerRegistration = usersCollection.document(userId).collection("userChats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { doc ->
                        doc.toChat(doc.id, userId)
                    }
                    trySend(chats).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }.flowOn(dispatchers.io)

    private fun DocumentSnapshot.toChat(chatId: String, currentUserId: String): Chat? {
        return try {
            val otherUserId = getString("otherUserId") ?: return null
            val lastMessage = getString("lastMessage") ?: ""
            val lastMessageTimestamp = getTimestamp("lastMessageTimestamp")

            Chat(
                chatId = chatId,
                participants = listOf(currentUserId, otherUserId),
                lastMessage = lastMessage,
                lastMessageTimestamp = lastMessageTimestamp
            )
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error converting document to Chat", e)
            null
        }
    }



    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.copy(messageId = it.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun verifyChatParticipation(chatId: String, userId: String): Boolean {
        return try {
            val chatDoc = chatsCollection.document(chatId).get().await()
            if (!chatDoc.exists()) {
                Log.e("FirebaseRepository", "Chat document not found for chatId: $chatId")
                return false
            }
            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val isParticipant = userId in participants
            Log.d("FirebaseRepository", "User $userId is${if (isParticipant) "" else " not"} a participant in chatId: $chatId, participants: $participants")
            isParticipant
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error verifying chat participation for chatId: $chatId, userId: $userId: ${e.message}", e)
            false
        }
    }

    suspend fun uploadChatImage(chatId: String, messageId: String, imageUri: Uri): Result<String> =
        withContext(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid ?: return@withContext Result.Failure(Exception("User not authenticated"))
            Log.d("FirebaseRepository", "Starting image upload for chatId: $chatId, messageId: $messageId, userId: $currentUserId, expecting auto-created folder: chatMedia")

            // Verify chat participation and log Storage folder
            if (!verifyChatParticipation(chatId, currentUserId)) {
                Log.e("FirebaseRepository", "User $currentUserId is not a participant in chatId: $chatId, aborting image upload")
                return@withContext Result.Failure(Exception("User is not a participant in chat: $chatId"))
            }

            Log.d("FirebaseRepository", "Verified user participation, proceeding with image upload for chatId: $chatId, expecting auto-created folder: chatMedia")
            Log.d("FirebaseRepository", "Auth token for upload: ${auth.currentUser?.getIdToken(false)?.await()}")
            Log.d("FirebaseRepository", "Storage bucket: ${storage.reference.bucket}")

            Log.d("FirebaseRepository", "Delegating image upload to StorageRepository for path: chatMedia/$chatId/$messageId, expecting auto-creation")
            try {
                val fileExtension = imageUri.getMimeType(context)?.substringAfterLast("/") ?: "jpg"
                val destinationPath = "chatMedia/$chatId/$messageId"
                Log.d("FirebaseRepository", "Destination path for image: $destinationPath, checking for auto-creation")
                val downloadUrl = storageRepository.uploadImage(imageUri, destinationPath)
                if (downloadUrl == null) {
                    Log.e("FirebaseRepository", "StorageRepository failed to upload image for chatId: $chatId, messageId: $messageId")
                    return@withContext Result.Failure(Exception("Failed to upload image via StorageRepository"))
                }
                Log.d("FirebaseRepository", "Image uploaded successfully via StorageRepository, URL: $downloadUrl, to auto-created folder: chatMedia, for chatId: $chatId, messageId: $messageId")
                Result.Success(downloadUrl)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Error uploading chat image for chatId: $chatId, messageId: $messageId: ${e.message}", e)
                Result.Failure(e)
            }
        }

    suspend fun uploadChatVideo(chatId: String, messageId: String, videoUri: Uri): Result<String> =
        withContext(dispatchers.io) {
            val currentUserId = auth.currentUser?.uid ?: return@withContext Result.Failure(Exception("User not authenticated"))
            Log.d("FirebaseRepository", "Starting video upload for chatId: $chatId, messageId: $messageId, userId: $currentUserId, expecting auto-created folder: chatMedia")

            // Verify chat participation and log Storage folder
            if (!verifyChatParticipation(chatId, currentUserId)) {
                Log.e("FirebaseRepository", "User $currentUserId is not a participant in chatId: $chatId, aborting video upload")
                return@withContext Result.Failure(Exception("User is not a participant in chat: $chatId"))
            }

            Log.d("FirebaseRepository", "Verified user participation, proceeding with video upload for chatId: $chatId, expecting auto-created folder: chatMedia")
            Log.d("FirebaseRepository", "Auth token for upload: ${auth.currentUser?.getIdToken(false)?.await()}")
            Log.d("FirebaseRepository", "Storage bucket: ${storage.reference.bucket}")

            Log.d("FirebaseRepository", "Delegating video upload to StorageRepository for path: chatMedia/$chatId/$messageId, expecting auto-creation")
            try {
                val fileExtension = videoUri.getMimeType(context)?.substringAfterLast("/") ?: "mp4"
                val destinationPath = "chatMedia/$chatId/$messageId"
                Log.d("FirebaseRepository", "Destination path for video: $destinationPath, checking for auto-creation")
                val downloadUrl = storageRepository.uploadVideo(videoUri, destinationPath)
                if (downloadUrl == null) {
                    Log.e("FirebaseRepository", "StorageRepository failed to upload video for chatId: $chatId, messageId: $messageId")
                    return@withContext Result.Failure(Exception("Failed to upload video via StorageRepository"))
                }
                Log.d("FirebaseRepository", "Video uploaded successfully via StorageRepository, URL: $downloadUrl, to auto-created folder: chatMedia, for chatId: $chatId, messageId: $messageId")
                Result.Success(downloadUrl)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Error uploading chat video for chatId: $chatId, messageId: $messageId: ${e.message}", e)
                Result.Failure(e)
            }
        }

    suspend fun sendMessage(message: Message): Result<Unit> = withContext(dispatchers.io) {
        val currentUserId = auth.currentUser?.uid ?: return@withContext Result.Failure(Exception("User not authenticated"))
        Log.d("FirebaseRepository", "Sending message for chatId: ${message.chatId}, messageId: ${message.messageId}, userId: $currentUserId, type: ${message.type}")

        // Verify chat participation before sending
        if (!verifyChatParticipation(message.chatId, currentUserId)) {
            Log.e("FirebaseRepository", "User $currentUserId is not a participant in chatId: ${message.chatId}, aborting message send")
            return@withContext Result.Failure(Exception("User is not a participant in chat: ${message.chatId}"))
        }

        try {
            val messageRef = chatsCollection.document(message.chatId).collection("messages").document(message.messageId)
            val messageData = message.toFirestoreMap()
            Log.d("FirebaseRepository", "Creating message document at Firestore path: chats/${message.chatId}/messages/${message.messageId}")
            messageRef.set(messageData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "Message sent successfully, Firestore path: chats/${message.chatId}/messages/${message.messageId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error sending message for chatId: ${message.chatId}, messageId: ${message.messageId}: ${e.message}", e)
            Result.Failure(e)
        }
    }


    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            val messagesRef = firestore.collection("chats/$chatId/messages")
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in messagesRef.documents) {
                batch.update(doc.reference, "read", true)
            }
            batch.commit().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun getChatReference(chatId: String) = firestore.collection("chats").document(chatId)

    suspend fun getAllUsers(): Result<List<UserProfile>> = withContext(dispatchers.io) {
        try {
            val querySnapshot = firestore.collection("users").get().await()
            val users = querySnapshot.documents.mapNotNull { document ->
                document.toObject(UserProfile::class.java)
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    // endregion

    // region Storage
    fun getStorageReference(): StorageReference = storage.reference
    // endregion
}

private fun Uri.getMimeType(context: Context): String? {
    return context.contentResolver.getType(this)
}


