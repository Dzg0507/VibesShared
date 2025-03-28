package com.example.vibesshared.ui.ui.cardgame

// File: CardEffects.kt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Handles visual effects applied to cards themselves (glows, particles, state changes).
 */

/**
 * Applies a colored glow based on card tier.
 *
 * @param content @Composable () -> Unit the card content to wrap with the effect
 * @param tier CardTier the card's tier
 * @param intensity Float the glow intensity (0.0-1.0)
 */
@Composable
fun TierGlowEffect(
    content: @Composable () -> Unit,
    tier: CardTier,
    intensity: Float = 1.0f
) {
    // Determine glow color based on tier
    val glowColor = when (tier) {
        CardTier.COMMON -> Color(0xFFBDBDBD) // Light gray
        CardTier.RARE -> Color(0xFF42A5F5) // Blue
        CardTier.EPIC -> Color(0xFFAB47BC) // Purple
        CardTier.LEGENDARY -> Color(0xFFFFB300) // Gold
        CardTier.MYTHIC -> Color(0xFFD50000) // Red
        CardTier.UNCOMMON -> Color(0xFF8BC34A) // Green
    }

    // Animate glow pulsing
    val infiniteTransition = rememberInfiniteTransition()
    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Only apply pulsing to Epic and above
    val finalIntensity = when {
        tier.ordinal >= CardTier.EPIC.ordinal -> pulseIntensity * intensity
        else -> intensity
    }

    // Apply glow effect
    Box {
        // The glow layer
        if (tier != CardTier.COMMON) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp)
                    .blur(radius = 8.dp * finalIntensity)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.5f * finalIntensity),
                                glowColor.copy(alpha = 0.2f * finalIntensity),
                                Color.Transparent
                            ),
                            center = Offset(0.5f, 0.5f),
                            radius = 1.5f
                        )
                    )
            )
        }

        // The card content
        content()
    }
}

/**
 * Applies theme styling based on card type.
 *
 * @param content @Composable () -> Unit the card content to wrap with the effect
 * @param type CardType the card's type
 */
@Composable
fun TypeThemingEffect(
    content: @Composable () -> Unit,
    type: CardType
) {
    // Get base color for the card type
    val typeColor = when (type) {
        CardType.COSMIC -> Color(0xFF3F51B5)      // Indigo
        CardType.ELEMENTAL -> Color(0xFF4CAF50)   // Green
        CardType.TEMPORAL -> Color(0xFFFF9800)    // Orange
        CardType.QUANTUM -> Color(0xFF00BCD4)     // Cyan
        CardType.TECH -> Color(0xFF607D8B)
        CardType.PSYCHIC -> Color(0xFFE91E63)
        CardType.MYTHIC -> Color(0xFF9C27B0)
        CardType.VOID -> Color(0xFF795548)
    }

    // Special visual treatment per type
    when (type) {
        CardType.COSMIC -> CosmicTheme(content, typeColor)
        CardType.TEMPORAL -> TemporalTheme(content, typeColor)
        CardType.QUANTUM -> QuantumTheme(content, typeColor)
        CardType.TECH -> SpectralTheme(content, typeColor)
        CardType.PSYCHIC -> DimensionalTheme(content, typeColor)
        CardType.MYTHIC -> PrimordialTheme(content, typeColor)
        CardType.VOID -> PrimordialTheme(content, typeColor)
        else -> DefaultTheme(content, typeColor) // ELEMENTAL, BIONIC, etc.
    }
}

/**
 * Cosmic theme with star-like particles.
 */
@Composable
private fun CosmicTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    Box {
        // Cosmic particles
        CosmicParticles(baseColor)

        // Border effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.7f),
                            baseColor.copy(alpha = 0.3f),
                            baseColor.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Temporal theme with clock-like effects.
 */
@Composable
private fun TemporalTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing)
        )
    )

    Box {
        // Rotating clock-like pattern
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.2f)
        ) {
            // Clock hand
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = (-30).dp)
                    .rotate(rotation)
                    .align(Alignment.Center)
                    .background(baseColor, RoundedCornerShape(50))
            )
        }

        // Border effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    color = baseColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Quantum theme with probability wave effects.
 */
@Composable
private fun QuantumTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        )
    )

    Box {
        // Quantum wave pattern
        for (i in 0 until 3) {
            val offset = (waveOffset + i * 0.33f) % 1f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = (offset * 200 - 100).dp)
                    .alpha(0.3f)
                    .background(baseColor)
                    .align(Alignment.Center)
            )
        }

        // Border with quantum dots
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.8f),
                            baseColor.copy(alpha = 0.4f),
                            baseColor.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Spectral theme with ghost-like transparency effects.
 */
@Composable
private fun SpectralTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box {
        // Spectral overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.15f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Border effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    color = baseColor.copy(alpha = alpha),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content with slight transparency
        Box(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
            }
        ) {
            content()
        }
    }
}

