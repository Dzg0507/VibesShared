package com.example.vibesshared.ui.ui.cardgame

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen for displaying detailed information about a card and actions that can be performed with it.
 *
 * @param cardId String The ID of the card to display
 * @param viewModel CardCollectionViewModel The view model for card collection
 * @param onNavigateBack () -> Unit Callback to navigate back
 */
@Composable
fun CardDetailsScreen(
    cardId: String,
    viewModel: CardCollectionViewModel,
    onNavigateBack: () -> Unit
) {
    // Ensure the card is loaded
    LaunchedEffect(cardId) {
        viewModel.selectCard(cardId)
    }

    // Collect state
    val card by viewModel.selectedCardDetails.collectAsState()
    val playerDecks by viewModel.playerDecks.collectAsState()

    // Local UI state
    var showAddToDeckDialog by remember { mutableStateOf(false) }

    // Background
    ThemedBackground(
        themeColors = listOf(
            Color(0xFF1A237E), // Dark blue
            Color(0xFF0D0221)  // Very dark purple
        )
    )

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen header
        ScreenHeader(
            title = "Card Details",
            showBackButton = true,
            onBackClick = onNavigateBack
        )

        // Card content
        if (card == null) {
            // Loading or card not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Card not found",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        } else {
            // Card details
            CardDetails(
                card = card!!,
                onAddToDeck = { showAddToDeckDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(top = 16.dp)
            )
        }
    }

    // Add to deck dialog
    if (showAddToDeckDialog && card != null) {
        AddToDeckDialog(
            cardId = card!!.id,
            decks = playerDecks,
            onAddToDeck = { _ -> /* Add card to deck */ },
            onDismiss = { showAddToDeckDialog = false }
        )
    }
}

/**
 * Component for displaying detailed card information.
 *
 * @param card MultiverseCard The card to display
 * @param onAddToDeck () -> Unit Callback when "Add to Deck" button is clicked
 * @param modifier Modifier Additional styling
 */
@Composable
private fun CardDetails(
    modifier: Modifier = Modifier,
    card: MultiverseCard,
    onAddToDeck: () -> Unit,
    selectedCard: MultiverseCard? = null,

) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        // Card visual display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CardComponent(
                card = card,
                displayMode = CardDisplayMode.FULL,
                isSelected = card.id == selectedCard?.id,
            )
        }

        // Card info sections
        CardBasicInfo(
            card = card,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CardStatsSection(
            card = card,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card description
        CardDescriptionSection(
            card = card,
            modifier = Modifier.fillMaxWidth()
        )

        // Card abilities (if any)
        if (card.abilities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            CardAbilitiesSection(
                abilities = card.abilities,
                modifier = Modifier.fillMaxWidth()
            )
        }



        Spacer(modifier = Modifier.height(24.dp))

        // Actions buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add to deck button
            Button(
                onClick = onAddToDeck,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Add to Deck",
                    color = Color.White
                )
            }
        }
    }
}


/**
 * Section displaying basic card information.
 *
 * @param card MultiverseCard The card
 * @param modifier Modifier Additional styling
 */
