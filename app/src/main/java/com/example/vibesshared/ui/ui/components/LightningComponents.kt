package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.random.Random

data class LightningBolt(
    val id: Int,
    val start: Offset,
    val end: Offset,
    val color: Color,
    val path: Path
)

@Composable
fun LightningBoltAnimation(bolt: LightningBolt, onComplete: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }

    // Faster, more intense lightning
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutLinearInEasing)
        ) { value, _ ->
            progress = value
        }
        // Give a slight delay before removing for a better effect
        delay(50)
        onComplete()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val fadeAlpha = (1 - progress).coerceIn(0f, 1f)
        val brightness = if (progress < 0.2f) {
            // Quick bright flash at the start
            1.0f
        } else {
            // Then fade out
            (1 - ((progress - 0.2f) / 0.8f)).coerceIn(0f, 1f)
        }

        // Draw outer glow - larger and softer
        drawPath(
            path = bolt.path,
            color = bolt.color.copy(alpha = fadeAlpha * 0.2f * brightness),
            style = Stroke(width = 24f)
        )

        // Draw secondary glow
        drawPath(
            path = bolt.path,
            color = bolt.color.copy(alpha = fadeAlpha * 0.5f * brightness),
            style = Stroke(width = 14f)
        )

        // Draw primary bolt
        drawPath(
            path = bolt.path,
            color = bolt.color.copy(alpha = fadeAlpha * 0.8f * brightness),
            style = Stroke(width = 6f)
        )

        // Draw bright core - intense white
        drawPath(
            path = bolt.path,
            color = Color.White.copy(alpha = fadeAlpha * brightness),
            style = Stroke(width = 2f)
        )
    }
}

fun createLightningPath(start: Offset, end: Offset): Path {
    val path = Path()
    path.moveTo(start.x, start.y)

    // Increase segments for more detail
    val segments = 30
    val mainDx = end.x - start.x
    val mainDy = end.y - start.y
    var current = start

    // Main branch
    for (i in 1..segments) {
        val progress = i.toFloat() / segments

        // Calculate the straight-line point at this progress
        val straightX = start.x + mainDx * progress
        val straightY = start.y + mainDy * progress

        // Add randomness that decreases as we get closer to the endpoint
        // This creates the illusion of lightning "finding" its target
        val randomFactor = (1f - progress) * 2f + 0.2f
        val offsetX = (Random.nextFloat() * 2f - 1f) * 20f * randomFactor
        val offsetY = (Random.nextFloat() * 2f - 1f) * 20f * randomFactor

        val nextX = straightX + offsetX
        val nextY = straightY + offsetY

        path.lineTo(nextX, nextY)
        current = Offset(nextX, nextY)

        // Occasionally add a branch (more likely in the middle of the bolt)
        if (i > 2 && i < segments - 2 && Random.nextFloat() < 0.2f) {
            // Create a branch lightning
            val branchPath = Path()
            branchPath.moveTo(current.x, current.y)

            // Branch length varies but is shorter than main bolt
            val branchSegments = Random.nextInt(5, 15)
            var branchCurrent = current

            // Random direction for branch with some bias toward perpendicular to main direction
            val branchAngle = kotlin.math.atan2(mainDy, mainDx) +
                    (Random.nextFloat() * kotlin.math.PI.toFloat() - kotlin.math.PI.toFloat() / 2f)

            val branchLength = Random.nextFloat() * 100f + 50f
            val targetX = current.x + kotlin.math.cos(branchAngle) * branchLength
            val targetY = current.y + kotlin.math.sin(branchAngle) * branchLength

            for (j in 1..branchSegments) {
                val branchProgress = j.toFloat() / branchSegments

                // Calculate the straight-line point on the branch
                val branchStraightX = current.x + (targetX - current.x) * branchProgress
                val branchStraightY = current.y + (targetY - current.y) * branchProgress

                // Add randomness that increases as we get further from the main bolt
                val branchRandomFactor = branchProgress * 1.5f
                val branchOffsetX = (Random.nextFloat() * 2f - 1f) * 15f * branchRandomFactor
                val branchOffsetY = (Random.nextFloat() * 2f - 1f) * 15f * branchRandomFactor

                val branchNextX = branchStraightX + branchOffsetX
                val branchNextY = branchStraightY + branchOffsetY

                path.lineTo(branchNextX, branchNextY)
                path.moveTo(branchCurrent.x, branchCurrent.y) // Return to the branch start
                branchCurrent = Offset(branchNextX, branchNextY)
            }

            // Return to main bolt path
            path.moveTo(current.x, current.y)
        }
    }

    // Always end at exact target
    path.lineTo(end.x, end.y)
    return path
}

