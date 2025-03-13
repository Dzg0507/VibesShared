package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibesshared.ui.ui.data.PowerUp
import com.example.vibesshared.ui.ui.data.PowerUpType
import com.example.vibesshared.ui.ui.viewmodel.PowerUpUiState
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel

@Composable
fun PowerUpBar(
    viewModel: TriviaGameViewModel,
    powerUpState: PowerUpUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A).copy(alpha = 0.8f),
                        Color(0xFF000000).copy(alpha = 0.4f)
                    )
                )
            )
            .padding(8.dp)
    ) {
        // Available power-ups
        if (powerUpState.playerPowerUps.powerUpInventory.powerUps.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(powerUpState.availablePowerUps) { powerUp ->
                    val count = powerUpState.playerPowerUps.getPowerUpCount(powerUp.id)
                    val isOnCooldown = powerUpState.powerUpCooldowns.containsKey(powerUp.id)
                    val isActive = powerUpState.activePowerUps.contains(powerUp.id)

                    if (count > 0) {
                        PowerUpButton(
                            powerUp = powerUp,
                            count = count,
                            isOnCooldown = isOnCooldown,
                            isActive = isActive,
                            onClick = {
                                viewModel.usePowerUp(powerUp.id)
                            }
                        )
                    }
                }
            }
        } else {
            // No power-ups
            Text(
                text = "Earn power-ups by completing quests!",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PowerUpButton(
    powerUp: PowerUp,
    count: Int,
    isOnCooldown: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scaleValue = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    } else {
        1f
    }

    val alpha = if (isOnCooldown) 0.5f else 1f

    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scaleValue)
            .alpha(alpha)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        getPowerUpColor(powerUp.type),
                        getPowerUpColor(powerUp.type).copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        getPowerUpColor(powerUp.type)
                    )
                ),
                shape = CircleShape
            )
            .clickable(enabled = !isOnCooldown && count > 0, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getPowerUpIcon(powerUp.type),
            contentDescription = powerUp.name,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

        // Count badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF3F0071))
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Cooldown overlay
        if (isOnCooldown) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassTop,
                    contentDescription = "On Cooldown",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
fun ActivePowerUpEffect(
    powerUp: PowerUp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha.value)
            .background(
                color = getPowerUpColor(powerUp.type).copy(alpha = 0.2f)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getPowerUpIcon(powerUp.type),
                contentDescription = powerUp.name,
                tint = getPowerUpColor(powerUp.type),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${powerUp.name} Active!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun PowerUpHintOverlay(
    correctAnswer: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3F0071)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = "Hint",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "HINT",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Look for an answer similar to:",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = correctAnswer,
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PowerUpShopItem(
    powerUp: PowerUp,
    playerTokens: Int,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = playerTokens >= powerUp.tokenCost

    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A237E).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                getPowerUpColor(powerUp.type),
                                getPowerUpColor(powerUp.type).copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPowerUpIcon(powerUp.type),
                    contentDescription = powerUp.name,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = powerUp.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = powerUp.description,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .background(
                        color = Color(0xFF00838F).copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Token,
                    contentDescription = "Cost",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${powerUp.tokenCost}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFF00E676) else Color.Gray,
                    contentColor = Color.Black
                ),
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (canAfford) "Purchase" else "Not Enough Tokens",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper functions

private fun getPowerUpColor(type: PowerUpType): Color {
    return when (type) {
        PowerUpType.TIME_FREEZE -> Color(0xFF29B6F6)    // Light Blue
        PowerUpType.TIME_BOOST -> Color(0xFF00E676)     // Green
        PowerUpType.FIFTY_FIFTY -> Color(0xFFFFEB3B)    // Yellow
        PowerUpType.CORRECT_ANSWER -> Color(0xFFFF4081) // Pink
        PowerUpType.HINT -> Color(0xFFFFD54F)          // Amber
        PowerUpType.SKIP_QUESTION -> Color(0xFF7C4DFF)  // Deep Purple
        PowerUpType.DOUBLE_POINTS -> Color(0xFFFF5722)  // Deep Orange
    }
}

private fun getPowerUpIcon(type: PowerUpType): ImageVector {
    return when (type) {
        PowerUpType.TIME_FREEZE -> Icons.Filled.PauseCircle
        PowerUpType.TIME_BOOST -> Icons.Filled.AddAlarm
        PowerUpType.FIFTY_FIFTY -> Icons.Filled.Filter2
        PowerUpType.CORRECT_ANSWER -> Icons.Filled.CheckCircle
        PowerUpType.HINT -> Icons.Filled.Lightbulb
        PowerUpType.SKIP_QUESTION -> Icons.Filled.SkipNext
        PowerUpType.DOUBLE_POINTS -> Icons.Filled.ExposurePlus2
    }
}