/**
 * Dimensional theme with portal-like effects.
 */
@Composable
private fun DimensionalTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box {
        // Portal-like background
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.2f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor,
                            Color.Black
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Pulsing border
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(scale)
                .border(
                    width = 2.dp,
                    color = baseColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Primordial theme with ancient, primal effects.
 */
@Composable
private fun PrimordialTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        )
    )

    Box {
        // Ancient texture overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.1f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            baseColor.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        start = Offset(shimmer, 0f),
                        end = Offset(shimmer + 1f, 1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Rugged border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.9f),
                            baseColor.copy(alpha = 0.7f),
                            baseColor.copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Default theme for types without special effects.
 */
@Composable
private fun DefaultTheme(
    content: @Composable () -> Unit,
    baseColor: Color
) {
    Box {
        // Simple colored border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    color = baseColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Content
        content()
    }
}

/**
 * Cosmic particles effect.
 *
 * @param color Color base color for the particles
 */
@Composable
private fun CosmicParticles(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    Box(modifier = Modifier.fillMaxSize()) {
        // Create multiple star-like particles
        repeat(10) { index ->
            val delay = index * 100L

            // Random starting position
            val startX = (-0.5f + index * 0.1f) * 150
            val startY = (-0.5f + (index % 5) * 0.2f) * 150

            // Twinkle animation
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
                        delayMillis = delay.toInt(),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            // Slow drift animation
            val offsetX by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 8000,
                        delayMillis = delay.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 6000,
                        delayMillis = delay.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            // Star particle
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .offset(
                        x = (startX + offsetX).dp,
                        y = (startY + offsetY).dp
                    )
                    .alpha(alpha)
                    .background(color.copy(alpha = 0.8f), CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * Shows status effect icon on a card.
 *
 * @param effectType EffectType the type of effect
 * @param modifier Modifier additional styling
 */
@Composable
fun StatusEffectIcon(
    effectType: EffectType,
    modifier: Modifier = Modifier
) {
    // Determine icon and color based on effect type
    val (icon, color) = when (effectType) {
        EffectType.BUFF_POWER -> Icons.Default.AddCircle to Color(0xFF4CAF50) // Green
        EffectType.BUFF_DEFENSE -> Icons.Default.Shield to Color(0xFF2196F3) // Blue
        EffectType.DEBUFF_POWER -> Icons.Default.RemoveCircle to Color(0xFFE53935) // Red
        EffectType.DEBUFF_DEFENSE -> Icons.Default.BrokenImage to Color(0xFFE53935) // Red
        EffectType.HEAL -> Icons.Default.Favorite to Color(0xFF4CAF50) // Green
        EffectType.DAMAGE_OVER_TIME -> Icons.Default.LocalFireDepartment to Color(0xFFE53935) // Red
        EffectType.STUN -> Icons.Default.FlashOff to Color(0xFFFFEB3B) // Yellow
        EffectType.SHIELD -> Icons.Default.Security to Color(0xFF2196F3) // Blue
        EffectType.REFLECT -> Icons.Default.Loop to Color(0xFF9C27B0) // Purple
        EffectType.CLEANSE -> Icons.Default.AutoFixHigh to Color(0xFFFFEB3B) // Yellow
    }

    // Animate the icon with a subtle pulse
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .scale(scale)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .border(1.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = effectType.toString(),
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Card flip animation between front and back.
 *
 * @param front @Composable () -> Unit front side content
 * @param back @Composable () -> Unit back side content
 * @param isFlipped Boolean whether card is showing back side
 * @param modifier Modifier additional styling
 */
@Composable
fun CardFlipAnimation(
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isFlipped) {
        rotation.animateTo(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 8 * density
                }
        ) {
            if (rotation.value <= 90f) {
                // Show front
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = 1 - (rotation.value / 90f).coerceIn(0f, 1f)
                        }
                ) {
                    front()
                }
            } else {
                // Show back (flipped)
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationY = 180f
                            alpha = ((rotation.value - 90f) / 90f).coerceIn(0f, 1f)
                        }
                ) {
                    back()
                }
            }
        }
    }
}

/**
 * Hover effect for cards.
 *
 * @param content @Composable () -> Unit card content
 * @param isHovered Boolean whether the card is being hovered over
 * @param modifier Modifier additional styling
 */
@Composable
fun CardHoverEffect(
    content: @Composable () -> Unit,
    isHovered: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val elevation = remember { Animatable(4f) }

    LaunchedEffect(isHovered) {
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = if (isHovered) 1.05f else 1f,
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                elevation.animateTo(
                    targetValue = if (isHovered) 8f else 4f,
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.shadowElevation = elevation.value
            }
    ) {
        content()
    }
}