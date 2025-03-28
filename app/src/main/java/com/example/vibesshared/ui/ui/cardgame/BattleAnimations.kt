package com.example.vibesshared.ui.ui.cardgame

// File: BattleAnimations.kt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Contains functions or classes responsible for executing battle-related animations.
 */

/**
 * Main composable to route animation events to specific animation implementations.
 * This is the entry point for animations called from BattleScreen.
 *
 * @param animationType AnimationEvent the type of animation to display
 * @param onComplete () -> Unit callback when animation completes
 * @param modifier Modifier additional styling
 */
@Composable
fun BattleAnimation(
    animationType: AnimationEvent,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple wrapper that routes to the appropriate animation based on the type
    Box(modifier = modifier.fillMaxSize()) {
        // In a real implementation, you would instance the correct animation based on the type
        // For now, this is a simple placeholder that just displays text and triggers the callback
        when (animationType) {
            AnimationEvent.ATTACK -> {
                // In a full implementation, you would get references to the attacker and defender cards
                // and pass them to the AttackAnimation composable
                SimpleAnimationPlaceholder(
                    text = "Attack",
                    color = Color(0xFFE53935),
                    onComplete = onComplete
                )
            }
            AnimationEvent.CARD_PLAYED -> {
                // In a full implementation, you would get references to the card and target slot
                // and pass them to the PlayCardAnimation composable
                SimpleAnimationPlaceholder(
                    text = "Card Played",
                    color = Color(0xFF1976D2),
                    onComplete = onComplete
                )
            }
            AnimationEvent.ABILITY_USED -> {
                // In a full implementation, you would get references to the card and effect type
                // and pass them to the AbilityAnimation composable
                SimpleAnimationPlaceholder(
                    text = "Ability Used",
                    color = Color(0xFF7B1FA2),
                    onComplete = onComplete
                )
            }
            AnimationEvent.DAMAGE_TAKEN -> {
                // In a full implementation, you would get references to the card and damage amount
                // and pass them to the DamageTakenAnimation composable
                SimpleAnimationPlaceholder(
                    text = "Damage Taken",
                    color = Color(0xFFE53935),
                    onComplete = onComplete
                )
            }
            AnimationEvent.CARD_DEFEATED -> {
                SimpleAnimationPlaceholder(
                    text = "Card Defeated",
                    color = Color(0xFF424242),
                    onComplete = onComplete
                )
            }
            AnimationEvent.HEAL_EFFECT -> {
                SimpleAnimationPlaceholder(
                    text = "Healed",
                    color = Color(0xFF4CAF50),
                    onComplete = onComplete
                )
            }
            AnimationEvent.BUFF_EFFECT -> {
                SimpleAnimationPlaceholder(
                    text = "Buffed",
                    color = Color(0xFF4CAF50),
                    onComplete = onComplete
                )
            }
            AnimationEvent.DEBUFF_EFFECT -> {
                SimpleAnimationPlaceholder(
                    text = "Debuffed",
                    color = Color(0xFFE53935),
                    onComplete = onComplete
                )
            }
        }
    }
}

/**
 * Simple placeholder animation for when we don't have all the parameters needed
 * for the full animation. Used by the BattleAnimation router.
 */
@Composable
private fun SimpleAnimationPlaceholder(
    text: String,
    color: Color,
    onComplete: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        delay(800)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(300, easing = LinearOutSlowInEasing)
        )
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = alpha.value * 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = color.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        )
    }
}

