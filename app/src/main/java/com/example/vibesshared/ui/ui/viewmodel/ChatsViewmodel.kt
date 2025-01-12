package com.example.vibesshared.ui.ui.viewmodel

// ChatsViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatData(
    val chatId: String? = null,
    val members: List<String> = listOf(),
    val messages: Map<String, MessageData> = mapOf()
)

data class MessageData(
    val content: String = "",
    val sender: String = "",
    val timestamp: Long = 0
)

class ChatsViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val chatsRef = database.reference.child("chats")
    private val _chatDataList = MutableStateFlow<List<ChatData>>(emptyList())
    val chatDataList: StateFlow<List<ChatData>> = _chatDataList.asStateFlow()

    init {
        createInitialChats()
    }

    private fun createInitialChats() {
        viewModelScope.launch {
            // Generate unique chat IDs using push()
            val chat1Ref = chatsRef.push()
            val chatId1 = chat1Ref.key

            val chat2Ref = chatsRef.push()
            val chatId2 = chat2Ref.key

            // Create sample chat data
            val chatData1 = ChatData(
                chatId = chatId1,
                members = listOf("user1", "user2"), // Replace with actual user IDs later
                messages = mapOf(
                    "message1" to MessageData("Hello!", "user1", 1673526400000),
                    "message2" to MessageData("Hi there!", "user2", 1673526460000)
                )
            )

            val chatData2 = ChatData(
                chatId = chatId2,
                members = listOf("user2", "user3"), // Replace with actual user IDs later
                messages = mapOf(
                    "message1" to MessageData("How are you?", "user2", 1673526500000),
                    "message2" to MessageData("I'm doing well!", "user3", 1673526580000),
                )
            )
        }


                        fetchChatData()
        }

        private fun fetchChatData() {
            chatsRef.addValueEventListener(object : ValueEventListener {
                @UnstableApi
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats = mutableListOf<ChatData>()
                    for (chatSnapshot in snapshot.children) {
                        val chatData = chatSnapshot.getValue(ChatData::class.java)
                        chatData?.let { chats.add(it) }
                        _chatDataList.value = chats
                        Log.d("ChatData", "Raw chat data: ${chatSnapshot.value}")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }

    fun createChat(chatData: ChatData) { // Add this function
        viewModelScope.launch {
            val newChatRef = chatsRef.push()
            newChatRef.setValue(chatData)
        }
    }
}



    // Add other functions to fetch, update, or delete chats as needed
