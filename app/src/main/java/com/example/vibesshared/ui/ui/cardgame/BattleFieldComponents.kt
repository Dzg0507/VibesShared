package com.example.vibesshared.ui.ui.cardgame

// File: BattleFieldComponents.kt

// --- Imports ---
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Make sure CardComponent, CardBackView, CardDisplayMode, CardTier, ThemedBackground,
// CardInPlay, MultiverseCard are imported or defined correctly

/**
 * Composables defining the layout structure of the battle screen.
 */

// --- Data classes ---
data class PlayerAreaState(
    val activeCard: CardInPlay?,
    val hand: List<MultiverseCard>,
    val onCardPlayed: (String) -> Unit
)

data class OpponentAreaState(
    val activeCard: CardInPlay?,
    val hand: List<MultiverseCard>,
    val handSize: Int
)

/**
 * Main battle field layout composable.
 */
@Composable
fun BattleField(
    playerState: PlayerAreaState,
    opponentState: OpponentAreaState,
    isPlayerTurn: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ThemedBackground( // Assumed defined/imported
            themeColors = listOf(
                Color(0xFF0D0221), // Very dark purple
                Color(0xFF1A237E)  // Dark blue
            ),
            modifier = Modifier.fillMaxSize() // Background fills the Box
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Opponent area (top)
            Box(modifier = Modifier.weight(1f)) {
                OpponentArea(
                    state = opponentState,
                    isOpponentTurn = !isPlayerTurn,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Center - Turn indicator
            TurnIndicator(
                isPlayerTurn = isPlayerTurn,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                turnText = if (isPlayerTurn) "Your Turn" else "Opponent's Turn"
            )

            // Player area (bottom)
            Box(modifier = Modifier.weight(1f)) {
                PlayerArea(
                    state = playerState,
                    isPlayerTurn = isPlayerTurn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


/**
 * Player area composable including active card slot and hand.
 */
@Composable
fun PlayerArea(
    state: PlayerAreaState,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        ActiveCardSlot( // Use modified ActiveCardSlot
            card = state.activeCard,
            isPlayer = true,
            isActiveTurn = isPlayerTurn,
            modifier = Modifier
                .fillMaxWidth()
                // Keep explicit height for the slot area if desired, or let content drive it
                .height(250.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlayerHandDisplay( // Assumed defined/imported
            cards = state.hand,
            onCardPlayed = state.onCardPlayed
        )
    }
}

/**
 * Opponent area composable including active card slot and hand.
 */
@Composable
fun OpponentArea(
    state: OpponentAreaState,
    isOpponentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        OpponentHandDisplay(handSize = state.handSize) // Assumed defined/imported
        Spacer(modifier = Modifier.height(8.dp))
        ActiveCardSlot( // Use modified ActiveCardSlot
            card = state.activeCard,
            isPlayer = false,
            isActiveTurn = isOpponentTurn,
            modifier = Modifier
                .fillMaxWidth()
                // Keep explicit height for the slot area if desired
                .height(250.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// --- MODIFIED ActiveCardSlot (V10) ---
/**
 * Slot for the currently active card in battle.
 * Ensures consistent sizing between empty/card states.
 */
@Composable
fun ActiveCardSlot(
    card: CardInPlay?,
    isPlayer: Boolean,
    isActiveTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isPlayer) Color(0xFF4CAF50) else Color(0xFFE53935)

    // Animation for card scale based on whose turn it is
    val targetScale = if (isActiveTurn) 1.0f else 0.8f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 500),
        label = "Card Scale Animation"
    )

    // Define the size consistently for both card and placeholder content area
    // Use the smaller card size from previous attempts (V7/V8/V9)
    val contentWidth = 95.dp
    val contentHeight = 135.dp

    Box(
        modifier = modifier // This is the overall slot area (e.g., height(250.dp))
    ) {
        // Outer container with border/background, filling the slot area
        Box(
            modifier = Modifier
                .fillMaxSize() // Fills the parent Box (the one with height(250.dp))
                .border(
                    width = if (isActiveTurn) 4.dp else 2.dp,
                    color = borderColor.copy(alpha = if (isActiveTurn) 1f else 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center // Center the content (Card or Placeholder)
        ) {
            // --- Consistent Sizing Logic ---
            if (card != null) {
                // Inner Box for scaling and applying consistent size
                Box(
                    modifier = Modifier
                        .scale(animatedScale)
                        // Apply the consistent size here
                        .size(width = contentWidth, height = contentHeight)
                ) {
                    CardComponent( // Ensure CardComponent is imported/defined
                        card = card.card,
                        displayMode = CardDisplayMode.BOARD,
                        onClick = { /* No action needed */ },
                        isSelected = isActiveTurn, // Used by CardComponent?
                        modifier = Modifier.fillMaxSize(), // Card fills this inner sized box
                        cardInPlay = card
                    )
                }

                // Active turn glow effect (Rendered on top of the Card Box)
                if (isActiveTurn) {
                    Box(
                        modifier = Modifier
                            // Match the size of the inner content box
                            .size(width = contentWidth, height = contentHeight)
                            .border(
                                width = 2.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        borderColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp) // Match card corner?
                            )
                    )
                }
            } else {
                // Placeholder content centered within the outer Box
                // Wrap Text in a Box with the *same size* as the card content box
                Box(
                    modifier = Modifier.size(width = contentWidth, height = contentHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isActiveTurn) "Play a Card" else "No Card",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // --- End Consistent Sizing Logic ---
        }
    }
}


/**
 * Display for the player's hand of cards.
 */
@Composable
fun PlayerHandDisplay(
    cards: List<MultiverseCard>,
    selectedCardId: String? = null, // Retained from original file if needed
    onCardPlayed: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) // Height from original file
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cards.isEmpty()) {
            Text(
                text = "Your hand is empty",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cards, key = { it.id }) { card ->
                    CardComponent( // Ensure CardComponent is imported/defined
                        card = card,
                        displayMode = CardDisplayMode.HAND,
                        onClick = { onCardPlayed(card.id) },
                        isSelected = card.id == selectedCardId, // Use parameter
                        modifier = Modifier
                            .width(100.dp) // Size from original file
                            .height(140.dp),
                        cardInPlay = null // Hand cards don't have CardInPlay state
                    )
                }
            }
        }
    }
}

/**
 * Display for the opponent's hand (cards are face down).
 */
@Composable
fun OpponentHandDisplay(handSize: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(60.dp), // Height from original file
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        if (handSize == 0) {
            // Optionally show text if hand is empty
            Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Opponent hand empty", color = Color.White.copy(alpha=0.5f), fontSize = 12.sp)
            }
        } else {
            // Show card backs
            for (i in 0 until handSize.coerceAtMost(7)) { // Limit from original file
                CardBackView( // Use the CardBackView composable, assumed imported/defined
                    tier = CardTier.COMMON, // Or some generic back
                    modifier = Modifier
                        .width(40.dp) // Size from original file
                        .height(56.dp)
                )
            }
            if (handSize > 7) {
                Text(
                    text = "+${handSize - 7}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

/**
 * Turn indicator showing whose turn it is.
 */
@Composable
fun TurnIndicator(isPlayerTurn: Boolean, modifier: Modifier = Modifier, turnText: String) {
    val bgColor = if (isPlayerTurn) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color(0xFFE53935).copy(alpha = 0.8f)
    val icon = if (isPlayerTurn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    // Re-introduce scaling animation if desired, or keep static
    // val scale by animateFloatAsState(
    //     targetValue = 1.0f, // Static scale
    //     // targetValue = if (isPlayerTurn) 1.1f else 1.0f // Example pulse effect
    //     animationSpec = tween(durationMillis = 500),
    //     label = "Turn Indicator Scale"
    // )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                // .scale(scale) // Apply scale if animation is used
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Turn direction",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = turnText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Background for the battle field with visual effects.
 */
@Composable
fun BattleFieldBackground(modifier: Modifier = Modifier) {
    ThemedBackground( // Assumed defined/imported
        themeColors = listOf(
            Color(0xFF0D0221),
            Color(0xFF1A237E)
        ),
        modifier = modifier
    )
}