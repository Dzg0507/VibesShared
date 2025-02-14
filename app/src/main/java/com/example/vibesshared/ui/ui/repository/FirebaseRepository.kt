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
import com.example.vibesshared.ui.ui.data.Question
import com.example.vibesshared.ui.ui.data.TriviaResponse
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
    private val friendRequestsCollection = firestore.collection("friendRequests") // Keep, but unused by getFriends
    private val chatsCollection = firestore.collection("chats")
    private val postsCollection = firestore.collection("posts")

    // region Authentication (Correct - As you provided)
    fun getCurrentUser() = auth.currentUser

    suspend fun signIn(email: String, pass: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
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

    // region User Profile (Corrected - Add getUserProfile)
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

                // Correct path: userId/profilePictures/...
                val imageRef = storage.reference.child(userId).child("profilePictures").child("${UUID.randomUUID()}.$fileExtension") // Improved file naming

                val uploadTask = imageRef.putFile(imageUri)
                val snapshot = uploadTask.await()
                val downloadUrl = snapshot.storage.downloadUrl.await().toString()

                // Update the user's document (if needed) - depends on your data structure
                usersCollection.document(userId)
                    .update("profilePictureUrl", downloadUrl) // Or set the whole UserProfile

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

    //USE THIS FOR GETTING USER PROFILE
    suspend fun getUserProfile(userId: String): Result<UserProfile> =
        withContext(dispatchers.io) {
            Log.d("FirebaseRepository", "getUserProfile called for userId: $userId") // Add logging here
            try {
                val doc = usersCollection.document(userId).get().await()
                Log.d("FirebaseRepository", "getUserProfile: Document snapshot: $doc") // Add this

                val userProfile = doc.toObject(UserProfile::class.java)
                Log.d("FirebaseRepository", "getUserProfile: User profile: $userProfile") //Add

                if (userProfile != null) {
                    Result.Success(userProfile)
                } else {
                    val errorMessage = "User profile does not exist or failed to parse for userId: $userId"
                    Log.e("FirebaseRepository", errorMessage) // Log the error
                    Result.Failure(Exception(errorMessage)) //Return
                }
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "getUserProfile: Error getting user profile for userId: $userId", e) //Log
                Result.Failure(e)
            }
        }

    // endregion


    suspend fun getFriends(userId: String): Result<List<UserProfile>> = withContext(dispatchers.io) {
        try {
            val currentUserDoc = usersCollection.document(userId).get().await()

            val friendIds = currentUserDoc.get("friends") as? List<String> ?: listOf()

            val friends = friendIds.mapNotNull { friendId ->
                val userResult = getUserProfile(friendId)
                if (userResult is Result.Success) {
                    userResult.data
                } else {
                    null // Filter out failures
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

            val requestId = friendRequestsCollection.document().id  // Get a new document ID
            val request = FriendRequest( //Use data class
                requestId = requestId, //Set the id
                senderId = senderId,
                receiverId = receiverId,
                status = "pending",
                participants = listOf(senderId, receiverId) // Add both participants
            )

            friendRequestsCollection.document(requestId).set(request).await() //Set it by id
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    //Correct acceptFriendRequest
    suspend fun acceptFriendRequest(requestId: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            // Get the friend request document
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val request = requestDoc.toObject(FriendRequest::class.java)
                ?: throw Exception("Friend request not found")

            // Update the status and add the participants array in a single transaction.
            friendRequestsCollection.document(requestId).update(mapOf(
                "status" to "accepted",
                "participants" to listOf(request.senderId, request.receiverId) // Add participants
            )).await()

            Result.Success(Unit)
        } catch (e: Exception){
            Result.Failure(e)
        }
    }
    //Corrected reject
    suspend fun rejectFriendRequest(requestId: String): Result<Unit> = withContext(dispatchers.io) {
        try{
            friendRequestsCollection.document(requestId).update("status", "rejected").await() //Update the status
            Result.Success(Unit)
        } catch (e: Exception){
            Result.Failure(e)
        }
    }


    //Gets the friend requests sent to user
    suspend fun getReceivedFriendRequests(userId: String): List<FriendRequest> = withContext(dispatchers.io) {
        try {
            val querySnapshot = friendRequestsCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "pending") //Get pending
                .get()
                .await()

            // Use .mapNotNull and .copy to correctly populate requestId
            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(FriendRequest::class.java)?.copy(requestId = doc.id) // Populate requestId
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting received friend requests", e)
            emptyList() // Return empty list on error
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
                        doc.toObject(UserProfile::class.java) // Directly to UserProfile

                    }
                Result.Success(users)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    // endregion

    // region Posts & Comments (Correct - As you provided)
    suspend fun createPost(post: Post): Result<String> = withContext(dispatchers.io) {
        try {
            val docRef = postsCollection.document()
            val newPost = post.copy(postId = docRef.id, timestamp = Timestamp.now())
            docRef.set(newPost).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
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
                            doc.toObject(Post::class.java)
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Error converting document to Post: ${e.message}", e)
                            null
                        }
                    } ?: emptyList()

                    val deferredPosts = posts.map { post ->
                        async {
                            try {
                                val userResult = getUserProfile(post.userId)
                                when (userResult) {
                                    is Result.Success -> {
                                        PostWithUser(post, userResult.data)
                                    }
                                    is Result.Failure -> {
                                        Log.e(
                                            "FirebaseRepository",
                                            "Failed to get user for post: ${userResult.exception.message}"
                                        )
                                        null
                                    }
                                    else -> {
                                        null
                                    }
                                }
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
                    val post = transaction.get(postRef).toObject(Post::class.java)
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

    // region Chats (Corrected - Add getUserProfile, remove toChatListItem)
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
        batch.set(usersCollection.document(currentUserUid).collection("userChats").document(chatId), currentUserChatData)

        val friendChatData = hashMapOf(
            "otherUserId" to currentUserUid,
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )
        batch.set(usersCollection.document(friendUid).collection("userChats").document(chatId), friendChatData)

        batch.commit().await()
        return chatId
    }

    suspend fun findExistingChat(userId: String, friendId: String): String? {
        val userChatsRef = usersCollection.document(userId).collection("userChats")
        val querySnapshot = userChatsRef.whereEqualTo("otherUserId", friendId).get().await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id
        } else{
            null
        }
    }
    //THIS FLOWS THE CHATS
    fun getUserChatsFlow(userId: String): Flow<List<Chat>> = callbackFlow {
        val listenerRegistration = usersCollection.document(userId).collection("userChats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { doc ->
                        doc.toChat(doc.id, userId) // Use the corrected toChat
                    }
                    trySend(chats).isSuccess //Try to send it
                }
            }
        awaitClose{listenerRegistration.remove()}
    }.flowOn(dispatchers.io)

    // Helper function to convert DocumentSnapshot to Chat, including current user
    private fun DocumentSnapshot.toChat(chatId: String, currentUserId: String): Chat? {
        return try {
            val otherUserId = getString("otherUserId") ?: return null
            val lastMessage = getString("lastMessage") ?: ""
            val lastMessageTimestamp = getTimestamp("lastMessageTimestamp")

            // Include both current user and other user in participants
            Chat(
                chatId = chatId,
                participants = listOf(currentUserId, otherUserId), // Both users!
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
                val fileExtension = imageUri.getMimeType(context)?.substringAfterLast("/")?: "jpg"

                val imageRef = storage.reference.child("chat_images").child(chatId).child("${UUID.randomUUID()}.$fileExtension") // Consistent path & UUID

                val uploadTask = imageRef.putFile(imageUri)
                val snapshot = uploadTask.await()
                val downloadUrl = snapshot.storage.downloadUrl.await().toString()
                Result.Success(downloadUrl)

            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Error uploading chat image", e)
                Result.Failure(e)
            }
        }

    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> = withContext(dispatchers.io){
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

        } catch (e: Exception){
            Result.Failure(e)
        }
    }
    fun getChatReference(chatId: String) = firestore.collection("chats").document(chatId)

    //CORRECT WAY TO GET ALL USERS - KEEP, but not used for chats
    suspend fun getAllUsers(): Result<List<UserProfile>> = withContext(dispatchers.io){
        try {
            val querySnapshot = firestore.collection("users").get().await()
            val users = querySnapshot.documents.mapNotNull { document ->
                document.toObject(UserProfile::class.java) // Directly to UserProfile
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    // endregion

    // region Storage (Correct - As you provided)
    fun getStorageReference(): StorageReference = storage.reference
    // endregion
}
private fun Uri.getMimeType(context: Context): String? {
    return context.contentResolver.getType(this)
    }

//Trivia Game Functions



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
    @ApplicationContext private val context: Context // Add @ApplicationContext

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
//Make the toQuestion() public
fun TriviaResponse.Result.toQuestion(): Question {
    return Question(
        question = question,
        correctAnswer = correct_answer,
        incorrectAnswers = incorrect_answers
    )
}


// Retrofit setup
class TriviaRetrofit @Inject constructor() {
    fun createApiService(): TriviaApiService {
        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApiService::class.java)
    }
}