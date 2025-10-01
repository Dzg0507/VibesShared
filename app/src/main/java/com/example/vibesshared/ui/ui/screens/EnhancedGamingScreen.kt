package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.*
import com.example.vibesshared.ui.ui.gaming.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedGamingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gamingService = remember { GamingService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(GamingTab.GAMES) }
    var selectedGame by remember { mutableStateOf<MiniGame?>(null) }
    var userStats by remember { mutableStateOf<UserGamingStats?>(null) }
    var userLevel by remember { mutableStateOf<UserLevel?>(null) }
    var enhancedAchievements by remember { mutableStateOf<List<EnhancedAchievement>>(emptyList()) }
    var socialData by remember { mutableStateOf<SocialGamingData?>(null) }
    var leaderboards by remember { mutableStateOf<Map<String, Leaderboard>>(emptyMap()) }
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var dailyChallenges by remember { mutableStateOf<List<DailyChallenge>>(emptyList()) }
    
    // Animation states
    var showAchievementAnimation by remember { mutableStateOf(false) }
    var showLevelUpAnimation by remember { mutableStateOf(false) }
    var currentAchievement by remember { mutableStateOf<EnhancedAchievement?>(null) }
    var currentLevelUp by remember { mutableStateOf<LevelUpData?>(null) }
    
    // Load data
    LaunchedEffect(Unit) {
        userStats = gamingService.getUserStats("current_user")
        userLevel = gamingService.getUserLevel("current_user")
        enhancedAchievements = gamingService.getEnhancedAchievements("current_user")
        socialData = gamingService.getSocialFeatures("current_user")
        tournaments = gamingService.getTournaments()
        dailyChallenges = gamingService.getDailyChallenges()
        
        // Load leaderboards for each game
        val leaderboardMap = mutableMapOf<String, Leaderboard>()
        gamingService.availableGames.forEach { game ->
            leaderboardMap[game.id] = gamingService.getLeaderboard(game.id, LeaderboardTimeFrame.WEEKLY)
        }
        leaderboards = leaderboardMap
    }
    
    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = gradientColors)
            )
    ) {
        // Achievement Animation Overlay
        currentAchievement?.let { achievement ->
            if (showAchievementAnimation) {
                AchievementAnimationOverlay(
                    animationType = achievement.animationType,
                    title = achievement.title,
                    description = achievement.description,
                    onAnimationComplete = {
                        showAchievementAnimation = false
                        currentAchievement = null
                    }
                )
            }
        }
        
        // Level Up Animation Overlay
        currentLevelUp?.let { levelUp ->
            if (showLevelUpAnimation) {
                LevelUpOverlay(
                    newLevel = levelUp.newLevel,
                    levelTitle = levelUp.title,
                    rewards = levelUp.rewards,
                    onAnimationComplete = {
                        showLevelUpAnimation = false
                        currentLevelUp = null
                    }
                )
            }
        }
        
        if (selectedGame != null) {
            // Game Detail Screen
            GameDetailScreen(
                game = selectedGame!!,
                gamingService = gamingService,
                leaderboard = leaderboards[selectedGame!!.id],
                onBack = { selectedGame = null },
                onPlay = { game ->
                    scope.launch {
                        val session = gamingService.startGame(game.id, "current_user")
                        // Simulate achievement unlock
                        if ((0..10).random() == 0) {
                            val randomAchievement = enhancedAchievements.filter { !it.isUnlocked }.randomOrNull()
                            if (randomAchievement != null) {
                                currentAchievement = randomAchievement
                                showAchievementAnimation = true
                            }
                        }
                        // Simulate level up
                        if ((0..20).random() == 0) {
                            userLevel?.let { level ->
                                currentLevelUp = LevelUpData(
                                    newLevel = level.level + 1,
                                    title = "New Level Title",
                                    rewards = level.rewards
                                )
                                showLevelUpAnimation = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Main Gaming Screen
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.SportsEsports,
                                    contentDescription = "Gaming",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Gaming Hub",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            userStats?.let { stats ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Level Badge
                                    userLevel?.let { level ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = level.badgeIcon,
                                                    fontSize = 16.sp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Lv.${level.level}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Coins
                                    Icon(
                                        imageVector = Icons.Filled.MonetizationOn,
                                        contentDescription = "Coins",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${stats.coinsEarned}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                                    )
                                }
                            }
                        }
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ) {
                        GamingTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                text = { Text(tab.title, color = Color.White) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = Color.White
                                    )
                                }
                            )
                        }
                    }
                    
                    // Content based on selected tab
                    when (currentTab) {
                        GamingTab.GAMES -> {
                            EnhancedGamesTab(
                                games = gamingService.availableGames,
                                onGameClick = { selectedGame = it },
                                userLevel = userLevel
                            )
                        }
                        GamingTab.LEADERBOARDS -> {
                            LeaderboardsTab(
                                leaderboards = leaderboards,
                                games = gamingService.availableGames
                            )
                        }
                        GamingTab.ACHIEVEMENTS -> {
                            EnhancedAchievementsTab(
                                achievements = enhancedAchievements,
                                onAchievementClick = { achievement ->
                                    if (achievement.isUnlocked) {
                                        currentAchievement = achievement
                                        showAchievementAnimation = true
                                    }
                                }
                            )
                        }
                        GamingTab.TOURNAMENTS -> {
                            TournamentsTab(
                                tournaments = tournaments,
                                gamingService = gamingService
                            )
                        }
                        GamingTab.CHALLENGES -> {
                            ChallengesTab(
                                challenges = dailyChallenges,
                                gamingService = gamingService
                            )
                        }
                        GamingTab.STATS -> {
                            EnhancedStatsTab(
                                stats = userStats,
                                userLevel = userLevel,
                                socialData = socialData
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedGamesTab(
    games: List<MiniGame>,
    onGameClick: (MiniGame) -> Unit,
    userLevel: UserLevel?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mini Games",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                userLevel?.let { level ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = level.badgeIcon,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = level.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        items(games.chunked(2)) { gameRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gameRow.forEach { game ->
                    EnhancedGameCard(
                        game = game,
                        onClick = { onGameClick(game) },
                        userLevel = userLevel,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if odd number of games
                if (gameRow.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EnhancedGameCard(
    game: MiniGame,
    onClick: () -> Unit,
    userLevel: UserLevel?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = glow),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = game.icon,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = game.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = game.description,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.category.name,
                    fontSize = 10.sp,
                    color = VividBlue,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${game.maxPlayers}p",
                    fontSize = 10.sp,
                    color = ElectricPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.difficulty.name,
                    fontSize = 10.sp,
                    color = when (game.difficulty) {
                        Difficulty.EASY -> LimeGreen
                        Difficulty.MEDIUM -> SunsetOrange
                        Difficulty.HARD -> Color.Red
                    },
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${game.estimatedDuration}s",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            // Level requirement indicator
            userLevel?.let { level ->
                if (game.difficulty == Difficulty.HARD && level.level < 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Level ${level.level + 1} required",
                            fontSize = 10.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedAchievementsTab(
    achievements: List<EnhancedAchievement>,
    onAchievementClick: (EnhancedAchievement) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Achievements",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(achievements) { achievement ->
            EnhancedAchievementCard(
                achievement = achievement,
                onClick = { onAchievementClick(achievement) }
            )
        }
    }
}

@Composable
fun EnhancedAchievementCard(
    achievement: EnhancedAchievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) 
                Color.White.copy(alpha = 0.9f) 
            else 
                Color.Gray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "icon")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Text(
                text = achievement.icon,
                fontSize = 48.sp,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .graphicsLayer { rotationZ = if (achievement.isUnlocked) rotation else 0f }
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) Color.Black else Color.Gray
                )
                
                Text(
                    text = achievement.description,
                    fontSize = 14.sp,
                    color = if (achievement.isUnlocked) Color.Gray else Color.Gray.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.category.name,
                        fontSize = 12.sp,
                        color = VividBlue,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = achievement.rarity.name,
                        fontSize = 12.sp,
                        color = when (achievement.rarity) {
                            AchievementRarity.COMMON -> Color.Gray
                            AchievementRarity.RARE -> Color.Blue
                            AchievementRarity.EPIC -> Color.Magenta
                            AchievementRarity.LEGENDARY -> Color(0xFFFFD700)
                        },
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "+${achievement.points} XP",
                        fontSize = 12.sp,
                        color = LimeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (achievement.isUnlocked) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = LimeGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedStatsTab(
    stats: UserGamingStats?,
    userLevel: UserLevel?,
    socialData: SocialGamingData?,
    modifier: Modifier = Modifier
) {
    if (stats == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Your Stats",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Level Progress Card
            userLevel?.let { level ->
                item {
                    LevelProgressCard(
                        userLevel = level,
                        stats = stats
                    )
                }
            }
            
            // Stats Grid
            item {
                StatsGrid(stats = stats)
            }
            
            // Social Stats
            socialData?.let { social ->
                item {
                    SocialStatsCard(socialData = social)
                }
            }
            
            item {
                DetailedStatsCard(stats = stats)
            }
        }
    }
}

@Composable
fun LevelProgressCard(
    userLevel: UserLevel,
    stats: UserGamingStats,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level ${userLevel.level}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = userLevel.badgeIcon,
                    fontSize = 24.sp
                )
            }
            
            Text(
                text = userLevel.title,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Experience Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stats.experiencePoints} XP",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${stats.experienceToNextLevel} to next level",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = stats.experiencePoints.toFloat() / (stats.experiencePoints + stats.experienceToNextLevel),
                    modifier = Modifier.fillMaxWidth(),
                    color = LimeGreen,
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Streak
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stats.streakDays} day streak",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SocialStatsCard(
    socialData: SocialGamingData,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Social Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${socialData.friendsPlaying.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividBlue
                    )
                    Text(
                        text = "Friends",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${socialData.recentChallenges.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunsetOrange
                    )
                    Text(
                        text = "Challenges",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${socialData.teamChallenges.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGreen
                    )
                    Text(
                        text = "Teams",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

data class LevelUpData(
    val newLevel: Int,
    val title: String,
    val rewards: List<LevelReward>
)