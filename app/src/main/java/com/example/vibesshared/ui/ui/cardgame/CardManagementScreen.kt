package com.example.vibesshared.ui.ui.cardgame

// File: CardManagementScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Composable screen combining card collection Browse and deck editing, potentially using tabs.
 */

/**
 * Main tabs for card management screen.
 */
enum class CardManagementTab {
    COLLECTION,
    DECK_EDITOR
}

/**
 * Main composable for card management screen.
 *
 * @param viewModel CardCollectionViewModel the view model for this screen
 */
@Composable
fun CardManagementScreen(viewModel: CardCollectionViewModel) {
    // Collect state from ViewModel
    val displayedCards by viewModel.displayedCards.collectAsState()
    val selectedCardDetails by viewModel.selectedCardDetails.collectAsState()
    val playerDecks by viewModel.playerDecks.collectAsState()
    val selectedDeckForEditing by viewModel.selectedDeckForEditing.collectAsState()
    val deckEditorCardIds by viewModel.deckEditorCardIds.collectAsState()

    // Local UI state
    var currentTab by remember { mutableStateOf(CardManagementTab.COLLECTION) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }

    // Background
    ThemedBackground(
        themeColors = listOf(
            Color(0xFF1A237E), // Dark blue
            Color(0xFF0D0221)  // Very dark purple
        )
    )

    // Main content
    Column(modifier = Modifier.fillMaxSize()) {
        // Screen header
        @Composable
        fun ScreenHeader(
            title: String,
            showBackButton: Boolean = false,
            onBackClick: () -> Unit = {},
            actions: @Composable () -> Unit = {}  // Note this parameter type
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Actions
                actions()
            }
        }

                // Add create deck button for deck editor tab
                if (currentTab == CardManagementTab.DECK_EDITOR && selectedDeckForEditing == null) {
                    IconButton(onClick = {
                        newDeckName = "New Deck"
                        // Create a new deck in edit mode
                        // In a real app, this would call a method on the ViewModel
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Deck",
                            tint = Color.White
                        )
                    }
                }
            }


        // Tabs
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color(0xFF4CAF50) // Green
        ) {
            Tab(
                selected = currentTab == CardManagementTab.COLLECTION,
                onClick = { currentTab = CardManagementTab.COLLECTION },
                text = {
                    Text(
                        text = "Collection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null
                    )
                }
            )

            Tab(
                selected = currentTab == CardManagementTab.DECK_EDITOR,
                onClick = { currentTab = CardManagementTab.DECK_EDITOR },
                text = {
                    Text(
                        text = "Decks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null
                    )
                }
            )
        }

        // Tab content
        when (currentTab) {
            CardManagementTab.COLLECTION -> {
                CollectionTab(
                    cards = displayedCards,
                    selectedCard = selectedCardDetails,
                    onCardSelect = { viewModel.selectCard(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            CardManagementTab.DECK_EDITOR -> {
                if (selectedDeckForEditing != null) {
                    // Deck editor with selected deck
                    DeckEditor(
                        availableCards = displayedCards,
                        currentDeckCardIds = deckEditorCardIds,
                        selectedCardId = selectedCardDetails?.id,
                        deckName = selectedDeckForEditing?.name ?: "",
                        onCardAdd = { viewModel.addCardToEditorDeck(it) },
                        onCardRemove = { viewModel.removeCardFromEditorDeck(it) },
                        onCardSelect = { viewModel.selectCard(it) },
                        onSaveDeck = { viewModel.saveEditedDeck() },
                        onNameChange = { /* Update deck name */ },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Deck list
                    DeckListTab(
                        decks = playerDecks,
                        onDeckSelect = { viewModel.selectDeck(it) },
                        onSetActiveDeck = { viewModel.setActiveDeck(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }


    // Filter dialog


/**
 * Collection tab displaying all player cards in a grid.
 *
 * @param cards List<MultiverseCard> cards to display
 * @param selectedCard MultiverseCard? currently selected card
 * @param onCardSelect (String) -> Unit callback when card is selected
 * @param modifier Modifier additional styling
 */
@Composable
private fun CollectionTab(
    cards: List<MultiverseCard>,
    selectedCard: MultiverseCard?,
    onCardSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Card grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                CardComponent(
                    card = card,
                    onClick = { onCardSelect(card.id) },
                    isSelected = card.id == selectedCard?.id,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Card details panel
        CardDetailsPanel(
            card = selectedCard,
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
        )
    }
}

/**
 * Panel showing details for the selected card.
 *
 * @param card MultiverseCard? the card to display details for
 * @param modifier Modifier additional styling
 */
@Composable
private fun CardDetailsPanel(
    card: MultiverseCard?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (card == null) {
            // No card selected
            Text(
                text = "Select a card to view details",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            // Card details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card display
                CardComponent(
                    card = card,
                    displayMode = CardDisplayMode.FULL,
                    // Remove the TODOs and use empty functions/values
                    isSelected = false,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Additional details
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Card Details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    DetailRow(label = "Type", value = card.type.name)
                    DetailRow(label = "Tier", value = card.tier.name)
                    DetailRow(label = "Source", value = formatCardSource(card.source))
                    DetailRow(label = "Token Value", value = card.tokenValue.toString())

                }
            }
        }
    }
}

/**
 * Helper function to display a label-value pair for card details.
 *
 * @param label String the label
 * @param value String the value
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Deck list tab showing all player decks.
 *
 * @param decks List<CardDeck> decks to display
 * @param onDeckSelect (String) -> Unit callback when deck is selected for editing
 * @param onSetActiveDeck (String) -> Unit callback when deck is set as active
 * @param modifier Modifier additional styling
 */
@Composable
private fun DeckListTab(
    decks: List<CardDeck>,
    onDeckSelect: (String) -> Unit,
    onSetActiveDeck: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Your Decks",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (decks.isEmpty()) {
            // No decks message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Decks Created",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create a new deck to get started!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Create New Deck",
                        icon = Icons.Default.Add,
                        onClick = { /* Create new deck */ }
                    )
                }
            }
        } else {
            // Deck list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                decks.forEach { deck ->
                    DeckListItem(
                        deck = deck,
                        onDeckSelect = { onDeckSelect(deck.id) },
                        onSetActive = { onSetActiveDeck(deck.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual deck item in the deck list.
 *
 * @param deck CardDeck the deck
 * @param onDeckSelect () -> Unit callback when deck is selected for editing
 * @param onSetActive () -> Unit callback when deck is set as active
 */
@Composable
private fun DeckListItem(
    deck: CardDeck,
    onDeckSelect: () -> Unit,
    onSetActive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Deck icon
        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = null,
            tint = when (deck.theme) {
                DeckTheme.COSMIC_ART -> Color(0xFF3F51B5) // Indigo
                DeckTheme.ELEMENTAL_FLAMES -> Color(0xFFFF9800) // Orange
                else -> Color.White
            },
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Deck info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = deck.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${deck.cardIds.size} cards - ${deck.theme.name}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        // Action buttons
        Row {
            // Set active button
            IconButton(onClick = onSetActive) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Set Active",
                    tint = Color(0xFFFFD700) // Gold
                )
            }

            // Edit button
            IconButton(onClick = onDeckSelect) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Deck",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Dialog for filtering cards.
 *
 * @param onDismiss () -> Unit callback when dialog is dismissed
 * @param onApplyFilter (CardFilter) -> Unit callback when filter is applied
 */
@Composable
private fun CardFilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (CardFilter) -> Unit
) {
    // State for selected filter values
    var selectedType by remember { mutableStateOf<CardType?>(null) }
    var selectedTier by remember { mutableStateOf<CardTier?>(null) }
    var minPower by remember { mutableIntStateOf(0) }
    var maxEnergy by remember { mutableIntStateOf(10) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2D2D),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(IntrinsicSize.Min)
            ) {
                Text(
                    text = "Filter Cards",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card type filter
                Text(
                    text = "Card Type",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Type chips would go here
                // For brevity, showing just a few types as buttons

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        text = "Cosmic",
                        isSelected = selectedType == CardType.COSMIC,
                        onClick = {
                            selectedType = if (selectedType == CardType.COSMIC) null else CardType.COSMIC
                        }
                    )

                    FilterChip(
                        text = "Elemental",
                        isSelected = selectedType == CardType.ELEMENTAL,
                        onClick = {
                            selectedType = if (selectedType == CardType.ELEMENTAL) null else CardType.ELEMENTAL
                        }
                    )

                    FilterChip(
                        text = "Temporal",
                        isSelected = selectedType == CardType.TEMPORAL,
                        onClick = {
                            selectedType = if (selectedType == CardType.TEMPORAL) null else CardType.TEMPORAL
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card tier filter - simplified for brevity
                Text(
                    text = "Card Tier",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        text = "Common",
                        isSelected = selectedTier == CardTier.COMMON,
                        onClick = {
                            selectedTier = if (selectedTier == CardTier.COMMON) null else CardTier.COMMON
                        }
                    )

                    FilterChip(
                        text = "Rare",
                        isSelected = selectedTier == CardTier.RARE,
                        onClick = {
                            selectedTier = if (selectedTier == CardTier.RARE) null else CardTier.RARE
                        }
                    )

                    FilterChip(
                        text = "Epic",
                        isSelected = selectedTier == CardTier.EPIC,
                        onClick = {
                            selectedTier = if (selectedTier == CardTier.EPIC) null else CardTier.EPIC
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Apply selected filters
                            if (selectedType != null) {
                                onApplyFilter(CardFilter(FilterType.TYPE, selectedType.toString()))
                            }

                            if (selectedTier != null) {
                                onApplyFilter(CardFilter(FilterType.TIER, selectedTier.toString()))
                            }

                            if (minPower > 0) {
                                onApplyFilter(CardFilter(FilterType.POWER_MIN, minPower.toString()))
                            }

                            if (maxEnergy < 10) {
                                onApplyFilter(CardFilter(FilterType.ENERGY_MAX, maxEnergy.toString()))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50) // Green
                        )
                    ) {
                        Text(
                            text = "Apply Filters",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Filter chip for selecting filter options.
 *
 * @param text String chip text
 * @param isSelected Boolean whether the chip is selected
 * @param onClick () -> Unit callback when clicked
 */
@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF4CAF50) else Color.White,
            fontSize = 14.sp
        )
    }
}

/**
 * Primary button with text and icon.
 *
 * @param text String button text
 * @param icon ImageVector button icon
 * @param onClick () -> Unit callback when clicked
 * @param modifier Modifier additional styling
 */
@Composable
private fun PrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50) // Green
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}