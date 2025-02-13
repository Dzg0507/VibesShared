// LottieAnimations.kt
@file:Suppress("DEPRECATION")

package com.example.vibesshared.ui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.screens.DarkBackground


@Composable
fun FloatingParticlesBackground() {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.floating_particles)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.5f // Slower speed for a calming effect
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f), // Subtle transparency
            contentScale = ContentScale.Crop
        )

        // Add a gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.8f),
                            DarkBackground.copy(alpha = 0.95f)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )
    }
}