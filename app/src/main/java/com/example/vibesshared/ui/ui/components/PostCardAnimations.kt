// PostCardAnimations.kt
package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vibesshared.ui.ui.screens.NeonBlue
import com.example.vibesshared.ui.ui.screens.NeonGreen
import com.example.vibesshared.ui.ui.screens.NeonPink
import com.example.vibesshared.ui.ui.screens.NeonYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ParticleEffect(modifier: Modifier = Modifier) {
    val particles = remember { List(50) { Particle() } }
    val infiniteTransition = rememberInfiniteTransition()

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            particle.update(time)
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(
                    x = particle.x * size.width,
                    y = particle.y * size.height
                ),
                alpha = 0.7f
            )
        }
    }
}

private class Particle {
    var x = Random.nextFloat()
    var y = Random.nextFloat()
    val size = Random.nextFloat() * 4 + 2
    val color = when (Random.nextInt(4)) {
        0 -> NeonGreen
        1 -> NeonPink
        2 -> NeonBlue
        else -> NeonYellow
    }
    private val speed = Random.nextFloat() * 0.02f + 0.01f
    private val angle = Random.nextFloat() * 2 * PI.toFloat()

    fun update(time: Float) {
        x += cos(angle) * speed
        y += sin(angle) * speed
        if (x < 0 || x > 1) x = x.coerceIn(0f, 1f)
        if (y < 0 || y > 1) y = y.coerceIn(0f, 1f)
    }
}

@Composable
fun HolographicProfile(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        )
    )

    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    shape = CircleShape
                    clip = true
                }
                .drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.5f),
                            NeonBlue.copy(alpha = 0.5f),
                            NeonPink.copy(alpha = 0.5f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width * shimmerProgress, size.height * shimmerProgress)
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(gradient, blendMode = BlendMode.Screen)
                    }
                }
        )
    }
}