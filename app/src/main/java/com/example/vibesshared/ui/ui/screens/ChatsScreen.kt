package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme

@Composable
fun ChatsScreen(navController: NavController) {
    VibesSharedTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Chats Screen")
            HomeButton(navController)
            // Add UI elements to display a list of active chats, start new chats, etc.
        }
    }
}