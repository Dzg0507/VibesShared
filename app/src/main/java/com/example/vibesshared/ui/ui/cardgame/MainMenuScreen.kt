package com.example.vibesshared.ui.ui.cardgame

// File: MainMenuScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// File: MainMenuScreen.kt

import androidx.navigation.NavController // Import NavController


/**
 * Composable function defining the layout and interaction logic for the main menu UI.
 */

/**
 * Main menu screen composable.
 *
 * @param viewModel MainMenuViewModel the view model for this screen
 * @param navController NavController The NavController for navigation actions
 */
@Composable
fun MainMenuScreen(
    viewModel: MainMenuViewModel,
    navController: NavController // Add NavController parameter
) {
    // Collect state from ViewModel
    val cardCount by viewModel.cardCount.collectAsState(initial = 0)
    val deckCount by viewModel.deckCount.collectAsState(initial = 0)
    val showNewContentIndicator by viewModel.showNewContentIndicator.collectAsState()
    val playerProgress by viewModel.playerProgress.collectAsState()

    // Background with cosmic theme
    ThemedBackground(
        themeColors = listOf(
            Color(0xFF0D0221), // Very dark purple
            Color(0xFF1A237E)  // Dark blue
        )
    )

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title and player info
        MainMenuHeader(
            playerLevel = playerProgress.level,
            playerXp = playerProgress.experience,
            xpToNextLevel = playerProgress.xpToNextLevel(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats summary
        PlayerStatsSummary(
            cardsCollected = cardCount,
            decksCreated = deckCount,
            tokens = playerProgress.multiverseTokens,
            battlesWon = playerProgress.battlesWon,
            battlesLost = playerProgress.battlesLost,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main menu buttons - Pass NavController to ViewModel functions
        MainMenuOptions(
            showNewContentIndicator = showNewContentIndicator,
            onBattleClick = { viewModel.navigateToBattle(navController) }, // Pass navController
            onCollectionClick = { viewModel.navigateToCollection(navController) }, // Pass navController
            onShopForgeClick = { viewModel.navigateToShopForge(navController) }, // Pass navController
            onProfileClick = { viewModel.navigateToProfile(navController) }, // Pass navController
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Game version text
        Text(
            text = "Card Battle Game v1.0",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- Keep MainMenuHeader, PlayerStatsSummary, StatItem, MainMenuOptions, MainMenuButton ---
// --- as they were, they don't need changes for this specific refactor ---

/* ... Rest of the composables (MainMenuHeader, PlayerStatsSummary, etc.) ... */
@Composable
private fun MainMenuHeader(
    playerLevel: Int,
    playerXp: Int,
    xpToNextLevel: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game title
        Text(
            text = "MULTIVERSE",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "CARD BATTLE",
            color = Color(0xFF4CAF50), // Green
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player level with progress bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Level circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50), // Green center
                                Color(0xFF1B5E20)  // Dark green outer
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playerLevel.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // XP progress bar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Level $playerLevel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // XP Progress bar
                val progress = playerXp.toFloat() / xpToNextLevel.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(Color(0xFF4CAF50)) // Green
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // XP text
                Text(
                    text = "$playerXp / $xpToNextLevel XP",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PlayerStatsSummary(
    cardsCollected: Int,
    decksCreated: Int,
    tokens: Int,
    battlesWon: Int,
    battlesLost: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cards stat
        StatItem(
            value = cardsCollected,
            label = "Cards",
            icon = Icons.Default.Style,
            modifier = Modifier.weight(1f)
        )

        // Decks stat
        StatItem(
            value = decksCreated,
            label = "Decks",
            icon = Icons.Default.Layers,
            modifier = Modifier.weight(1f)
        )

        // Tokens stat
        StatItem(
            value = tokens,
            label = "Tokens",
            icon = Icons.Default.Toll,
            modifier = Modifier.weight(1f)
        )

        // Wins stat
        StatItem(
            value = battlesWon,
            label = "Wins",
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun StatItem(
    value: Int,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}


@Composable
private fun MainMenuOptions(
    showNewContentIndicator: Boolean,
    onBattleClick: () -> Unit,
    onCollectionClick: () -> Unit,
    onShopForgeClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battle button
        MainMenuButton(
            text = "Battle",
            description = "Fight opponents and earn rewards",
            icon = Icons.AutoMirrored.Filled.CallMade,
            color = Color(0xFFE53935), // Red
            onClick = onBattleClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Collection button
        MainMenuButton(
            text = "Collection",
            description = "Manage your cards and decks",
            icon = Icons.Default.Style,
            color = Color(0xFF3F51B5), // Indigo
            onClick = onCollectionClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Shop/Forge button
        MainMenuButton(
            text = "Shop & Forge",
            description = "Buy new cards or create powerful combinations",
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFF4CAF50), // Green
            showNotification = showNewContentIndicator,
            onClick = onShopForgeClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Profile button
        MainMenuButton(
            text = "Profile",
            description = "View your stats and achievements",
            icon = Icons.Default.Person,
            color = Color(0xFFFF9800), // Orange
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun MainMenuButton(
    text: String,
    description: String,
    icon: ImageVector,
    color: Color,
    showNotification: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D))
            .border(2.dp, color.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in colored circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // New content indicator
            if (showNotification) {
                Box {
                    Badge(
                        count = 1,
                        color = Color(0xFFE53935), // Red
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}