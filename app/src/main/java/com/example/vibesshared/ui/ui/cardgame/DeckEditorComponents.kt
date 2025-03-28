package com.example.vibesshared.ui.ui.cardgame

// File: DeckEditorComponents.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
 * Composables for the deck building/editing interface.
 */

/**
 * Main deck editor screen composable.
 *
 * @param availableCards List<MultiverseCard> cards available to add to deck
 * @param currentDeckCardIds List<String> card IDs currently in the deck
 * @param selectedCardId String? currently selected card ID for details
 * @param deckName String current name of the deck
 * @param onCardAdd (String) -> Unit callback when card is added to deck
 * @param onCardRemove (String) -> Unit callback when card is removed from deck
 * @param onCardSelect (String) -> Unit callback when card is selected for details
 * @param onSaveDeck () -> Unit callback when deck is saved
 * @param onNameChange (String) -> Unit callback when deck name is changed
 */
@Composable
fun DeckEditor(
    availableCards: List<MultiverseCard>,
    currentDeckCardIds: List<String>,
    selectedCardId: String?,
    deckName: String,
    onCardAdd: (String) -> Unit,
    onCardRemove: (String) -> Unit,
    onCardSelect: (String) -> Unit,
    onSaveDeck: () -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        // Deck name input and save button
        DeckNameInput(
            deckName = deckName,
            onNameChange = onNameChange,
            onSaveDeck = onSaveDeck,
            modifier = Modifier.fillMaxWidth()
        )

        // Deck stats
        DeckStatsDisplay(
            currentDeckSize = currentDeckCardIds.size,
            minDeckSize = CardDeck.MIN_CARDS,
            maxDeckSize = CardDeck.MAX_CARDS,
            modifier = Modifier.fillMaxWidth()
        )

        // Main content - available cards grid and current deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Available cards grid
            CardGrid(
                cards = availableCards,
                currentDeckCardIds = currentDeckCardIds,
                selectedCardId = selectedCardId,
                onCardSelect = onCardSelect,
                onCardAdd = onCardAdd,
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            )

            // Current deck list
            DeckListDisplay(
                cards = availableCards.filter { it.id in currentDeckCardIds },
                selectedCardId = selectedCardId,
                onCardSelect = onCardSelect,
                onCardRemove = onCardRemove,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            )
        }

        // Save button at bottom
        SaveDeckButton(
            isValid = currentDeckCardIds.size >= CardDeck.MIN_CARDS,
            onSaveDeck = onSaveDeck,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Grid display of available cards.
 *
 * @param cards List<MultiverseCard> available cards
 * @param currentDeckCardIds List<String> card IDs already in deck
 * @param selectedCardId String? currently selected card ID
 * @param onCardSelect (String) -> Unit callback when card is selected
 * @param onCardAdd (String) -> Unit callback when card is added to deck
 * @param modifier Modifier additional styling
 */
@Composable
fun CardGrid(
    cards: List<MultiverseCard>,
    currentDeckCardIds: List<String>,
    selectedCardId: String?,
    onCardSelect: (String) -> Unit,
    onCardAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Available Cards",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                CardGridItem(
                    card = card,
                    isSelected = card.id == selectedCardId,
                    isInDeck = card.id in currentDeckCardIds,
                    onCardSelect = { onCardSelect(card.id) },
                    onCardAdd = { onCardAdd(card.id) }
                )
            }
        }
    }
}

/**
 * Individual card item in the grid.
 *
 * @param card MultiverseCard the card to display
 * @param isSelected Boolean whether the card is selected
 * @param isInDeck Boolean whether the card is already in the deck
 * @param onCardSelect () -> Unit callback when card is selected
 * @param onCardAdd () -> Unit callback when card is added to deck
 */
