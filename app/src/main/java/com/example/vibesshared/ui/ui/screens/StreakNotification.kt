package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Displays a streak notification when the player achieves consecutive correct answers
 */
@Composable
fun StreakNotification(
    streakCount: Int,
    bonusXp: Int,
    bonusDescription: String
) {
    if (streakCount < 3 || bonusXp <= 0) return // Only show for meaningful streaks

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    // Create pulsating effect
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        // Animate in
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 300,
                easing = EaseOutBack
            )
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            )
        )

        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )

        // Rotate slightly for added effect
        rotation.animateTo(
            targetValue = 5f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        rotation.animateTo(
            targetValue = -5f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )

        // Keep visible for a moment
        delay(2500)

        // Animate out
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(300)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .scale(scale.value * pulseScale.value)
                .alpha(alpha.value)
                .padding(top = 64.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$streakCount",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = " STREAK!",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "+$bonusXp XP BONUS",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (bonusDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bonusDescription,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Add small star particles around the notification
        repeat(10) { index ->
            val particleDelay = index * 50
            val particleInitialScale = remember { Animatable(0f) }
            val particleAlpha = remember { Animatable(0f) }

            val angle = index * 36f // 360 degrees divided by 10 particles
            val radius = 130f
            val x = radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() + 64f // Add offset to center around the card

            LaunchedEffect(Unit) {
                delay(particleDelay.toLong())
                particleInitialScale.animateTo(1f, tween(300))
                particleAlpha.animateTo(0.8f, tween(300))
                delay(1200)
                particleAlpha.animateTo(0f, tween(500))
            }

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(8.dp)
                    .scale(particleInitialScale.value)
                    .alpha(particleAlpha.value * alpha.value)
                    .background(Color.Yellow, RoundedCornerShape(2.dp))
            )
        }
    }
}