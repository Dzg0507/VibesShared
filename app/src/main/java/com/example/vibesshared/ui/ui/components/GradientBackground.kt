package com.example.vibesshared.ui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.vibesshared.ui.ui.theme.*

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val gradientColors = if (isDark) {
        listOf(
            SurfaceDark,
            VibrantPurple.copy(alpha = 0.1f),
            ElectricBlue.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            SurfaceLight,
            VibrantPurple.copy(alpha = 0.05f),
            ElectricBlue.copy(alpha = 0.03f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    )
}

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val gradientColors = if (isDark) {
        listOf(
            SurfaceDark,
            VibrantPurple.copy(alpha = 0.15f),
            ElectricBlue.copy(alpha = 0.08f),
            HotPink.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            SurfaceLight,
            VibrantPurple.copy(alpha = 0.08f),
            ElectricBlue.copy(alpha = 0.05f),
            HotPink.copy(alpha = 0.03f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = gradientColors,
                    radius = 1000f
                )
            )
    )
}