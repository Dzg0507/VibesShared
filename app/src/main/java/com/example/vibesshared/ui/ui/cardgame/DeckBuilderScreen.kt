package com.example.vibesshared.ui.ui.cardgame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Deck Builder screen for viewing and managing decks.
 *
 * @param viewModel CardCollectionViewModel the view model for this screen
 * @param onNavigateToDeckEditor (String) -> Unit callback to navigate to deck editor
 * @param onNavigateBack () -> Unit callback to navigate back
 */
@Composable
fun DeckBuilderScreen(
    viewModel: CardCollectionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDeckEditor: (String) -> Unit
) {
    // Collect state from ViewModel
    val playerDecks by viewModel.playerDecks.collectAsState()
    val activeDeckId by viewModel.cardRepository.activeDeckId.collectAsState(initial = null)

    // Local UI state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<CardDeck?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen header with back button
        ScreenHeader(
            title = "Deck Builder",
            showBackButton = true,
            onBackClick = onNavigateBack
        )

        // Create new deck button
        CreateNewDeckButton(
            onClick = {
                // Navigate to deck editor with new deck ID
                onNavigateToDeckEditor("new_deck_${System.currentTimeMillis()}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Deck list
        if (playerDecks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You haven't created any decks yet.\nCreate your first deck to start playing!",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Deck list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playerDecks) { deck ->
                    DeckItem(
                        deck = deck,
                        isActive = deck.id == activeDeckId,
                        onSetActive = { viewModel.setActiveDeck(deck.id) },
                        onEdit = { onNavigateToDeckEditor(deck.id) },
                        onDelete = {
                            deckToDelete = deck
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && deckToDelete != null) {
        DeleteDeckConfirmationDialog(
            deckName = deckToDelete!!.name,
            onConfirm = {
                viewModel.cardRepository.deleteDeck(deckToDelete!!.id)
                showDeleteConfirmDialog = false
                deckToDelete = null
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                deckToDelete = null
            }
        )
    }
}

// Rest of the file remains the same as in the previous versions...


private fun ColumnScope.onNavigateToDeckEditor(string: String) {
    TODO("Not yet implemented")
}

/**
 * Button to create a new deck.
 *
 * @param onClick () -> Unit callback when button is clicked
 * @param modifier Modifier additional styling
 */
@Composable
fun CreateNewDeckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Create New Deck",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Individual deck item in the list.
 *
 * @param deck CardDeck the deck
 * @param isActive Boolean whether this is the active deck
 * @param onSetActive () -> Unit callback to set as active deck
 * @param onEdit () -> Unit callback to edit the deck
 * @param onDelete () -> Unit callback to delete the deck
 */
@Composable
fun DeckItem(
    deck: CardDeck,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Deck header with name and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deck name and card count
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = deck.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${deck.cardIds.size} Cards • ${getDeckThemeText(deck.theme)}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }

                // Action buttons
                Row {
                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Deck",
                            tint = Color.White
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Deck",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deck preview - in a real app, you might show card thumbnails here
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                    .border(1.dp, getColorForDeckTheme(deck.theme).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Deck Preview",
                    color = Color.White.copy(alpha = 0.5f)
                )

                // In a real app, you would display card previews here
                // For example:
                // Row {
                //     deck.cardIds.take(5).forEach { cardId ->
                //         // Mini card preview for each card
                //     }
                // }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Set as active button
                OutlinedButton(
                    onClick = onSetActive,
                    enabled = !isActive,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isActive) Color.Gray else Color(0xFF4CAF50)
                    )
                ) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (isActive) "Active Deck" else "Set as Active",
                        color = if (isActive) Color.Gray else Color(0xFF4CAF50)
                    )
                }

                // Play button - in a real app, this might start a battle with this deck
                Button(
                    onClick = { /* Start battle with this deck */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Play",
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Confirmation dialog for deleting a deck.
 *
 * @param deckName String name of the deck to delete
 * @param onConfirm () -> Unit callback to confirm deletion
 * @param onDismiss () -> Unit callback to dismiss the dialog
 */
@Composable
fun DeleteDeckConfirmationDialog(
    deckName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Deck",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$deckName\"? This action cannot be undone.",
                color = Color.White
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF2D2D2D),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

/**
 * Get color for deck theme.
 *
 * @param theme DeckTheme the deck theme
 * @return Color corresponding to the theme
 */
fun getColorForDeckTheme(theme: DeckTheme): Color {
    return when (theme) {
        DeckTheme.STANDARD -> Color(0xFF2196F3)       // Blue
        DeckTheme.COSMIC_ART -> Color(0xFF673AB7)     // Deep Purple
        DeckTheme.ELEMENTAL_FLAMES -> Color(0xFFFF5722) // Deep Orange
        DeckTheme.TEMPORAL_VORTEX -> Color(0xFFFFEB3B) // Yellow
        DeckTheme.QUANTUM_GRID -> Color(0xFF03A9F4)   // Light Blue
        DeckTheme.BIONIC_TECH -> Color(0xFF9E9E9E)    // Grey
        DeckTheme.SPECTRAL_MIST -> Color(0xFFE91E63)  // Pink
        DeckTheme.DIMENSIONAL_RIFT -> Color(0xFF9C27B0) // Purple
        DeckTheme.PRIMORDIAL_ANCIENT -> Color(0xFF795548) // Brown
    }
}

/**
 * Get display text for deck theme.
 *
 * @param theme DeckTheme the deck theme
 * @return String display text for the theme
 */
fun getDeckThemeText(theme: DeckTheme): String {
    return when (theme) {
        DeckTheme.STANDARD -> "Standard"
        DeckTheme.COSMIC_ART -> "Space and energy-based powers"
        DeckTheme.ELEMENTAL_FLAMES -> "Natural elements like fire, water, earth"
        DeckTheme.TEMPORAL_VORTEX -> "Time-based Activities"
        DeckTheme.QUANTUM_GRID -> "Physics and probability manipulation"
        DeckTheme.BIONIC_TECH -> "Technology and machinery"
        DeckTheme.SPECTRAL_MIST -> "Ghost and spirit-related"
        DeckTheme.DIMENSIONAL_RIFT -> "Reality and dimension shifting"
        DeckTheme.PRIMORDIAL_ANCIENT -> "Ancient and primal forces"

    }
}