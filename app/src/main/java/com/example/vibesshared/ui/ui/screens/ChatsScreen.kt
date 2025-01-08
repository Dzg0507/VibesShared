// ChatsScreen.kt
package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme

@Composable
fun ChatsScreen(navController: NavController) {
    VibesSharedTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Add UI elements to display a list of active chats, start new chats, etc.
        }
    }
}