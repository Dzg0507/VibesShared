package com.example.vibesshared.ui.ui.cardgame

// File: ShopAndForgeScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.vibesshared.R
import java.util.concurrent.TimeUnit

/**
 * Composable screen combining the card shop and card forging interfaces, potentially using tabs.
 */

/**
 * Main tabs for shop and forge screen.
 */
enum class ShopForgeTab {
    SHOP,
    FORGE
}

/**
 * Main composable for shop and forge screen.
 *
 * @param viewModel CardShopViewModel the view model for this screen
 */
@Composable
fun ShopAndForgeScreen(
    viewModel: CardShopViewModel,
    onBackClick: () -> Unit
) {
    // Collect state from ViewModel
    val shopCards by viewModel.shopCards.collectAsState()
    val selectedShopCard by viewModel.selectedShopCard.collectAsState()
    val playerTokens by viewModel.playerTokens.collectAsState(initial = 0)
    val refreshTimerSeconds by viewModel.refreshTimerSeconds.collectAsState()
    val selectedForgeCardIds by viewModel.selectedForgeCardIds.collectAsState()
    val lastForgedCardResult by viewModel.lastForgedCardResult.collectAsState()

    // Local UI state
    var currentTab by remember { mutableStateOf(ShopForgeTab.SHOP) }
    var showForgeResultDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top row with back button and refresh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button placeholder - replace with actual back navigation
            Button(onClick = onBackClick) {
                Text("Back")
            }

            // Refresh button for shop tab
            if (currentTab == ShopForgeTab.SHOP) {
                IconButton(onClick = { viewModel.requestRefresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Shop",
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
                selected = currentTab == ShopForgeTab.SHOP,
                onClick = { currentTab = ShopForgeTab.SHOP },
                text = {
                    Text(
                        text = "Shop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null
                    )
                }
            )

            Tab(
                selected = currentTab == ShopForgeTab.FORGE,
                onClick = { currentTab = ShopForgeTab.FORGE },
                text = {
                    Text(
                        text = "Forge",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null
                    )
                }
            )
        }

        // Tab content
        when (currentTab) {
            ShopForgeTab.SHOP -> {
                ShopTab(
                    shopCards = shopCards,
                    selectedCard = selectedShopCard,
                    playerTokens = playerTokens,
                    refreshTimerSeconds = refreshTimerSeconds,
                    onCardSelect = { viewModel.selectShopCard(it) },
                    onPurchase = { viewModel.purchaseSelected() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            ShopForgeTab.FORGE -> {
                ForgeTab(
                    selectedCardIds = selectedForgeCardIds,
                    onCardToggle = { viewModel.selectForgeCard(it) },
                    onForge = {
                        viewModel.executeForge()
                        showForgeResultDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }

    // Forge result dialog
    if (showForgeResultDialog && lastForgedCardResult != null) {
        RewardDialog(
            card = lastForgedCardResult!!,
            onViewCard = { /* View card details */ },
            onDismiss = {
                viewModel.clearForgeResult()
                showForgeResultDialog = false
            }
        )
    }
}

/**
 * Shop tab displaying cards available for purchase.
 *
 * @param shopCards List<MultiverseCard> cards available in the shop
 * @param selectedCard MultiverseCard? currently selected card
 * @param playerTokens Int current token balance
 * @param refreshTimerSeconds Int seconds until shop refresh
 * @param onCardSelect (String) -> Unit callback when card is selected
 * @param onPurchase () -> Unit callback when purchase button is clicked
 * @param modifier Modifier additional styling
 */
@Composable
private fun ShopTab(
    shopCards: List<MultiverseCard>,
    selectedCard: MultiverseCard?,
    playerTokens: Int,
    refreshTimerSeconds: Int,
    onCardSelect: (String) -> Unit,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Shop header with tokens and refresh timer
        ShopHeader(
            playerTokens = playerTokens,
            refreshTimerSeconds = refreshTimerSeconds,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shop content
        Row(modifier = Modifier.weight(1f)) {
            // Card grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shopCards) { card ->
                    ShopCardItem(
                        card = card,
                        isSelected = card.id == selectedCard?.id,
                        onCardSelect = { onCardSelect(card.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Purchase panel
            PurchasePanel(
                card = selectedCard,
                playerTokens = playerTokens,
                onPurchase = onPurchase,
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Shop header with tokens display and refresh timer.
 *
 * @param playerTokens Int current token balance
 * @param refreshTimerSeconds Int seconds until shop refresh
 * @param modifier Modifier additional styling
 */
@Composable
private fun ShopHeader(
    playerTokens: Int,
    refreshTimerSeconds: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tokens display
        ResourceCounter(
            value = playerTokens,
            label = "Tokens",
            icon = Icons.Default.Toll,
            color = Color(0xFFFFD700) // Gold
        )

        // Shop refresh timer
        ShopRefreshTimer(
            seconds = refreshTimerSeconds
        )
    }
}

/**
 * Shop refresh timer display.
 *
 * @param seconds Int seconds until refresh
 */
@Composable
private fun ShopRefreshTimer(
    seconds: Int
) {
    // Convert seconds to hours:minutes:seconds format
    val hours = TimeUnit.SECONDS.toHours(seconds.toLong())
    val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()) % 60
    val remainingSeconds = seconds % 60

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Refresh Timer",
            tint = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "until refresh",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

/**
 * Individual card item in the shop.
 *
 * @param card MultiverseCard the card
 * @param isSelected Boolean whether the card is selected
 * @param onCardSelect () -> Unit callback when card is selected
 */
@Composable
private fun ShopCardItem(
    card: MultiverseCard,
    isSelected: Boolean,
    onCardSelect: () -> Unit
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

        // Token cost overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .background(Color(0xFF1A1A1A).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Toll,
                    contentDescription = "Token Cost",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${card.tokenValue}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Purchase panel for selected shop card.
 *
 * @param card MultiverseCard? the selected card
 * @param playerTokens Int current token balance
 * @param onPurchase () -> Unit callback for purchase button
 * @param modifier Modifier additional styling
 */
@Composable
private fun PurchasePanel(
    card: MultiverseCard?,
    playerTokens: Int,
    onPurchase: () -> Unit,
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
                text = "Select a card to purchase",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            // Card purchase details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card display
                CardComponent(
                    card = card,
                    displayMode = CardDisplayMode.FULL,
                    onClick = TODO(),
                    modifier = TODO(),
                    isSelected = TODO(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Token cost
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Toll,
                        contentDescription = "Token Cost",
                        tint = Color(0xFFFFD700), // Gold
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${card.tokenValue} Tokens",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tokens status
                val canAfford = playerTokens >= card.tokenValue
                Text(
                    text = if (canAfford) "You have enough tokens" else "Not enough tokens",
                    color = if (canAfford) Color(0xFF4CAF50) else Color(0xFFE53935),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Purchase button
                Button(
                    onClick = onPurchase,
                    enabled = canAfford,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                        disabledContentColor = Color (0xFFFFD700).copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Purchase Card",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Forge tab for combining cards.
 *
 * @param selectedCardIds List<String> IDs of cards selected for forging
 * @param onCardToggle (String) -> Unit callback to toggle card selection
 * @param onForge () -> Unit callback to execute forging
 * @param modifier Modifier additional styling
 */
@Composable
private fun ForgeTab(
    selectedCardIds: List<String>,
    onCardToggle: (String) -> Unit,
    onForge: () -> Unit,
    modifier: Modifier = Modifier
) {
    // In a real app, this would get the actual card objects from a repository
    // For now, using a placeholder for selected cards
    val selectedForgeCards = remember(selectedCardIds) {
        // Placeholder - in real app, would look up actual cards
        selectedCardIds.map { id ->
            MultiverseCard(
                id = id,
                name = "Card $id",
                description = "Selected for forging",
                tier = CardTier.COMMON,
                type = CardType.COSMIC,
                power = 5,
                defense = 5,
                health = 10,
                energy = 2,
                abilities = emptyList(),
                source = CardSource.SHOP,
                tokenValue = 100,
                imageResId = R.drawable.charizard,
                effects = listOf(
                    CardEffect.Damage(5),
                    CardEffect.ApplyStatus("Stellar Aura", 2)
                ),
                keywords = listOf("Robot", "Builder")
            )
        }
    }

    // Player's collection - in a real app, this would come from repository
    val playerCollection = remember {
        // Placeholder - in real app, would come from repository
        List(10) { index ->
            MultiverseCard(
                id = "card_$index",
                name = "Collection Card $index",
                description = "Available for forging",
                tier = CardTier.entries[index % CardTier.entries.size],
                type = CardType.entries[index % CardType.entries.size],
                power = 5 + index,
                defense = 5,
                health = 10,
                energy = 2,
                abilities = emptyList(),
                source = CardSource.SHOP,
                tokenValue = 100,
                imageResId = R.drawable.charizard,
                effects = listOf(
                    CardEffect.Damage(5),
                    CardEffect.ApplyStatus("Stellar Aura", 2)
                ),
                keywords = listOf("Robot", "Builder")
            )
        }
    }

    Column(modifier = modifier) {
        // Forge header and instructions
        ForgeHeader(
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Forge content
        Row(modifier = Modifier.weight(1f)) {
            // Card selection grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playerCollection) { card ->
                    ForgeCardItem(
                        card = card,
                        isSelected = card.id in selectedCardIds,
                        onCardToggle = { onCardToggle(card.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Forge panel
            ForgePanel(
                selectedCards = selectedForgeCards,
                onCardToggle = onCardToggle,
                onForge = onForge,
                canForge = selectedCardIds.size == 2, // Need exactly 2 cards
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Forge header with instructions.
 *
 * @param modifier Modifier additional styling
 */
@Composable
private fun ForgeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "The Multiverse Forge",
            color = Color(0xFFFFD700), // Gold
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select two cards from your collection to combine them into a more powerful card. The resulting card will have enhanced stats and may gain new abilities.",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

/**
 * Individual card item in the forge selection grid.
 *
 * @param card MultiverseCard the card
 * @param isSelected Boolean whether the card is selected
 * @param onCardToggle () -> Unit callback to toggle selection
 */
@Composable
private fun ForgeCardItem(
    card: MultiverseCard,
    isSelected: Boolean,
    onCardToggle: () -> Unit
) {
    Box(
        modifier = Modifier.aspectRatio(0.7f)
    ) {
        // Card component
        CardComponent(
            card = card,
            onClick = onCardToggle,
            isSelected = isSelected,
        )

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Forge panel showing selected cards and forge button.
 *
 * @param selectedCards List<MultiverseCard> cards selected for forging
 * @param onCardToggle (String) -> Unit callback to toggle card selection
 * @param onForge () -> Unit callback to execute forging
 * @param canForge Boolean whether forging is currently possible
 * @param modifier Modifier additional styling
 */
@Composable
private fun ForgePanel(
    selectedCards: List<MultiverseCard>,
    onCardToggle: (String) -> Unit,
    onForge: () -> Unit,
    canForge: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Selected Cards",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected cards
        if (selectedCards.isEmpty()) {
            Text(
                text = "Select cards to forge",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Show selected cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedCards.forEach { card ->
                    SelectedForgeCard(
                        card = card,
                        onRemove = { onCardToggle(card.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Forge button
        Button(
            onClick = onForge,
            enabled = canForge,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800), // Orange
                disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.3f)
            )
        ) {

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Forge New Card",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Forge status
        Text(
            text = if (selectedCards.size < 2)
                "Select exactly 2 cards to forge"
            else if (canForge)
                "Ready to forge!"
            else
                "Cannot forge these cards",
            color = if (canForge) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Selected card item in the forge panel.
 *
 * @param card MultiverseCard the selected card
 * @param onRemove () -> Unit callback to remove from selection
 */
@Composable
private fun SelectedForgeCard(
    card: MultiverseCard,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF424242), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card type icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    getColorForCardType(card.type),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.type.name.first().toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Card info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = card.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${card.type} - ${card.tier}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        // Stats preview
        Text(
            text = card.getStatsSummary(),
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White
            )
        }
    }
}

/**
 * Helper function to get color for card type.
 *
 * @param type CardType the card type
 * @return Color corresponding to the type
 */
private fun getColorForCardType(type: CardType): Color {
    return when (type) {
        CardType.COSMIC -> Color(0xFF3F51B5)      // Indigo
        CardType.ELEMENTAL -> Color(0xFF4CAF50)   // Green
        CardType.TEMPORAL -> Color(0xFFFF9800)    // Orange
        CardType.QUANTUM -> Color(0xFF00BCD4)     // Cyan
        CardType.TECH -> Color(0xFF607D8B)
        CardType.PSYCHIC -> Color(0xFFE91E63)
        CardType.MYTHIC -> Color(0xFF9C27B0)
        CardType.VOID -> Color(0xFF795548)
    }
}