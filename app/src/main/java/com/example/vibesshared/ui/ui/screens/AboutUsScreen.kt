package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AboutUsScreen(
    name: String,
    settingsName: String,
    aboutName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = aboutName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Vibes Shared App",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Blue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Version 2.3.1",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Green,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "As someone who loves connecting with people and sharing experiences, I was inspired to create this chat app to provide a space for others to do the same. I believe in the power of technology to foster meaningful interactions and build stronger communities. While I may not be a coder myself, I've dedicated myself to learning the ins and outs of app development to bring this vision to life. I hope this app helps you stay connected with the people who matter most to you.                                           -------------------------------------------------------------------------------    Solo Creator/Developer                   Devin Z Griffin",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(30.dp))

                    AsyncImage(
                    model = "https://picsum.photos/200/300", // Replace with actual avatar URL
            contentDescription = "Avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )

    }
}