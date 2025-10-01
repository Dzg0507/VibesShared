package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import com.example.vibesshared.ui.ui.gaming.*
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.screens.EnhancedGamingScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Use the enhanced gaming screen
    EnhancedGamingScreen(
        navController = navController,
        modifier = modifier
    )
}

@Composable
fun OriginalGamingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gamingService = remember { GamingService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(GamingTab.GAMES) }
    var selectedGame by remember { mutableStateOf<MiniGame?>(null) }
    var userStats by remember { mutableStateOf<UserGamingStats?>(null) }
    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var leaderboards by remember { mutableStateOf<Map<String, Leaderboard>>(emptyMap()) }
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var dailyChallenges by remember { mutableStateOf<List<DailyChallenge>>(emptyList()) }
    
    // Load data
    LaunchedEffect(Unit) {
        userStats = gamingService.getUserStats("current_user")
        achievements = gamingService.getUserAchievements("current_user")
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
                        // Handle game session
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
                            GamesTab(
                                games = gamingService.availableGames,
                                onGameClick = { selectedGame = it }
                            )
                        }
                        GamingTab.LEADERBOARDS -> {
                            LeaderboardsTab(
                                leaderboards = leaderboards,
                                games = gamingService.availableGames
                            )
                        }
                        GamingTab.ACHIEVEMENTS -> {
                            AchievementsTab(achievements = achievements)
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
                            StatsTab(stats = userStats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamesTab(
    games: List<MiniGame>,
    onGameClick: (MiniGame) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mini Games",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(games.chunked(2)) { gameRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gameRow.forEach { game ->
                    GameCard(
                        game = game,
                        onClick = { onGameClick(game) },
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
fun GameCard(
    game: MiniGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
        }
    }
}

@Composable
fun GameDetailScreen(
    game: MiniGame,
    gamingService: GamingService,
    leaderboard: Leaderboard?,
    onBack: () -> Unit,
    onPlay: (MiniGame) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = game.icon,
                            fontSize = 80.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = game.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = game.description,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onPlay(game) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play Now", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
            
            item {
                GameInfoCard(game = game)
            }
            
            leaderboard?.let { leaderboard ->
                item {
                    LeaderboardCard(leaderboard = leaderboard)
                }
            }
        }
        
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun GameInfoCard(game: MiniGame) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Game Info",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            InfoRow("Category", game.category.name)
            InfoRow("Difficulty", game.difficulty.name)
            InfoRow("Players", "${game.maxPlayers} max")
            InfoRow("Duration", "${game.estimatedDuration} seconds")
            InfoRow("High Score", "${game.highScore}")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun LeaderboardCard(leaderboard: Leaderboard) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Leaderboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            leaderboard.entries.take(10).forEachIndexed { index, entry ->
                LeaderboardEntryRow(
                    rank = entry.rank,
                    playerName = entry.playerName,
                    score = entry.score,
                    isCurrentUser = entry.isCurrentUser
                )
                
                if (index < 9) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntryRow(
    rank: Int,
    playerName: String,
    score: Int,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = when (rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> Color.Black
            },
            modifier = Modifier.width(40.dp)
        )
        
        AsyncImage(
            model = "https://picsum.photos/32/32?id=$rank",
            contentDescription = "Avatar",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(
                    width = if (isCurrentUser) 2.dp else 0.dp,
                    color = if (isCurrentUser) VividBlue else Color.Transparent,
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = playerName,
            fontSize = 14.sp,
            color = if (isCurrentUser) VividBlue else Color.Black,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "$score",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricPurple
        )
    }
}

@Composable
fun LeaderboardsTab(
    leaderboards: Map<String, Leaderboard>,
    games: List<MiniGame>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Leaderboards",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        games.forEach { game ->
            leaderboards[game.id]?.let { leaderboard ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = game.icon,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = game.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            
                            leaderboard.entries.take(5).forEach { entry ->
                                LeaderboardEntryRow(
                                    rank = entry.rank,
                                    playerName = entry.playerName,
                                    score = entry.score,
                                    isCurrentUser = entry.isCurrentUser
                                )
                                
                                if (entry != leaderboard.entries.take(5).last()) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsTab(achievements: List<Achievement>) {
    LazyColumn(
        modifier = Modifier
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
            AchievementCard(achievement = achievement)
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
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
            Text(
                text = achievement.icon,
                fontSize = 48.sp,
                modifier = Modifier.padding(end = 16.dp)
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
fun TournamentsTab(
    tournaments: List<Tournament>,
    gamingService: GamingService
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tournaments",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(tournaments) { tournament ->
            TournamentCard(tournament = tournament)
        }
    }
}

@Composable
fun TournamentCard(tournament: Tournament) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = tournament.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tournament.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Prize Pool",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${tournament.prizePool}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGreen
                    )
                }
                
                Column {
                    Text(
                        text = "Entry Fee",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (tournament.entryFee == 0) "Free" else "$${tournament.entryFee}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                }
                
                Column {
                    Text(
                        text = "Players",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${tournament.participants}/${tournament.maxParticipants}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tournament.status.name,
                    fontSize = 12.sp,
                    color = when (tournament.status) {
                        TournamentStatus.REGISTRATION_OPEN -> LimeGreen
                        TournamentStatus.ACTIVE -> Color.Red
                        TournamentStatus.COMPLETED -> Color.Gray
                        TournamentStatus.CANCELLED -> Color.Red
                    },
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { },
                    enabled = tournament.status == TournamentStatus.REGISTRATION_OPEN,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tournament.status == TournamentStatus.REGISTRATION_OPEN) 
                            VividBlue else Color.Gray
                    )
                ) {
                    Text(
                        text = when (tournament.status) {
                            TournamentStatus.REGISTRATION_OPEN -> "Join"
                            TournamentStatus.ACTIVE -> "Live"
                            TournamentStatus.COMPLETED -> "Finished"
                            TournamentStatus.CANCELLED -> "Cancelled"
                        },
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengesTab(
    challenges: List<DailyChallenge>,
    gamingService: GamingService
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Daily Challenges",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(challenges) { challenge ->
            ChallengeCard(challenge = challenge)
        }
    }
}

@Composable
fun ChallengeCard(challenge: DailyChallenge) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = challenge.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "+${challenge.reward} coins",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LimeGreen
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = challenge.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${challenge.progress}/${challenge.target}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${(challenge.progress.toFloat() / challenge.target * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = challenge.progress.toFloat() / challenge.target,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (challenge.isCompleted) LimeGreen else VividBlue,
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { },
                enabled = !challenge.isCompleted,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (challenge.isCompleted) Color.Gray else SunsetOrange
                )
            ) {
                Text(
                    text = if (challenge.isCompleted) "Completed!" else "Complete Challenge",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatsTab(stats: UserGamingStats?) {
    if (stats == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = Modifier
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
            
            // Stats cards
            item {
                StatsGrid(stats = stats)
            }
            
            item {
                DetailedStatsCard(stats = stats)
            }
        }
    }
}

@Composable
fun StatsGrid(stats: UserGamingStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Games Played",
            value = "${stats.totalGamesPlayed}",
            icon = Icons.Filled.Gamepad,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Total Score",
            value = "${stats.totalScore}",
            icon = Icons.Filled.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Best Score",
            value = "${stats.bestScore}",
            icon = Icons.Filled.Star,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Achievements",
            value = "${stats.achievementsUnlocked}",
            icon = Icons.Filled.MilitaryTech,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ElectricPurple,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailedStatsCard(stats: UserGamingStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Detailed Stats",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            InfoRow("Average Score", "${stats.averageScore}")
            InfoRow("Favorite Game", stats.favoriteGame)
            InfoRow("Play Time", "${stats.totalPlayTime / 3600}h ${(stats.totalPlayTime % 3600) / 60}m")
            InfoRow("Global Rank", "#${stats.rank}")
            InfoRow("Coins Earned", "${stats.coinsEarned}")
            InfoRow("Tournaments Won", "${stats.tournamentsWon}")
        }
    }
}

enum class GamingTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GAMES("Games", Icons.Filled.SportsEsports),
    LEADERBOARDS("Leaderboards", Icons.Filled.EmojiEvents),
    ACHIEVEMENTS("Achievements", Icons.Filled.MilitaryTech),
    TOURNAMENTS("Tournaments", Icons.Filled.Tournament),
    CHALLENGES("Challenges", Icons.Filled.Assignment),
    STATS("Stats", Icons.Filled.Analytics)
}