@Composable
private fun CardBasicInfo(
    card: MultiverseCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Card name and tier
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Level ${card.level} ${card.type} Card",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }

            // Tier stars
            Row {
                repeat(card.tier.ordinal + 1) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = getColorForCardTier(card.tier),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Source and value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Source badge
            Box(
                modifier = Modifier
                    .background(
                        color = getColorForCardSource(card.source).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = getColorForCardSource(card.source).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = formatCardSource(card.source),
                    color = getColorForCardSource(card.source),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Token value
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Toll,
                    contentDescription = "Token Value",
                    tint = Color(0xFFFFD700), // Gold
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${card.tokenValue}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Section displaying card stats.
 *
 * @param card MultiverseCard The card
 * @param modifier Modifier Additional styling
 */
@Composable
private fun CardStatsSection(
    card: MultiverseCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Card Stats",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats bars
        StatBar(
            label = "Power",
            value = card.power,
            maxValue = 15,
            color = Color(0xFFE53935) // Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatBar(
            label = "Defense",
            value = card.defense,
            maxValue = 15,
            color = Color(0xFF3F51B5) // Indigo
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatBar(
            label = "Health",
            value = card.health,
            maxValue = 25,
            color = Color(0xFF4CAF50) // Green
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatBar(
            label = "Energy",
            value = card.energy,
            maxValue = 10,
            color = Color(0xFF03A9F4) // Light Blue
        )
    }
}

/**
 * Individual stat bar.
 *
 * @param label String The stat label
 * @param value Int The stat value
 * @param maxValue Int The maximum reference value for scaling
 * @param color Color The color for the stat
 */
@Composable
private fun StatBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Label and value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )

            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * Section displaying card description.
 *
 * @param card MultiverseCard The card
 * @param modifier Modifier Additional styling
 */
@Composable
private fun CardDescriptionSection(
    card: MultiverseCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Description",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = card.description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

/**
 * Section displaying card abilities.
 *
 * @param abilities List<CardAbility> The card abilities
 * @param modifier Modifier Additional styling
 */
@Composable
private fun CardAbilitiesSection(
    abilities: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Abilities",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        abilities.forEachIndexed { index, ability ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.2f)
                )
            }

            Text(
                text = ability.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ability.toString(),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}



/**
 * Dialog for adding a card to a deck.
 *
 * @param cardId String ID of the card to add
 * @param decks List<CardDeck> Available decks
 * @param onAddToDeck (String) -> Unit Callback when a deck is selected
 * @param onDismiss () -> Unit Callback to dismiss the dialog
 */
@Composable
private fun AddToDeckDialog(
    cardId: String,
    decks: List<CardDeck>,
    onAddToDeck: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Add to Deck",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deck list
            if (decks.isEmpty()) {
                Text(
                    text = "You don't have any decks yet. Create a deck first.",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    decks.forEach { deck ->
                        DeckSelectItem(
                            deck = deck,
                            onSelect = {
                                onAddToDeck(deck.id)
                                onDismiss()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create new deck button
            Button(
                onClick = { /* Navigate to create deck screen */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5) // Indigo
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Create New Deck",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(text = "Cancel")
            }
        }
    }
}

/**
 * Item for selecting a deck in the add to deck dialog.
 *
 * @param deck CardDeck The deck
 * @param onSelect () -> Unit Callback when selected
 */
@Composable
private fun DeckSelectItem(
    deck: CardDeck,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF424242), RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Deck icon/theme indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(getColorForDeckTheme(deck.theme), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Deck info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deck.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${deck.cardIds.size} cards",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        // Add icon
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add to this deck",
            tint = Color.White
        )
    }
}

/**
 * Helper function to get color for card tier.
 *
 * @param tier CardTier The card tier
 * @return Color corresponding to the tier
 */
private fun getColorForCardTier(tier: CardTier): Color {
    return when (tier) {
        CardTier.COMMON -> Color(0xFF9E9E9E)     // Grey
        CardTier.RARE -> Color(0xFF00BCD4)       // Cyan
        CardTier.EPIC -> Color(0xFF9C27B0)       // Purple
        CardTier.LEGENDARY -> Color(0xFFFFD700)  // Gold
        CardTier.MYTHIC -> Color(0xFFE91E63)     // Pink
        CardTier.UNCOMMON -> Color(0xFF8BC34A)  // Light Green
    }
}

/**
 * Helper function to get color for card source.
 *
 * @param source CardSource The card source
 * @return Color corresponding to the source
 */
private fun getColorForCardSource(source: CardSource): Color {
    return when (source) {
        CardSource.STARTER -> Color(0xFF8BC34A)    // Light Green
        CardSource.SHOP -> Color(0xFF03A9F4)       // Light Blue
        CardSource.REWARD -> Color(0xFFFFD700)     // Gold
        CardSource.FORGED -> Color(0xFFFF9800)     // Orange
        CardSource.EVENT -> Color(0xFF9C27B0)      // Purple
        CardSource.QUEST -> Color(0xFF673AB7)      // Deep Purple
        CardSource.ACHIEVEMENT -> Color(0xFF4CAF50) // Green
    }
}

/**
 * Format card source for display.
 *
 * @param source CardSource The card source
 * @return Formatted string representation
 */
fun formatCardSource(source: CardSource): String {
    return when (source) {
        CardSource.STARTER -> "Starter Deck"
        CardSource.SHOP -> "Shop Purchase"
        CardSource.REWARD -> "Battle Reward"
        CardSource.FORGED -> "Forge Creation"
        CardSource.EVENT -> "Special Event"
        CardSource.QUEST -> "Quest Reward"
        CardSource.ACHIEVEMENT -> "Achievement"
    }
}