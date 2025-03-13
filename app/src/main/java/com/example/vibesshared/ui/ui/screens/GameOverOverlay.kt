package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibesshared.ui.ui.data.Reward
import com.example.vibesshared.ui.ui.data.RewardTier
import com.example.vibesshared.ui.ui.data.RewardType

/**
 * Enhanced Game Over overlay that shows detailed reward information
 */
@Composable
fun EnhancedGameOverOverlay(
    score: Int,
    totalQuestions: Int,
    questCompleted: Boolean,
    questTitle: String,
    awardedRewards: List<Reward> = emptyList(),
    onDismiss: () -> Unit  // Add dismiss callback
) {
    val percentage = (score.toFloat() / totalQuestions) * 100
    val resultColor = when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFFE57373) // Red
    }

    // Animation for rewards
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A237E).copy(alpha = 0.95f)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                if (questCompleted) {
                    // Modified "Completed" banner section
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        // Diagonal ribbon with "COMPLETED" text
                        Box(
                            modifier = Modifier
                                .width(320.dp)
                                .height(70.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                    translationY = -10f
                                }
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "QUEST COMPLETED",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    Text(
                        text = questTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "Game Over",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Score display
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    resultColor,
                                    resultColor.copy(alpha = 0.7f),
                                    resultColor.copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "/$totalQuestions",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    color = resultColor,
                    fontWeight = FontWeight.Bold
                )

                // Result message
                Text(
                    text = when {
                        questCompleted -> "Great job! You've completed the quest and earned rewards!"
                        percentage >= 80 -> "Excellent work! Your knowledge is impressive!"
                        percentage >= 60 -> "Good job! Keep practicing to improve your score!"
                        else -> "Practice makes perfect! Try again soon!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Only show rewards if quest is completed and there are rewards
                if (questCompleted && awardedRewards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Rewards section divider
                    @Suppress("DEPRECATION")
                    Divider(
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Rewards Earned",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // List all earned rewards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        println("Displaying ${awardedRewards.size} rewards")

                        awardedRewards.forEach { reward ->
                            println("Displaying reward: ${reward.name}")
                            QuestRewardItem(reward = reward)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add a dismiss button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    score: Int,
    totalQuestions: Int,
    questCompleted: Boolean,
    questTitle: String = "",
    awardedRewards: List<Reward> = emptyList(),
    onDismiss: () -> Unit  // Add dismiss callback
) {
    println("GameOverOverlay called - Quest completed: $questCompleted, Title: $questTitle, Rewards: ${awardedRewards.size}")

    EnhancedGameOverOverlay(
        score = score,
        totalQuestions = totalQuestions,
        questCompleted = questCompleted,
        questTitle = questTitle,
        awardedRewards = awardedRewards,
        onDismiss = onDismiss  // Pass the dismiss callback
    )
}

@Composable
fun QuestRewardItem(reward: Reward) {
    val rewardColor = getRewardTierColor(reward.tier)
    val infiniteTransition = rememberInfiniteTransition()
    val glowStrength = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = (glowStrength.value * 8).dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        rewardColor.copy(alpha = glowStrength.value),
                        Color.White.copy(alpha = glowStrength.value * 0.7f),
                        rewardColor.copy(alpha = glowStrength.value)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reward icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                rewardColor.copy(alpha = 0.3f),
                                rewardColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getRewardTypeIcon(reward.type),
                    contentDescription = null,
                    tint = rewardColor,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(8.dp, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Reward details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Tier badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = rewardColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = reward.tier.name,
                        color = rewardColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = reward.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = rewardColor,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                // Values
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (reward.xpValue > 0) {
                        ValueBadge(
                            icon = Icons.Filled.Star,
                            value = "+${reward.xpValue} XP",
                            color = Color(0xFF4CAF50)
                        )
                    }

                    if (reward.multiverseTokenValue > 0) {
                        ValueBadge(
                            icon = Icons.Filled.Token,
                            value = "+${reward.multiverseTokenValue} Tokens",
                            color = Color(0xFF00FFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ValueBadge(
    icon: ImageVector,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



private fun getRewardTierColor(tier: RewardTier): Color {
    return when (tier) {
        RewardTier.COMMON -> Color(0xFF78909C)
        RewardTier.RARE -> Color(0xFF29B6F6)
        RewardTier.EPIC -> Color(0xFF9C27B0)
        RewardTier.LEGENDARY -> Color(0xFFFF9800)
        RewardTier.MYTHICAL -> Color(0xFFE91E63)
    }
}

private fun getRewardTypeIcon(type: RewardType): ImageVector {
    return when (type) {
        RewardType.UNIQUE_CHARACTER -> Icons.Filled.Person
        RewardType.COSMETIC_ITEM -> Icons.Filled.Palette
        RewardType.MULTIVERSE_FRAGMENT -> Icons.Filled.Extension
        RewardType.RARE_ACHIEVEMENT -> Icons.Filled.EmojiEvents
        RewardType.EXCLUSIVE_BACKGROUND -> Icons.Filled.Wallpaper
    }
}