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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    private val dispatchers: DispatcherProvider
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
    suspend fun migratePostImages(db: FirebaseFirestore) {
        try {
            val posts = db.collection("posts").get().await()
            posts.documents.forEach { doc ->
                val postImage = doc.getString("postImage")
                if (postImage != null) {
                    db.collection("posts").document(doc.id)
                        .update(mapOf(
                            "postImages" to listOf(postImage),
                            "postImage" to FieldValue.delete()
                        ))
                        .await()
                    Log.d("FirebaseRepository", "Migrated postImage to postImages for document ${doc.id}")
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error migrating post images: ${e.message}", e)
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

    suspend fun getUserProfile(userId: String): Result<UserProfile> =
        withContext(dispatchers.io) {
            Log.d("FirebaseRepository", "getUserProfile called for userId: $userId")
            try {
                val doc = usersCollection.document(userId).get().await()
                Log.d("FirebaseRepository", "getUserProfile: Document snapshot: $doc")
                val userProfile = doc.toObject(UserProfile::class.java)
                Log.d("FirebaseRepository", "getUserProfile: User profile: $userProfile")
                if (userProfile != null) {
                    Result.Success(userProfile)
                } else {
                    val errorMessage = "User profile does not exist or failed to parse for userId: $userId"
                    Log.e("FirebaseRepository", errorMessage)
                    Result.Failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "getUserProfile: Error getting user profile for userId: $userId", e)
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
        pollData: Map<String, Any>? = null
    ): Result<String> = withContext(dispatchers.io) {
        try {
            Log.d("FirebaseRepository", "Creating post for user $userId, postImages: $postImages, postVideo: $postVideo, pollData: $pollData")
            val postData = mapOf(
                "userId" to userId,
                "postText" to postText,
                "postImages" to postImages,
                "postVideo" to postVideo,
                "profilePictureUrl" to null,
                "userName" to null,
                "timestamp" to Timestamp.now(),
                "pollData" to pollData,
                "hasUserVoted" to false
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
        val listener = postsCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                CoroutineScope(dispatchers.io).launch {
                    val posts = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Post::class.java)?.copy()
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Error converting document to Post: ${e.message}", e)
                            null
                        }
                    } ?: emptyList()

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
                    trySend(postsWithUsers)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(dispatchers.io)

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

    suspend fun sendMessage(message: Message): Result<Unit> = withContext(dispatchers.io) {
        try {
            chatsCollection.document(message.chatId)
                .collection("messages")
                .add(message)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
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

    suspend fun uploadChatImage(chatId: String, imageUri: Uri): Result<String> =
        withContext(dispatchers.io) {
            try {
                val fileExtension = imageUri.getMimeType(context)?.substringAfterLast("/") ?: "jpg"
                val imageRef = storage.reference.child("chat_images").child(chatId).child("${UUID.randomUUID()}.$fileExtension")

                val uploadTask = imageRef.putFile(imageUri)
                val snapshot = uploadTask.await()
                val downloadUrl = snapshot.storage.downloadUrl.await().toString()
                Result.Success(downloadUrl)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Error uploading chat image", e)
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface TriviaApiService {
    @GET("api.php")
    suspend fun getQuestions(
        @retrofit2.http.Query("amount") amount: Int = 10,
        @retrofit2.http.Query("difficulty") difficulty: String = "easy",
        @retrofit2.http.Query("category") category: Int? = null,
        @retrofit2.http.Query("type") type: String = "multiple"
    ): TriviaResponse

    @GET("api_daily_challenge.php")
    suspend fun getDailyChallengeQuestions(): TriviaResponse
}

class TriviaRepository @Inject constructor(
    private val apiService: TriviaApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getQuestions(difficulty: String, category: String, questionCount: Int): List<Question> {
        val categoryId = getCategoryCode(category)
        val response =
            apiService.getQuestions(amount = questionCount, difficulty = difficulty, category = categoryId)
        return response.results.map { it.toQuestion() }
    }

    suspend fun getDailyChallengeQuestions(): List<Question> {
        val response = apiService.getDailyChallengeQuestions()
        return response.results.map { it.toQuestion() }
    }

    private fun getCategoryCode(category: String): Int? {
        return when (category) {
            "science" -> 17
            "history" -> 23
            else -> null
        }
    }

    suspend fun saveLeaderboard(scores: List<Int>) {
        context.dataStore.edit { preferences ->
            scores.forEachIndexed { index, score ->
                preferences[intPreferencesKey("score_$index")] = score
            }
        }
    }

    fun getLeaderboard(): Flow<List<Int>> {
        return context.dataStore.data.map { preferences ->
            (0..4).mapNotNull { index ->
                preferences[intPreferencesKey("score_$index")]
            }
        }
    }
}

fun TriviaResponse.Result.toQuestion(): Question {
    return Question(
        question = question,
        correctAnswer = correct_answer,
        incorrectAnswers = incorrect_answers
    )
}

class TriviaRetrofit @Inject constructor() {
    fun createApiService(): TriviaApiService {
        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApiService::class.java)
    }
}