package com.example.vibesshared.ui.ui.cardgame

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

/**
 * Enhanced Bottom Bar component that includes player hand and actions.
 * This component is designed to be more space-efficient and user-friendly.
 */
@Composable
fun EnhancedBottomBar(
    playerHand: List<MultiverseCard>,
    isPlayerTurn: Boolean,
    activePlayerCard: CardInPlay?,
    currentEnergy: Int,
    canPlayCard: (MultiverseCard) -> Boolean,
    onCardPlayed: (String) -> Unit,
    onAttack: () -> Unit,
    onEndTurn: () -> Unit,
    onUseAbility: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAbilitiesDialog by remember { mutableStateOf(false) }
    var showFullHandDialog by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Rotation Animation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000).copy(alpha = 0.3f),
                        Color(0xFF000000).copy(alpha = 0.9f)
                    )
                )
            )
            .animateContentSize()
    ) {
        // Toggle bar for expanding/collapsing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropUp,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color.White,
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        // Primary actions (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attack Button
            ActionButton(
                text = "Attack",
                enabled = isPlayerTurn && activePlayerCard != null && !activePlayerCard.isStunned(),
                onClick = onAttack,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(end = 8.dp, start = 8.dp)
            )

            // Energy Display
            EnergyDisplay(
                currentEnergy = currentEnergy,
                maxEnergy = 10 // Assuming max energy is 10, adjust as needed
            )

            // End Turn Button
            ActionButton(
                text = "End Turn",
                enabled = isPlayerTurn,
                onClick = onEndTurn,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(end = 8.dp, start = 8.dp)
            )
        }

        // Expanded content
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Abilities section (if card has abilities)
                if (activePlayerCard != null && activePlayerCard.abilities.isNotEmpty()) {
                    Text(
                        text = "Abilities",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    activePlayerCard.abilities.take(2).forEachIndexed { index, abilityInPlay ->
                        EnhancedAbilityButton(
                            ability = abilityInPlay,
                            enabled = isPlayerTurn && !activePlayerCard.isStunned() && abilityInPlay.isReady,
                            onClick = { onUseAbility(index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (activePlayerCard.abilities.size > 2) {
                        Button(
                            onClick = { showAbilitiesDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3F51B5)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text("More Abilities (${activePlayerCard.abilities.size - 2})")
                        }
                    }
                }

                // Player Hand Preview
                Text(
                    text = "Your Hand (${playerHand.size})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (playerHand.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color(0xFF424242).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your hand is empty",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Hand cards preview
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        items(playerHand) { card ->
                            CardComponent(
                                card = card,
                                displayMode = CardDisplayMode.HAND,
                                onClick = { onCardPlayed(card.id) },
                                isSelected = false,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(120.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // View Full Hand button
                    Button(
                        onClick = { showFullHandDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF424242)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Full Hand")
                    }
                }
            }
        }
    }

    // Full Hand Dialog
    if (showFullHandDialog) {
        FullHandDialog(
            cards = playerHand,
            canPlayCard = canPlayCard,
            onCardPlayed = { cardId ->
                onCardPlayed(cardId)
                showFullHandDialog = false
            },
            onDismiss = { showFullHandDialog = false }
        )
    }

    // All Abilities Dialog
    if (showAbilitiesDialog && activePlayerCard != null) {
        AllAbilitiesDialog(
            abilities = activePlayerCard.abilities,
            canUseAbility = { index ->
                isPlayerTurn &&
                        !activePlayerCard.isStunned() &&
                        activePlayerCard.abilities.getOrNull(index)?.isReady == true
            },
            onUseAbility = { index ->
                onUseAbility(index)
                showAbilitiesDialog = false
            },
            onDismiss = { showAbilitiesDialog = false }
        )
    }
}

/**
 * Enhanced ability button with better visual design
 */
@Composable
private fun EnhancedAbilityButton(
    ability: AbilityInPlay,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = when {
        !ability.isReady -> Color(0xFF424242)
        !enabled -> Color(0xFF3F51B5).copy(alpha = 0.5f)
        else -> Color(0xFF3F51B5)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ability name and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ability.ability.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ability.ability.description.take(30) +
                            if (ability.ability.description.length > 30) "..." else "",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Energy cost
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF1565C0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ability.ability.energyCost}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Cooldown indicator
                if (!ability.isReady) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF424242), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
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
}

/**
 * Action button with improved styling
 */
@Composable
fun ActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = Modifier.width(100.dp).height(55.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * Full hand dialog for detailed card selection
 */
@Composable
private fun FullHandDialog(
    cards: List<MultiverseCard>,
    canPlayCard: (MultiverseCard) -> Boolean,
    onCardPlayed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF212121)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Hand",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f))

                // Card list
                if (cards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your hand is empty",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        cards.forEach { card ->
                            val playable = canPlayCard(card)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (playable) Color(0xFF1A237E).copy(alpha = 0.3f)
                                        else Color(0xFF424242).copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (playable) Color(0xFF3F51B5) else Color(0xFF424242),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = playable) { onCardPlayed(card.id) }
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Card component
                                    CardComponent(
                                        card = card,
                                        displayMode = CardDisplayMode.HAND,
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(120.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Card details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = card.name,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${card.type} • ${card.tier}",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            StatDisplay(value = card.power, label = "POWER", color = Color(0xFFD32F2F))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            StatDisplay(value = card.defense, label = "DEFENSE", color = Color(0xFF1976D2))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            StatDisplay(value = card.energy, label = "ENERGY", color = Color(0xFF43A047))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = card.description,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stat display component for card stats
 */
@Composable
private fun StatDisplay(
    value: Int,
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

/**
 * Dialog displaying all abilities for the active card
 */
@Composable
private fun AllAbilitiesDialog(
    abilities: List<AbilityInPlay>,
    canUseAbility: (Int) -> Boolean,
    onUseAbility: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF212121)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Abilities",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f))

                // Abilities list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    abilities.forEachIndexed { index, ability ->
                        AbilityDetailItem(
                            ability = ability,
                            enabled = canUseAbility(index),
                            onClick = { onUseAbility(index) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed ability item for the abilities dialog
 */
@Composable
private fun AbilityDetailItem(
    ability: AbilityInPlay,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val buttonColor = when {
        !ability.isReady -> Color(0xFF424242)
        !enabled -> Color(0xFF3F51B5).copy(alpha = 0.5f)
        else -> Color(0xFF3F51B5)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled && ability.isReady) Color(0xFF1A237E).copy(alpha = 0.3f)
                else Color(0xFF424242).copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (enabled && ability.isReady) Color(0xFF3F51B5) else Color(0xFF424242),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled && ability.isReady) { onClick() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with name and cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ability.ability.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Energy cost
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF1565C0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ability.ability.energyCost}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Cooldown indicator
                    if (!ability.isReady) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF424242), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${ability.currentCooldown}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = ability.ability.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Target and cooldown info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target: ${ability.ability.targetType.name.lowercase().capitalize(Locale.getDefault())}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                Text(
                    text = "Cooldown: ${ability.ability.cooldown}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}