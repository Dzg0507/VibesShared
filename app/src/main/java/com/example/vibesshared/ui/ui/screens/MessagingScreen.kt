// MessagingScreen.kt
package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue


data class Message(
    val id: Int,
    val senderId: Int,
    val content: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(navController: NavController, chatId: String) {
    val messages = remember { mutableStateListOf<Message>() }
    var newMessageText by remember { mutableStateOf("") }

    // Simulate receiving messages
    LaunchedEffect(Unit) {
        repeat(5) {
            messages.add(
                Message(
                    it,
                    if (it % 2 == 0) 1 else 0,
                    "Message ${it + 1}",
                    "12:0${it} PM"
                )
            )
        }
    }









//data class Message(
  //  val id: Int,
    //val senderId: Int,
   // val content: String,
    //val timestamp: String
//)

//@Composable
//fun MessagingScreen(navController: NavController, chatId: String) {
  //  val messages = remember { mutableStateListOf<Message>() }
    //var newMessageText by remember { mutableStateOf("") }
    //val database = FirebaseDatabase.getInstance()

    // Simulate receiving messages
  //  LaunchedEffect(chatId) {
    //    val messagesRef = database.reference.child("chats").child(chatId).child("messages")
      //  messagesRef.addValueEventListener(object : ValueEventListener {
        //    override fun onDataChange(snapshot: DataSnapshot) {
          //      messages.clear()
            //    for (messageSnapshot in snapshot.children) {
              //      val messageData =
                //        messageSnapshot.getValue(Message::class.java) // Assuming you have a Message data class
                  //  messageData?.let { messages.add(it) }
                //}
            //}

           // override fun onCancelled(error: DatabaseError) {
                // Handle error
           // }
        //})




    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display chat messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageItem(message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field and send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    label = { Text("Enter message") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            messages.add(
                                Message(
                                    messages.size,
                                    0, // Assuming current user ID is 0
                                    newMessageText,
                                    "Now"
                                )
                            )
                            newMessageText = ""
                        }
                    },
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val isCurrentUser = message.senderId == 0 // Assuming current user ID is 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = message.content,
            modifier = Modifier
                .background(if (isCurrentUser) Color.Cyan else Color.LightGray)
                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message.timestamp,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}