/**
 * Animates a card moving from hand to the battlefield.
 *
 * @param cardComponent Composable the card component to animate
 * @param targetSlot Rect destination bounds
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun PlayCardAnimation(
    cardComponent: @Composable () -> Unit,
    targetSlot: Rect,
    onComplete: () -> Unit
) {
    var cardRect by remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current

    // Animation values
    val animatedScale = remember { Animatable(1f) }
    val animatedTranslationX = remember { Animatable(0f) }
    val animatedTranslationY = remember { Animatable(0f) }

    LaunchedEffect(targetSlot) {
        // First get the card position
        if (cardRect != Rect.Zero) {
            // Calculate translation to move from current position to target
            val targetTranslationX = targetSlot.center.x - cardRect.center.x
            val targetTranslationY = targetSlot.center.y - cardRect.center.y

            // Calculate scale to match target size
            val targetScale = (targetSlot.width / cardRect.width)
                .coerceAtMost(targetSlot.height / cardRect.height)

            // Play animation sequence
            animatedScale.snapTo(1f)
            animatedTranslationX.snapTo(0f)
            animatedTranslationY.snapTo(0f)

            // First scale up slightly for a "pick up" effect
            animatedScale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
            )

            // Then move to the target position and scale
            coroutineScope {
                launch {
                    animatedTranslationX.animateTo(
                        targetValue = targetTranslationX,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedTranslationY.animateTo(
                        targetValue = targetTranslationY,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedScale.animateTo(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                }
            }

            // Animation complete
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                cardRect = coordinates.boundsInParent()
            }
            .graphicsLayer {
                scaleX = animatedScale.value
                scaleY = animatedScale.value
                translationX = animatedTranslationX.value
                translationY = animatedTranslationY.value
            }
    ) {
        cardComponent()
    }
}

/**
 * Animates an attack between two cards.
 *
 * @param attackerComponent Composable the attacking card
 * @param defenderComponent Composable the defending card
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun AttackAnimation(
    attackerComponent: @Composable () -> Unit,
    defenderComponent: @Composable () -> Unit,
    onComplete: () -> Unit
) {
    var attackerRect by remember { mutableStateOf(Rect.Zero) }
    var defenderRect by remember { mutableStateOf(Rect.Zero) }

    // Animation values
    val translateX = remember { Animatable(0f) }
    val attackEffectAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Start animation once we have both positions
        if (attackerRect != Rect.Zero && defenderRect != Rect.Zero) {
            // Calculate direction vector from attacker to defender
            val direction = defenderRect.center.x - attackerRect.center.x
            val movementDistance = direction * 0.3f // Move 30% of the way

            // Sequence:
            // 1. Move attacker toward defender
            translateX.snapTo(0f)
            translateX.animateTo(
                targetValue = movementDistance,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )

            // 2. Show attack effect
            attackEffectAlpha.snapTo(1f)
            delay(100)

            // 3. Move attacker back to original position
            translateX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = EaseOut)
            )

            // 4. Fade out attack effect
            attackEffectAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )

            // Animation complete
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Attacker card with animation
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    attackerRect = coordinates.boundsInParent()
                }
                .graphicsLayer {
                    translationX = translateX.value
                }
        ) {
            attackerComponent()
        }

        // Defender card
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    defenderRect = coordinates.boundsInParent()
                }
        ) {
            defenderComponent()

            // Attack effect overlay
            if (attackEffectAlpha.value > 0) {
                AttackEffectOverlay(
                    modifier = Modifier.alpha(attackEffectAlpha.value)
                )
            }
        }
    }
}

/**
 * Attack effect overlay animation.
 *
 * @param modifier Modifier additional styling
 */
@Composable
private fun AttackEffectOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.3f))
    ) {
        // Slashes or impact effects
        repeat(3) { index ->
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(
                        x = ((index - 1) * 20).dp,
                        y = ((index - 1) * 20).dp

                    )
                    .rotate(rotation + index * 45f)
                    .scale(1.5f)
            )
        }
    }
}

