package com.example.vibesshared.ui.ui.cardgame
// File: BattleActionComponents.kt (Corrected Filename)

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Specific UI elements used during battle for player actions.
 */

/**
 * Main battle actions container.
 *
 * @param activeCard CardInPlay? player's active card
 * @param isPlayerTurn Boolean whether it's the player's turn
 * @param canAffordAbility (Int) -> Boolean function to check if player can afford ability
 * @param onAttack () -> Unit callback for attack action
 * @param onUseAbility (Int) -> Unit callback for ability use with index
 * @param onEndTurn () -> Unit callback for end turn action
 * @param modifier Modifier additional styling for the panel itself
 */
@Composable
fun BattleActionPanel(
    activeCard: CardInPlay?,
    isPlayerTurn: Boolean,
    canAffordAbility: (Int) -> Boolean,
    onAttack: () -> Unit,
    onUseAbility: (Int) -> Unit,
    onEndTurn: () -> Unit,
    modifier: Modifier = Modifier // Apply modifier to the Column
) {
    Column(
        modifier = modifier // Use the passed modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Adjusted padding slightly
            .background(Color(0xFF1A1A1A).copy(alpha = 0.8f), RoundedCornerShape(12.dp)) // Use theme color and rounder corners
            .border(1.dp, Color(0xFF424242), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
    ) {
        Text(
            text = "Actions",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally) // Center title
        )

        // Attack button
        AttackButton(
            enabled = isPlayerTurn && activeCard != null && !activeCard.isStunned(), // Check if stunned
            onAttack = onAttack
        )

        // Abilities section
        if (activeCard != null && activeCard.abilities.isNotEmpty()) {
            Text(
                text = "Abilities",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).align(Alignment.Start)
            )

            activeCard.abilities.forEachIndexed { index, abilityInPlay ->
                AbilityButton(
                    ability = abilityInPlay,
                    // Check stun, readiness, and affordability
                    enabled = isPlayerTurn && !activeCard.isStunned() && abilityInPlay.isReady && canAffordAbility(index),
                    { onUseAbility(index) },
                    Modifier.padding(end = 8.dp, start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // End turn button
        EndTurnButton(
            enabled = isPlayerTurn,
            onEndTurn = onEndTurn
        )
    }
}


/**
 * Button for attack action.
 */
@Composable
fun AttackButton(
    enabled: Boolean,
    onAttack: () -> Unit
) {
    Button(
        onClick = onAttack,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935), // Red
            disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.5f) // Dimmed red when disabled
        ),
        shape = RoundedCornerShape(8.dp) // Consistent shape
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.CallMade,
            contentDescription = "Attack",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text( // <<-- ADDED TEXT
            text = "Attack",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Button for using a specific ability.
 */
@Composable
fun AbilityButton(
    ability: AbilityInPlay,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val buttonColor = when {
        !ability.isReady -> Color.Gray // Cooldown
        !enabled -> Color(0xFF3F51B5).copy(alpha = 0.5f) // Cannot afford or not turn
        else -> Color(0xFF3F51B5) // Ready and affordable
    }

    Button(
        onClick = onClick,
        enabled = enabled, // Button is enabled if logic allows
        modifier = Modifier
            .width(100.dp)
            .height(55.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor // Use the calculated color even when disabled by parent logic
        ),
        shape = RoundedCornerShape(8.dp) // Consistent shape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ability name and description
            Column(
                modifier = Modifier.padding(2.dp)
            ) {
                Text( // <<-- TEXT was already here
                    text = ability.ability.name,
                    modifier.fillMaxSize(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text( // <<-- TEXT was already here
                    text = ability.ability.description.take(30) + if (ability.ability.description.length > 30) "..." else "",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Energy cost
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF1565C0), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text( // <<-- TEXT was already here
                    text = "${ability.ability.energyCost}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Cooldown indicator (if on cooldown)
            if (!ability.isReady) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF424242), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text( // <<-- TEXT was already here
                        text = "${ability.currentCooldown}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Button to end the player's turn.
 */
@Composable
fun EndTurnButton(
    enabled: Boolean,
    onEndTurn: () -> Unit
) {
    Button(
        onClick = onEndTurn,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50), // Green
            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.5f) // Dimmed green when disabled
        ),
        shape = RoundedCornerShape(8.dp) // Consistent shape
    ) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = "End Turn",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text( // <<-- ADDED TEXT
            text = "End Turn",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Energy display showing current player energy.
 */
@Composable
fun EnergyDisplay(
    currentEnergy: Int,
    maxEnergy: Int
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp) // Reduced vertical padding
            .background(Color(0xFF1565C0).copy(alpha = 0.3f), RoundedCornerShape(8.dp)) // Use theme color and shape
            .border(1.dp, Color(0xFF1565C0), RoundedCornerShape(8.dp)) // Add border
            .padding(horizontal = 12.dp, vertical = 8.dp), // Adjust padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Energy",
            tint = Color(0xFF00FFFF), // Neon Cyan for energy icon
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$currentEnergy / $maxEnergy", // Simplified text
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp // Slightly larger font
        )
    }
}

/**
 * Target selector for abilities that require a target.
 */
@Composable
fun TargetSelector(
    targetType: TargetType,
    availableTargets: List<CardInPlay>,
    onTargetSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A).copy(alpha = 0.95f), RoundedCornerShape(12.dp)) // Darker background, rounder corners
            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp)) // Red border indicating action needed
            .padding(16.dp)
    ) {
        Text(
            text = "Select Target",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        if (availableTargets.isEmpty()) {
            Text(
                text = "No valid targets available",
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // Use column for vertical list
                availableTargets.forEach { target ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF303030), RoundedCornerShape(8.dp))
                            .clickable { onTargetSelected(target.card.id) } // Use card ID
                            .padding(12.dp), // Increased padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Space out elements
                    ) {
                        Text(
                            text = target.card.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp // Larger font
                            // modifier = Modifier.weight(1f) // Removed weight to allow spacing
                        )
                        Text(
                            text = "HP: ${target.currentHealth}/${target.card.health}", // Show current health
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp // Larger font
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // More space before cancel

        Button(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterHorizontally), // Center cancel button
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF616161) // Grey color for cancel
            ),
            shape = RoundedCornerShape(8.dp) // Consistent shape
        ) {
            Text(
                text = "Cancel Selection", // Clearer text
                color = Color.White
            )
        }
    }
}