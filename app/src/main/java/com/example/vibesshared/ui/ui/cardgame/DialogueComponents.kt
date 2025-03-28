package com.example.vibesshared.ui.ui.cardgame

// File: DialogComponents.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Reusable dialog composables for common scenarios.
 */

/**
 * Dialog shown at the end of a battle.
 *
 * @param result BattleResult outcome of the battle
 * @param summary String description of the battle results
 * @param rewards List<Any> rewards earned from the battle
 * @param onConfirm () -> Unit callback when confirmed
 */
@Composable
fun GameOverDialog(
    result: BattleResult,
    summary: String,
    rewards: List<Any> = emptyList(),
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onConfirm) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2D2D),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title and icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (result) {
                                BattleResult.PLAYER_VICTORY -> Color(0xFF4CAF50)
                                BattleResult.ENEMY_VICTORY -> Color(0xFFE53935)
                                else -> Color(0xFF607D8B)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (result) {
                                BattleResult.PLAYER_VICTORY -> Icons.Default.EmojiEvents
                                BattleResult.ENEMY_VICTORY -> Icons.Default.SentimentVeryDissatisfied
                                else -> Icons.Default.Balance
                            },
                            contentDescription = "Battle Result",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when (result) {
                                BattleResult.PLAYER_VICTORY -> "Victory!"
                                BattleResult.ENEMY_VICTORY -> "Defeat!"
                                else -> "Battle Ended"
                            },
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Battle summary
                Text(
                    text = summary,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rewards section (if any)
                if (rewards.isNotEmpty()) {
                    Text(
                        text = "Rewards",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display rewards (simplified - would typically show cards, tokens, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        rewards.forEach { reward ->
                            // Placeholder for reward rendering
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFF424242), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reward.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Confirm button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (result) {  // Changed from backgroundColor
                            BattleResult.PLAYER_VICTORY -> Color(0xFF4CAF50)
                            BattleResult.ENEMY_VICTORY -> Color(0xFFE53935)
                            else -> Color(0xFF607D8B)
                        }
                    )
                ) {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Dialog showing a card reward.
 *
 * @param card MultiverseCard the card received as reward
 * @param onViewCard () -> Unit callback to view card details
 * @param onDismiss () -> Unit callback to dismiss dialog
 */
@Composable
fun RewardDialog(
    card: MultiverseCard,
    onViewCard: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2D2D),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "New Card Acquired!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card display
                CardComponent(
                    card = card,
                    displayMode = CardDisplayMode.FULL,
                    onClick = TODO(),
                    modifier = TODO(),
                    isSelected = TODO(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // View card button
                    Button(
                        onClick = onViewCard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3F51B5)  // Changed from backgroundColor
                        )
                    ) {
                        Text(
                            text = "View Details",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dismiss button
                    Button(
                        onClick = onViewCard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3F51B5)  // Changed from backgroundColor
                        )
                    ) {
                        Text(
                            text = "Add to Collection",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generic confirmation dialog.
 *
 * @param title String dialog title
 * @param message String dialog message
 * @param confirmText String text for confirm button
 * @param cancelText String text for cancel button
 * @param onConfirm () -> Unit callback when confirmed
 * @param onDismiss () -> Unit callback when dismissed
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2D2D),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(text = cancelText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Confirm button
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)  // Changed from backgroundColor
                        )
                    ) {
                        Text(
                            text = confirmText,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Help/tutorial dialog.
 *
 * @param title String dialog title
 * @param contents List<Pair<String, String>> list of help sections with titles and descriptions
 * @param onDismiss () -> Unit callback when dismissed
 */
@Composable
fun HelpDialog(
    title: String,
    contents: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2D2D),
            tonalElevation  = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 500.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Modifier.padding(vertical = 16.dp)
                Color.White.copy(alpha = 0.2f)
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.2f)
                )

                // Help content
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    contents.forEach { (sectionTitle, description) ->
                        Column {
                            Text(
                                text = sectionTitle,
                                color = Color(0xFF4CAF50),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = description,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)  // Changed from backgroundColor
                    )
                ) {
                    Text(
                        text = "Got it!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}