@Composable
fun CardGridItem(
    card: MultiverseCard,
    isSelected: Boolean,
    isInDeck: Boolean,
    onCardSelect: () -> Unit,
    onCardAdd: () -> Unit
) {
    Box(
        modifier = Modifier.aspectRatio(0.7f)
    ) {
        // Card component
        CardComponent(
            card = card,
            onClick = onCardSelect,
            isSelected = isSelected,
        )

        // Add button overlay
        if (!isInDeck) {
            IconButton(
                onClick = onCardAdd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Deck",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Display of cards currently in the deck.
 *
 * @param cards List<MultiverseCard> cards in the deck
 * @param selectedCardId String? currently selected card ID
 * @param onCardSelect (String) -> Unit callback when card is selected
 * @param onCardRemove (String) -> Unit callback when card is removed from deck
 * @param modifier Modifier additional styling
 */
@Composable
fun DeckListDisplay(
    cards: List<MultiverseCard>,
    selectedCardId: String?,
    onCardSelect: (String) -> Unit,
    onCardRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Current Deck",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cards in deck yet. Select cards from the left to add them.",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cards.forEach { card ->
                    DeckCardItem(
                        card = card,
                        isSelected = card.id == selectedCardId,
                        onCardSelect = { onCardSelect(card.id) },
                        onCardRemove = { onCardRemove(card.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual card item in the deck list.
 *
 * @param card MultiverseCard the card
 * @param isSelected Boolean whether the card is selected
 * @param onCardSelect () -> Unit callback when card is selected
 * @param onCardRemove () -> Unit callback when card is removed
 */
@Composable
fun DeckCardItem(
    card: MultiverseCard,
    isSelected: Boolean,
    onCardSelect: () -> Unit,
    onCardRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                if (isSelected) Color(0xFF3F51B5) else Color(0xFF424242),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card info
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onCardSelect),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = card.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = "${card.type} - ${card.tier}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        // Card stats summary
        Text(
            text = card.getStatsSummary(),
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Remove button
        IconButton(
            onClick = onCardRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove from Deck",
                tint = Color.White
            )
        }
    }
}

/**
 * Deck statistics display.
 *
 * @param currentDeckSize Int current number of cards in the deck
 * @param minDeckSize Int minimum required cards
 * @param maxDeckSize Int maximum allowed cards
 * @param modifier Modifier additional styling
 */
@Composable
fun DeckStatsDisplay(
    currentDeckSize: Int,
    minDeckSize: Int,
    maxDeckSize: Int,
    modifier: Modifier = Modifier
) {
    val isValid = currentDeckSize in minDeckSize..maxDeckSize

    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .background(
                if (isValid) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE53935).copy(alpha = 0.2f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Deck Size: $currentDeckSize / $maxDeckSize",
            color = if (isValid) Color.White else Color(0xFFE53935),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!isValid) {
            Text(
                text = if (currentDeckSize < minDeckSize)
                    "Add ${minDeckSize - currentDeckSize} more cards"
                else
                    "Remove ${currentDeckSize - maxDeckSize} cards",
                color = Color(0xFFE53935)
            )
        }
    }
}

/**
 * Deck name input field.
 *
 * @param deckName String current deck name
 * @param onNameChange (String) -> Unit callback when name changes
 * @param onSaveDeck () -> Unit callback to save deck
 * @param modifier Modifier additional styling
 */
@Composable
fun DeckNameInput(
    deckName: String,
    onNameChange: (String) -> Unit,
    onSaveDeck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = deckName,
            onValueChange = onNameChange,
            label = { Text("Deck Name") },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFF4CAF50),
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onSaveDeck,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Deck",
                tint = Color.White
            )
        }
    }
}

/**
 * Save deck button.
 *
 * @param isValid Boolean whether the deck is valid for saving
 * @param onSaveDeck () -> Unit callback to save deck
 * @param modifier Modifier additional styling
 */
@Composable
fun SaveDeckButton(
    isValid: Boolean,
    onSaveDeck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSaveDeck,
        enabled = isValid,
        modifier = modifier
            .padding(vertical = 16.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),         // Changed from backgroundColor
            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f)  // Changed from disabledBackgroundColor
        )
    ) {
        // Button content
    }
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Save Deck",
            color = Color.White,
            fontSize =

                16.sp,
            fontWeight = FontWeight.Bold
        )
    }
