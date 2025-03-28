package com.example.vibesshared.ui.ui.cardgame

// File: PlayerProfileScreen.kt

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * Composable screen for displaying player statistics, achievements, progression, and potentially settings.
 */

/**
 * Profile screen tabs.
 */
enum class ProfileTab {
    STATS,
    ACHIEVEMENTS,
    SETTINGS
}

/**
 * Main composable for player profile screen.
 *
 * @param playerProgress PlayerProgress the player's progression data
 */
@Composable
fun PlayerProfileScreen(
    playerProgress: PlayerProgress,
    onBackClick: () -> Boolean
) {
    // Local UI state
    var currentTab by remember { mutableStateOf(ProfileTab.STATS) }

    // Background
    ThemedBackground(
        themeColors = listOf(
            Color(0xFF3F51B5), // Indigo
            Color(0xFF0D0221)  // Very dark purple
        )
    )

    // Main content
    Column(modifier = Modifier.fillMaxSize()) {
        // Screen header
        ScreenHeader(
            title = "Player Profile",
            showBackButton = true,
            onBackClick = { /* Navigate back */ }
        )

        // Profile header with player info
        ProfileHeader(
            playerLevel = playerProgress.level,
            playerName = "Cosmic Explorer", // In a real app, this would come from a user profile
            modifier = Modifier.fillMaxWidth()
        )

        // Tabs
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color(0xFF3F51B5) // Indigo
        ) {
            Tab(
                selected = currentTab == ProfileTab.STATS,
                onClick = { currentTab = ProfileTab.STATS },
                text = {
                    Text(
                        text = "Stats",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null
                    )
                }
            )

            Tab(
                selected = currentTab == ProfileTab.ACHIEVEMENTS,
                onClick = { currentTab = ProfileTab.ACHIEVEMENTS },
                text = {
                    Text(
                        text = "Achievements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null
                    )
                }
            )

            Tab(
                selected = currentTab == ProfileTab.SETTINGS,
                onClick = { currentTab = ProfileTab.SETTINGS },
                text = {
                    Text(
                        text = "Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                }
            )
        }

        // Tab content
        when (currentTab) {
            ProfileTab.STATS -> {
                StatsTab(
                    playerProgress = playerProgress,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            ProfileTab.ACHIEVEMENTS -> {
                AchievementsTab(
                    achievements = playerProgress.achievements,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            ProfileTab.SETTINGS -> {
                SettingsTab(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * Profile header with player info and avatar.
 *
 * @param playerLevel Int player level
 * @param playerName String player name
 * @param modifier Modifier additional styling
 */
@Composable
private fun ProfileHeader(
    playerLevel: Int,
    playerName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3F51B5), // Indigo center
                            Color(0xFF1A237E)  // Dark indigo outer
                        )
                    )
                )
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for avatar (in a real app, this would be an image)
            Text(
                text = playerName.first().toString(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Level badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(28.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playerLevel.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Player info
        Column {
            Text(
                text = playerName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Level $playerLevel Multiverse Explorer",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Stats tab showing player statistics.
 *
 * @param playerProgress PlayerProgress player progression data
 * @param modifier Modifier additional styling
 */
@Composable
private fun StatsTab(
    playerProgress: PlayerProgress,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progression summary
        item {
            ProgressionSummary(
                level = playerProgress.level,
                xp = playerProgress.experience,
                xpForNextLevel = playerProgress.xpToNextLevel(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Battle statistics
        item {
            BattleStats(
                battlesWon = playerProgress.battlesWon,
                battlesLost = playerProgress.battlesLost,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Collection statistics
        item {
            CollectionStats(
                cardsCollected = playerProgress.cardsCollected,
                tokens = playerProgress.multiverseTokens,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Attribute mastery (placeholder - in a real app, this would track player's attribute usage)
        item {
            AttributeMastery(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Player progression summary.
 *
 * @param level Int player level
 * @param xp Int current XP
 * @param xpForNextLevel Int XP needed for next level
 * @param modifier Modifier additional styling
 */
@Composable
private fun ProgressionSummary(
    level: Int,
    xp: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Progression",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Level and XP progress
        Column {
            Text(
                text = "Level $level",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // XP progress bar
            val progress = xp.toFloat() / xpForNextLevel.toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$xp / $xpForNextLevel XP",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                            .background(Color(0xFF3F51B5)) // Indigo
                    )
                }
            }
        }
    }
}

/**
 * Player battle statistics.
 *
 * @param battlesWon Int number of battles won
 * @param battlesLost Int number of battles lost
 * @param modifier Modifier additional styling
 */
@Composable
private fun BattleStats(
    battlesWon: Int,
    battlesLost: Int,
    modifier: Modifier = Modifier
) {
    val totalBattles = battlesWon + battlesLost
    val winRate = if (totalBattles > 0) (battlesWon.toFloat() / totalBattles.toFloat()) * 100f else 0f

    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Battle Statistics",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Battle stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Battles Won
            StatBox(
                value = battlesWon,
                label = "Battles Won",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFF4CAF50) // Green
            )

            // Battles Lost
            StatBox(
                value = battlesLost,
                label = "Battles Lost",
                icon = Icons.Default.Close,
                color = Color(0xFFE53935) // Red
            )

            // Win Rate
            StatBox(
                value = winRate.toInt(),
                label = "Win Rate (%)",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = Color(0xFF3F51B5) // Indigo
            )
        }
    }
}

/**
 * Player collection statistics.
 *
 * @param cardsCollected Int number of cards collected
 * @param tokens Int number of tokens owned
 * @param modifier Modifier additional styling
 */
@Composable
private fun CollectionStats(
    cardsCollected: Int,
    tokens: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Collection",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Collection stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Cards Collected
            StatBox(
                value = cardsCollected,
                label = "Cards Collected",
                icon = Icons.Default.Style,
                color = Color(0xFF00BCD4) // Cyan
            )

            // Tokens
            StatBox(
                value = tokens,
                label = "Tokens",
                icon = Icons.Default.Toll,
                color = Color(0xFFFFD700) // Gold
            )

            // Completion %
            // In a real app, this would be calculated based on total cards in the game
            val completion = (cardsCollected.toFloat() / 100f) * 100f // Assuming 100 total cards
            StatBox(
                value = completion.toInt(),
                label = "Completion (%)",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFFFF9800) // Orange
            )
        }
    }
}

/**
 * Player attribute mastery statistics.
 *
 * @param modifier Modifier additional styling
 */
@Composable
private fun AttributeMastery(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Attribute Mastery",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Attribute mastery bars (placeholder - in a real app, this would show actual mastery levels)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AttributeMasteryBar(
                attribute = "COSMIC",
                progress = 0.8f,
                color = Color(0xFF3F51B5) // Indigo
            )

            AttributeMasteryBar(
                attribute = "ELEMENTAL",
                progress = 0.6f,
                color = Color(0xFF4CAF50) // Green
            )

            AttributeMasteryBar(
                attribute = "TEMPORAL",
                progress = 0.4f,
                color = Color(0xFFFF9800) // Orange
            )

            AttributeMasteryBar(
                attribute = "QUANTUM",
                progress = 0.3f,
                color = Color(0xFF00BCD4) // Cyan
            )
        }
    }
}

/**
 * Individual attribute mastery progress bar.
 *
 * @param attribute String attribute name
 * @param progress Float progress value (0.0-1.0)
 * @param color Color attribute color
 */
@Composable
private fun AttributeMasteryBar(
    attribute: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = attribute,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                    .background(color)
            )
        }
    }
}

/**
 * Individual stat box.
 *
 * @param value Int stat value
 * @param label String stat label
 * @param icon ImageVector icon for the stat
 * @param color Color color theme for the stat
 */
@Composable
private fun StatBox(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon in colored circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Label
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Achievements tab showing player achievements.
 *
 * @param achievements List<String> player's achievements
 * @param modifier Modifier additional styling
 */
@Composable
private fun AchievementsTab(
    achievements: List<String>,
    modifier: Modifier = Modifier
) {
    if (achievements.isEmpty()) {
        // No achievements yet
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Achievements Yet",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Play more battles and collect cards to earn achievements!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Achievement list
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { achievement ->
                AchievementItem(
                    achievement = achievement,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Placeholder for locked achievements
            item {
                Text(
                    text = "Locked Achievements",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            // Placeholder locked achievements
            items(5) { _ ->
                LockedAchievementItem(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Individual achievement item.
 *
 * @param achievement String the achievement name
 * @param modifier Modifier additional styling
 */
@Composable
private fun AchievementItem(
    achievement: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFD700).copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Achievement text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = achievement,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Unlocked on March 10, 2025", // In a real app, this would be the actual date
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Locked achievement item (placeholder).
 *
 * @param modifier Modifier additional styling
 */
@Composable
private fun LockedAchievementItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Locked icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Placeholder text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "???",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Locked achievement",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Settings tab with game preferences.
 *
 * @param modifier Modifier additional styling
 */
@Composable
private fun SettingsTab(
    modifier: Modifier = Modifier
) {
    // Local state for settings
    var musicEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    var gameNotifications by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Audio settings
        item {
            SettingsSection(
                title = "Audio Settings",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Music toggle
                SettingsToggle(
                    title = "Music",
                    description = "Enable background music",
                    isChecked = musicEnabled,
                    onCheckedChange = { musicEnabled = it }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.1f)
                )

                // Sound effects toggle
                SettingsToggle(
                    title = "Sound Effects",
                    description = "Enable sound effects during gameplay",
                    isChecked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }
        }

        // Visual settings
        item {
            SettingsSection(
                title = "Visual Settings",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dark mode toggle
                SettingsToggle(
                    title = "Dark Mode",
                    description = "Use dark theme for reduced eye strain",
                    isChecked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
        }

        // Notification settings
        item {
            SettingsSection(
                title = "Notifications",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Notifications toggle
                SettingsToggle(
                    title = "Game Notifications",
                    description = "Receive updates about shop refreshes and events",
                    isChecked = gameNotifications,
                    onCheckedChange = { gameNotifications = it }
                )
            }
        }

        // Account settings (placeholders)
        item {
            SettingsSection(
                title = "Account",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile button
                SettingsButton(
                    title = "Edit Profile",
                    description = "Change your profile name and avatar",
                    icon = Icons.Default.Person,
                    onClick = { /* Edit profile */ }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.1f)
                )

                // Log out button
                SettingsButton(
                    title = "Log Out",
                    description = "Sign out of your account",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = { /* Log out */ }
                )
            }
        }

        // About section
        item {
            SettingsSection(
                title = "About",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Version info
                Text(
                    text = "Card Battle Game v1.0",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "© 2025 Multiverse Games",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Section container for settings.
 *
 * @param title String section title
 * @param modifier Modifier additional styling
 * @param content @Composable () -> Unit section content
 */
@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

/**
 * Toggle setting item.
 *
 * @param title String setting title
 * @param description String setting description
 * @param isChecked Boolean toggle state
 * @param onCheckedChange (Boolean) -> Unit callback when toggle changes
 */
@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF3F51B5),
                checkedTrackColor = Color(0xFF3F51B5).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * Button setting item.
 *
 * @param title String button title
 * @param description String button description
 * @param icon ImageVector button icon
 * @param onClick () -> Unit callback when button is clicked
 */
@Composable
private fun SettingsButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}