/**
 * Animates a special ability being used.
 *
 * @param userComponent Composable the card using the ability
 * @param effectType EffectType The type of effect being applied (MODIFIED parameter)
 * @param effectColor Color The color associated with the effect (ADDED parameter)
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun AbilityAnimation(
    userComponent: @Composable () -> Unit,
    effectType: EffectType, // MODIFIED: Use EffectType enum
    effectColor: Color,     // ADDED: Pass the color for visualization
    onComplete: () -> Unit
) {
    var userRect by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) } // Use Rect from ui.geometry

    // Animation values
    val glowAlpha = remember { Animatable(0f) }
    val effectScale = remember { Animatable(0f) }

    // Use the passed effectColor

    LaunchedEffect(Unit) {
        // Start animation once we have the position
        if (userRect != androidx.compose.ui.geometry.Rect.Zero) { // Use Rect from ui.geometry
            // Sequence
            coroutineScope {
                // Glow effect
                launch {
                    glowAlpha.snapTo(0f)
                    glowAlpha.animateTo(
                        targetValue = 0.8f,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                    glowAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                    )
                }

                // Effect burst
                launch {
                    effectScale.snapTo(0.1f)
                    effectScale.animateTo(
                        targetValue = 2f,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                    )
                }
            }

            // Animation complete
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Card with glow effect
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    userRect = coordinates.boundsInParent()
                }
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                effectColor.copy(alpha = glowAlpha.value), // Use effectColor
                                Color.Transparent
                            ),
                            radius = 150f
                        )
                    )
                    .zIndex(-1f)
            )

            userComponent()

            // Effect burst
            if (effectScale.value > 0) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                        .scale(effectScale.value)
                        .alpha(1f - (effectScale.value / 2f))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    effectColor.copy(alpha = 0.7f), // Use effectColor
                                    effectColor.copy(alpha = 0.3f), // Use effectColor
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Animates damage being taken by a card.
 *
 * @param targetComponent Composable the card taking damage
 * @param damage Int amount of damage taken
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun DamageTakenAnimation(
    targetComponent: @Composable () -> Unit,
    damage: Int,
    onComplete: () -> Unit
) {
    var targetRect by remember { mutableStateOf(Rect.Zero) }

    // Animation values
    val shakeOffset = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }
    val damageNumberScale = remember { Animatable(0f) }
    val damageNumberOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Start animation once we have the position
        if (targetRect != Rect.Zero) {
            // Parallel animations
            coroutineScope {
                // Shake effect
                launch {
                    // Series of quick moves back and forth
                    val shakePattern = listOf(5f, -5f, 4f, -4f, 3f, -3f, 2f, -2f, 1f, -1f, 0f)
                    shakePattern.forEach { offset ->
                        shakeOffset.animateTo(
                            targetValue = offset,
                            animationSpec = tween(durationMillis = 50)
                        )
                    }
                }

                // Flash red
                launch {
                    flashAlpha.snapTo(0.7f)
                    delay(100)
                    flashAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }

                // Damage number
                launch {
                    // Pop up and fade out
                    damageNumberScale.snapTo(0.5f)

                    coroutineScope {
                        launch {
                            damageNumberScale.animateTo(
                                targetValue = 1.5f,
                                animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                            )

                            damageNumberScale.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
                            )
                        }

                        launch {
                            damageNumberOffset.snapTo(0f)
                            damageNumberOffset.animateTo(
                                targetValue = -50f,
                                animationSpec = tween(durationMillis = 600)
                            )
                        }
                    }
                }
            }

            // Animation complete
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Target card with shake and flash effects
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    targetRect = coordinates.boundsInParent()
                }
                .graphicsLayer {
                    translationX = shakeOffset.value
                }
        ) {
            targetComponent()

            // Red flash overlay
            if (flashAlpha.value > 0) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Red.copy(alpha = flashAlpha.value))
                )
            }
        }

        // Damage number animation
        if (damageNumberScale.value > 0 && targetRect != Rect.Zero) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = targetRect.center.x.toInt(),
                            y = (targetRect.center.y + damageNumberOffset.value).toInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = damageNumberScale.value
                        scaleY = damageNumberScale.value
                    }
            ) {
                // Damage number text
                Text(
                    text = "-$damage",
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Victory animation played at the end of a battle.
 *
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun VictoryAnimation(
    onComplete: () -> Unit
) {
    // Animation values
    val textScale = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Text animation
        textScale.snapTo(0.1f)
        textScale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Particle effect
        particleAlpha.snapTo(1f)
        particleAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )

        // Animation complete
        delay(800)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Victory text
        Text(
            text = "VICTORY!",
            color = Color(0xFFFFD700), // Gold
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = textScale.value
                    scaleY = textScale.value
                }
        )

        // Particle effects
        if (particleAlpha.value > 0) {
            VictoryParticles(alpha = particleAlpha.value)
        }
    }
}

/**
 * Defeat animation played at the end of a battle.
 *
 * @param onComplete () -> Unit callback when animation completes
 */
@Composable
fun DefeatAnimation(
    onComplete: () -> Unit
) {
    // Animation values
    val textScale = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Overlay fade in
        overlayAlpha.snapTo(0f)
        overlayAlpha.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(durationMillis = 500)
        )

        // Text animation
        textScale.snapTo(0.1f)
        textScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Hold for a moment
        delay(800)

        // Animation complete
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF880000).copy(alpha = overlayAlpha.value)),
        contentAlignment = Alignment.Center
    ) {
        // Defeat text
        Text(
            text = "DEFEAT",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = textScale.value
                    scaleY = textScale.value
                }
        )
    }
}

/**
 * Particle effects for victory animation.
 *
 * @param alpha Float alpha value for the particles
 */
@Composable
private fun VictoryParticles(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition()

    // Create multiple particles with different animations
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(20) { index ->
            val delay = index * 100L
            val baseAngle = index * 18f

            // Unique animation for each particle
            val distance by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 500f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = delay.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )

            val angle by infiniteTransition.animateFloat(
                initialValue = baseAngle,
                targetValue = baseAngle + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000,
                        delayMillis = delay.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )

            // Calculate position
            val x = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val y = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * distance

            // Particle size variation
            val size = 8.dp + ((index % 3) * 4).dp

            // Colors for particles
            val particleColor = when (index % 3) {
                0 -> Color(0xFFFFD700) // Gold
                1 -> Color(0xFFFFFF00) // Yellow
                else -> Color(0xFFFF9800) // Orange
            }

            // Draw particle
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(x = x.dp, y = y.dp)
                    .alpha(alpha * (1f - distance / 500f))
                    .background(particleColor, CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}