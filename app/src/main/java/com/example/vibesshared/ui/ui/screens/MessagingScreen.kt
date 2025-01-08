// MessagingScreen.kt
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
fun MessagingScreen(navController: NavController) {
    VibesSharedTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Messaging Screen")
            HomeButton(navController)

            // Add UI elements for messages, input field, and media gallery
        }
    }
}