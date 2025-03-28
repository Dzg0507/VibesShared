package com.example.vibesshared.ui.ui.cardgame

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
 * Card Collection screen that displays all cards owned by the player.
 *
 * @param viewModel CardCollectionViewModel the view model for this screen
 * @param onNavigateToDeckBuilder () -> Unit callback to navigate to deck builder
 * @param onNavigateToCardDetails (String) -> Unit callback to navigate to card details
 * @param onNavigateBack () -> Unit callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CardCollectionViewModel,
    onNavigateToDeckBuilder: () -> Unit,
    onNavigateToCardDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Collect state from ViewModel
    val playerCards by viewModel.displayedCards.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    val playerDecks by viewModel.playerDecks.collectAsState()

    // Local UI state
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen top bar with back and action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Screen title
            Text(
                text = "Card Collection",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            // Action buttons
            Row {
                // Search icon
                IconButton(onClick = { /* Toggle search bar */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }

                // Filter icon
                IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }

                // Sort icon
                IconButton(onClick = { showSortMenu = !showSortMenu }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        tint = Color.White
                    )
                }
            }
        }

        // Collection stats and deck button
        CollectionHeader(
            cardCount = playerCards.size,
            deckCount = playerDecks.size,
            onDeckButtonClick = onNavigateToDeckBuilder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Search bar (if enabled)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search cards...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF3F51B5),
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        )

        // Active filters display
        if (activeFilters.isNotEmpty()) {
            ActiveFiltersDisplay(
                filters = activeFilters,
                onFilterRemove = { viewModel.applyFilter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Card grid
        if (playerCards.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cards in your collection yet.\nVisit the shop to get your first cards!",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Card grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playerCards) { card ->
                    CollectionCardItem(
                        card = card,
                        onClick = { onNavigateToCardDetails(card.id) }
                    )
                }
            }
        }

        // Filter popup menu
        FilterMenu(
            showMenu = showFilterMenu,
            onDismiss = { showFilterMenu = false },
            onFilterSelected = { viewModel.applyFilter(it) },
            activeFilters = activeFilters
        )

        // Sort popup menu
        SortMenu(
            showMenu = showSortMenu,
            onDismiss = { showSortMenu = false },
            onSortSelected = { /* Apply sort */ }
        )
    }
}

/**
 * Collection header with stats and deck building button.
 *
 * @param cardCount Int number of cards in collection
 * @param deckCount Int number of decks created
 * @param onDeckButtonClick () -> Unit callback when deck button is clicked
 * @param modifier Modifier additional styling
 */
@Composable
fun CollectionHeader(
    cardCount: Int,
    deckCount: Int,
    onDeckButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collection stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Your Collection",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$cardCount Cards • $deckCount Decks",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        // Deck building button
        Button(
            onClick = onDeckButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3F51B5)
            )
        ) {
            Text(
                text = "Deck Builder",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Individual card item in the collection grid.
 *
 * @param card MultiverseCard the card
 * @param onClick () -> Unit callback when card is clicked
 */
@Composable
fun CollectionCardItem(
    card: MultiverseCard,
    onClick: () -> Unit,
    selectedCard: MultiverseCard? = null
) {
    Box(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Card component
        CardComponent(
            card = card,
            onClick = onClick,
            isSelected = card.id == selectedCard?.id,
        )
    }
}

/**
 * Display of active filters with remove option.
 *
 * @param filters List<CardFilter> active filters
 * @param onFilterRemove (CardFilter) -> Unit callback to remove a filter
 * @param modifier Modifier additional styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFiltersDisplay(
    filters: List<CardFilter>,
    onFilterRemove: (CardFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Filters:",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        filters.forEach { filter ->
            FilterChip(
                selected = true,
                onClick = { onFilterRemove(filter) },
                label = {
                    Text(
                        text = formatFilterText(filter),
                        fontSize = 12.sp
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Filter",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

/**
 * Filter selection menu.
 *
 * @param showMenu Boolean whether to show the menu
 * @param onDismiss () -> Unit callback when menu is dismissed
 * @param onFilterSelected (CardFilter) -> Unit callback when filter is selected
 * @param activeFilters List<CardFilter> currently active filters
 */
@Composable
fun FilterMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onFilterSelected: (CardFilter) -> Unit,
    activeFilters: List<CardFilter>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .background(Color(0xFF2D2D2D))
                .width(250.dp)
        ) {
            Text(
                text = "Filter By Type",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Type filters
            CardType.entries.forEach { type ->
                val isActive = activeFilters.any { it.type == FilterType.TYPE && it.value == type.toString() }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.toString(),
                            color = if (isActive) Color(0xFF3F51B5) else Color.White
                        )
                    },
                    onClick = {
                        onFilterSelected(CardFilter(FilterType.TYPE, type.toString()))
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Filter By Rarity",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Rarity filters
            CardTier.entries.forEach { tier ->
                val isActive = activeFilters.any { it.type == FilterType.TIER && it.value == tier.toString() }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = tier.toString(),
                            color = if (isActive) Color(0xFF3F51B5) else Color.White
                        )
                    },
                    onClick = {
                        onFilterSelected(CardFilter(FilterType.TIER, tier.toString()))
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * Sort selection menu.
 *
 * @param showMenu Boolean whether to show the menu
 * @param onDismiss () -> Unit callback when menu is dismissed
 * @param onSortSelected (String) -> Unit callback when sort option is selected
 */
@Composable
fun SortMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .background(Color(0xFF2D2D2D))
                .width(200.dp)
        ) {
            Text(
                text = "Sort By",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DropdownMenuItem(
                text = { Text("Name (A-Z)", color = Color.White) },
                onClick = {
                    onSortSelected("name_asc")
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text("Name (Z-A)", color = Color.White) },
                onClick = {
                    onSortSelected("name_desc")
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text("Power (High-Low)", color = Color.White) },
                onClick = {
                    onSortSelected("power_desc")
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text("Power (Low-High)", color = Color.White) },
                onClick = {
                    onSortSelected("power_asc")
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text("Rarity (High-Low)", color = Color.White) },
                onClick = {
                    onSortSelected("rarity_desc")
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text("Energy Cost (Low-High)", color = Color.White) },
                onClick = {
                    onSortSelected("energy_asc")
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Format filter text for display.
 *
 * @param filter CardFilter the filter to format
 * @return String formatted filter text
 */
fun formatFilterText(filter: CardFilter): String {
    return when (filter.type) {
        FilterType.TYPE -> "Type: ${filter.value}"
        FilterType.TIER -> "Tier: ${filter.value}"
        FilterType.POWER_MIN -> "Power ≥ ${filter.value}"
        FilterType.ENERGY_MAX -> "Energy ≤ ${filter.value}"
    }
}