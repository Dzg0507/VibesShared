package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.VideoPlayer
import com.example.vibesshared.ui.ui.theme.DarkBackground

@Composable
fun VideoPlaybackScreen(
    navController: NavController,
    videoUrl: String
) {
    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            VideoPlayer(
                modifier = Modifier.fillMaxSize(),
                videoUrl = videoUrl,
                onVideoEnd = { navController.popBackStack() } // Return to previous screen when video ends
            )
        }
    }
}