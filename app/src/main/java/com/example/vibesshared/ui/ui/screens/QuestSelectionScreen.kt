package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.vibesshared.ui.ui.components.TriviaScene
import com.example.vibesshared.ui.ui.components.TriviaSceneRenderer
import com.example.vibesshared.ui.ui.components.rememberTriviaModelsManager
import com.example.vibesshared.ui.ui.data.*
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestSelectionScreenDetailed(
    viewModel: TriviaGameViewModel,
    onBackToMenu: () -> Unit,
    onStartQuest: () -> Unit
) {
    val questState by viewModel.questState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var sceneRenderer by remember { mutableStateOf<TriviaSceneRenderer?>(null) }
    var selectedQuestId by remember { mutableStateOf<String?>(null) }
    val modelsManager = rememberTriviaModelsManager()

    var selectedDifficulty by remember { mutableStateOf<QuestDifficulty?>(null) }

    // Toggle for showing completed quests
    var showCompletedQuests by remember { mutableStateOf(true) }

    // Get the completed quest IDs directly from player progress
    val completedQuestIds = questState.playerProgress.completedQuests

    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000),  // Darker color at the top
            Color(0xFF26004D)   // Deep purple at the bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        // 3D Scene for background
        TriviaScene(
            modifier = Modifier.fillMaxSize(),
            modelsManager = modelsManager,
            onSceneReady = { renderer ->
                sceneRenderer = renderer
            }
        )

        // Floating particles
        FloatingParticles(
            particleCount = 30,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackToMenu()
                    }
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Multiverse Quests",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                LevelBadge(level = questState.playerProgress.level)
            }

            // Player Stats Card
            PlayerStatsCard(playerProgress = questState.playerProgress)

            Spacer(modifier = Modifier.height(16.dp))

            // Filters row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty filter pills - take most of the space
                Box(modifier = Modifier.weight(5f)) {
                    DifficultyFilterPills(
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { difficulty ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedDifficulty = if (selectedDifficulty == difficulty) null else difficulty
                        }
                    )
                }

                // Toggle for completed quests
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = if (showCompletedQuests) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (showCompletedQuests) Color(0xFF4CAF50) else Color.Gray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showCompletedQuests = !showCompletedQuests
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (showCompletedQuests) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Completed Quests",
                            tint = if (showCompletedQuests) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (showCompletedQuests) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quests List
            Box(modifier = Modifier.weight(1f)) {
                // Get all quests based on selected difficulty
                val allQuests = if (selectedDifficulty != null) {
                    questState.availableQuests.filter { it.difficulty == selectedDifficulty }
                } else {
                    questState.availableQuests
                }

                // Filter quests based on completion status and toggle
                val filteredQuests = allQuests.filter { quest ->
                    val isCompleted = completedQuestIds.contains(quest.id)
                    showCompletedQuests || !isCompleted
                }

                if (filteredQuests.isEmpty()) {
                    EmptyQuestsMessage()
                } else {
                    // Group quests by difficulty
                    val questsByDifficulty = filteredQuests.groupBy { it.difficulty }

                    // Sort difficulties by order
                    val sortedDifficulties = QuestDifficulty.values().filter { difficulty ->
                        questsByDifficulty.containsKey(difficulty)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        sortedDifficulties.forEach { difficulty ->
                            item(key = "header_${difficulty.name}") {
                                DifficultyHeader(difficulty = difficulty)
                            }

                            items(
                                items = questsByDifficulty[difficulty] ?: emptyList(),
                                key = { it.id }
                            ) { quest ->
                                val isCompleted = completedQuestIds.contains(quest.id)

                                // For debugging
                                println("Quest ${quest.id} - Completed: $isCompleted")

                                QuestCard3D(
                                    quest = quest,
                                    isSelected = quest.id == selectedQuestId,
                                    isCompleted = isCompleted,  // Pass completion status to card
                                    onQuestSelected = { questId ->
                                        // Only start quests that aren't completed
                                        if (!isCompleted) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.startQuest(questId)
                                            onStartQuest()
                                        }
                                    }
                                )
                            }
                        }

                        // Add extra space at bottom
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingParticles(
    particleCount: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val maxWidth = constraints.maxWidth
        val maxHeight = constraints.maxHeight

        repeat(particleCount) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val random = Random(index)

            val xPosition = infiniteTransition.animateFloat(
                initialValue = random.nextInt(until = maxWidth).toFloat(),
                targetValue = random.nextInt(until = maxWidth).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 20000 + random.nextInt(20000),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val yPosition = infiniteTransition.animateFloat(
                initialValue = random.nextInt(until = maxHeight).toFloat(),
                targetValue = random.nextInt(until = maxHeight).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 20000 + random.nextInt(20000),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val alpha = infiniteTransition.animateFloat(
                initialValue = 0.1f + 0.3f * random.nextFloat(),
                targetValue = 0.1f + 0.3f * random.nextFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 5000 + random.nextInt(5000),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val size = (3 + random.nextInt(6)).dp

            val colors = listOf(
                Color(0xFF29B6F6),  // Light Blue
                Color(0xFF00E676),  // Green
                Color(0xFFFFEB3B),  // Yellow
                Color(0xFFFF4081),  // Pink
                Color(0xFF7C4DFF)   // Purple
            )

            val color = colors[index % colors.size]

            Box(
                modifier = Modifier
                    .size(size)
                    .alpha(alpha.value)
                    .background(color, CircleShape)
                    .offset(
                        x = xPosition.value.dp,
                        y = yPosition.value.dp
                    )
            )
        }
    }
}

@Composable
fun LevelBadge(level: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFF512DA8)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE040FB)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$level",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun PlayerStatsCard(
    playerProgress: PlayerProgress
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                icon = Icons.Filled.Star,
                value = "${playerProgress.level}",
                label = "Level",
                color = Color(0xFFFFD700)
            )

            @Suppress("DEPRECATION")
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            StatisticItem(
                icon = Icons.Filled.Bolt,
                value = "${playerProgress.xp}/${playerProgress.xpForNextLevel()}",
                label = "XP",
                color = Color(0xFF4CAF50)
            )

            @Suppress("DEPRECATION")
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            StatisticItem(
                icon = Icons.Filled.MonetizationOn,
                value = "${playerProgress.multiverseTokens}",
                label = "Tokens",
                color = Color(0xFF00FFFF)
            )
        }
    }
}

@Composable
fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DifficultyFilterPills(
    selectedDifficulty: QuestDifficulty?,
    onDifficultySelected: (QuestDifficulty) -> Unit
) {
    val difficulties = remember { QuestDifficulty.values() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        difficulties.forEach { difficulty ->
            val isSelected = selectedDifficulty == difficulty
            val difficultyColor = getDifficultyColor(difficulty)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) difficultyColor else Color.Transparent,
                border = BorderStroke(1.dp, difficultyColor),
                modifier = Modifier.clickable { onDifficultySelected(difficulty) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = getDifficultyIcon(difficulty),
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else difficultyColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) Color.Black else difficultyColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyHeader(difficulty: QuestDifficulty) {
    val (color, emoji) = getDifficultyColorAndEmoji(difficulty)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = "${difficulty.name} QUESTS",
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        @Suppress("DEPRECATION")
        Divider(
            modifier = Modifier
                .weight(2f)
                .padding(start = 8.dp),
            color = color.copy(alpha = 0.5f)
        )
    }
}

// In QuestSelectionScreen.kt, modify the QuestCard3D function:

// First, update the QuestCard3D function definition with the correct parameters
// Restore the expandable quest cards while keeping the start quest button

// Update the QuestCard3D component to show a "Completed" overlay for finished quests

@Composable
fun QuestCard3D(
    quest: Quest,
    isSelected: Boolean,
    onQuestSelected: (String) -> Unit,
    isCompleted: Boolean = false // Parameter to track completion status
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val buttonScale = remember { Animatable(1f) }

    // Animation for card selection
    LaunchedEffect(isSelected) {
        if (isSelected) {
            buttonScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val difficultyColor = getDifficultyColor(quest.difficulty)
    val infiniteTransition = rememberInfiniteTransition()
    val borderGlow = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(buttonScale.value)
                .clickable {
                    // Toggle expanded state when clicking on the card
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    expanded = !expanded
                }
                .graphicsLayer {
                    shadowElevation = if (isSelected) borderGlow.value * 4 else 2f
                    shape = RoundedCornerShape(16.dp)
                }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF26004D).copy(alpha = if (isSelected) 0.9f else 0.7f)
            ),
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    difficultyColor
                } else {
                    difficultyColor.copy(alpha = 0.5f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Expand/collapse icon
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            expanded = !expanded
                        }
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 8.dp),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Basic quest info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuestInfoChip(
                        icon = Icons.Filled.QuestionAnswer,
                        text = "${quest.questionCount}",
                        color = difficultyColor
                    )

                    QuestInfoChip(
                        icon = Icons.Filled.Timer,
                        text = "${quest.timeLimit / 60}m",
                        color = difficultyColor
                    )

                    QuestInfoChip(
                        icon = Icons.Filled.Category,
                        text = quest.category.replaceFirstChar { it.uppercase() },
                        color = difficultyColor
                    )

                    if (quest.requiredLevel > 1) {
                        QuestInfoChip(
                            icon = Icons.Filled.Star,
                            text = "Lvl ${quest.requiredLevel}+",
                            color = difficultyColor
                        )
                    }
                }

                // Reward preview - always visible
                if (quest.rewards.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Rewards: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )

                        val mainReward = quest.rewards.maxByOrNull { it.tier.ordinal }
                        mainReward?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = getRewardTierColor(it.tier),
                                fontWeight = FontWeight.Bold
                            )

                            if (quest.rewards.size > 1) {
                                Text(
                                    text = " +${quest.rewards.size - 1} more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Add expanded content for details
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    @Suppress("DEPRECATION")
                    Divider(color = Color.White.copy(alpha = 0.2f))

                    // Quest type
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Games,
                            contentDescription = null,
                            tint = difficultyColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " Quest Type: ${quest.type.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Required level
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " Required Level: ${quest.requiredLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Completion criteria
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " Completion: ${quest.completionThreshold}/${quest.questionCount} correct",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Detailed rewards section
                    if (quest.rewards.isNotEmpty()) {
                        Text(
                            text = "Rewards:",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )

                        // Show all rewards with a limit of 3 for space
                        val visibleRewards = quest.rewards.take(3)

                        visibleRewards.forEach { reward ->
                            val rewardColor = getRewardTierColor(reward.tier)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
                                ),
                                border = BorderStroke(1.dp, rewardColor.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getRewardTypeIcon(reward.type),
                                        contentDescription = null,
                                        tint = rewardColor,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column {
                                        Text(
                                            text = reward.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = rewardColor
                                        )
                                        Text(
                                            text = reward.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        // Show indicator if there are more rewards
                        if (quest.rewards.size > 3) {
                            Text(
                                text = "+ ${quest.rewards.size - 3} more rewards...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Start quest button - disabled if completed, otherwise at the bottom
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuestSelected(quest.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color.Gray else difficultyColor,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    enabled = !isCompleted
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.PlayArrow,
                        contentDescription = if (isCompleted) "Completed" else "Start Quest",
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isCompleted) "Completed" else "Start Quest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Completed banner overlay - fixed positioning and styling
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(10f),  // Ensure it's on top
                contentAlignment = Alignment.Center
            ) {
                // Diagonal ribbon with "COMPLETED" text
                Box(
                    modifier = Modifier
                        .width(200.dp)  // Adjusted width
                        .height(40.dp)  // Adjusted height
                        .graphicsLayer {
                            rotationZ = -32f  // Diagonal angle
                        }
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.9f),  // Green with slight transparency
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COMPLETED",
                        color = Color.White,
                        fontSize = 18.sp,  // Adjusted font size
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp  // Adjusted letter spacing
                    )
                }
            }
        }
    }
}

@Composable
fun QuestTypeIcon(
    questType: QuestType,
    color: Color
) {
    @Suppress("DEPRECATION") val icon = when (questType) {
        QuestType.STANDARD -> Icons.Filled.Assignment
        QuestType.TIME_ATTACK -> Icons.Filled.Timer
        QuestType.COOPERATIVE -> Icons.Filled.People
        QuestType.CHALLENGE_MODE -> Icons.Filled.EmojiEvents
        QuestType.BOSS_BATTLE -> Icons.Filled.Whatshot
        QuestType.RESEARCH_EXPEDITION -> Icons.Filled.Explore
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                color = color.copy(alpha = 0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = questType.name,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun QuestInfoChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Surface(
        modifier = Modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
fun EmptyQuestsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(
                color = Color(0xFF1A1A1A).copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Explore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No quests available",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Complete more challenges or level up to unlock new quests",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Button(
                onClick = { /* Navigate to daily challenges or something */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Find Challenges")
            }
        }
    }
}

// In QuestSelectionScreen.kt, modify the QuestDetailsOverlay function:

@Composable
fun QuestDetailsOverlay(
    quest: Quest,
    onClose: () -> Unit,
    onStartQuest: () -> Unit
) {
    val difficultyColor = getDifficultyColor(quest.difficulty)
    val scrollState = rememberScrollState() // Add a scroll state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onClose)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card with quest details - add clickable(onClick = {}) to prevent click-through
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // Set a maximum height
                .padding(16.dp)
                .clickable(onClick = {}), // Prevent click-through to the background
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF26004D).copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, difficultyColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Header with difficulty badge and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = difficultyColor,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = quest.difficulty.name,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Scrollable content area for quest details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Quest title and description
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    Text(
                        text = quest.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Quest details
                    DetailItem(
                        icon = Icons.Filled.Category,
                        label = "Category",
                        value = quest.category.replaceFirstChar { it.uppercase() }
                    )

                    DetailItem(
                        icon = Icons.Filled.QuestionAnswer,
                        label = "Questions",
                        value = "${quest.questionCount} (min. ${quest.completionThreshold} correct)"
                    )

                    DetailItem(
                        icon = Icons.Filled.Timer,
                        label = "Time Limit",
                        value = "${quest.timeLimit / 60} minutes"
                    )

                    DetailItem(
                        icon = Icons.Filled.VideogameAsset,
                        label = "Quest Type",
                        value = quest.type.name.replace('_', ' ')
                    )

                    DetailItem(
                        icon = Icons.Filled.Star,
                        label = "Required Level",
                        value = "${quest.requiredLevel}"
                    )

                    if (quest.isTimeLimited && quest.expirationDate != null) {
                        DetailItem(
                            icon = Icons.Filled.Schedule,
                            label = "Available Until",
                            value = "48 hours"  // Placeholder, you'd calculate this from expirationDate
                        )
                    }

                    // Rewards section
                    if (quest.rewards.isNotEmpty()) {
                        Text(
                            text = "Rewards",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // If there are many rewards, we can limit how many we show to save space
                        val rewardsToShow = quest.rewards.take(3) // Show only first 3 rewards

                        rewardsToShow.forEach { reward ->
                            RewardItem(reward = reward)
                        }

                        // Indicate if there are more rewards
                        if (quest.rewards.size > 3) {
                            Text(
                                text = "+ ${quest.rewards.size - 3} more rewards",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Add extra space at bottom for better scrolling
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Start quest button is outside the scrollable area
                Button(
                    onClick = onStartQuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = difficultyColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = "Start Quest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .width(100.dp)
                .padding(start = 8.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RewardItem(reward: Reward) {
    val rewardColor = getRewardTierColor(reward.tier)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, rewardColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reward icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = rewardColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getRewardTypeIcon(reward.type),
                    contentDescription = null,
                    tint = rewardColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reward details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = reward.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = rewardColor,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (reward.xpValue > 0) {
                    Text(
                        text = "+${reward.xpValue} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50)
                    )
                }

                if (reward.multiverseTokenValue > 0) {
                    Text(
                        text = "+${reward.multiverseTokenValue} Tokens",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF00FFFF)
                    )
                }

                Text(
                    text = reward.tier.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = rewardColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Helper functions
private fun getDifficultyColorAndEmoji(difficulty: QuestDifficulty): Pair<Color, String> {
    return when (difficulty) {
        QuestDifficulty.EASY -> Pair(Color(0xFF4CAF50), "🌱")
        QuestDifficulty.MEDIUM -> Pair(Color(0xFF2196F3), "✨")
        QuestDifficulty.HARD -> Pair(Color(0xFFFF9800), "🔥")
        QuestDifficulty.ADEPT -> Pair(Color(0xFF9575CD), "🌟")
        QuestDifficulty.EXPERT -> Pair(Color(0xFFEF5350), "💪")
        QuestDifficulty.LEGENDARY -> Pair(Color(0xFFE91E63), "⚡")
        QuestDifficulty.MULTIVERSE_BREAKER -> Pair(Color(0xFF9C27B0), "🌌")
    }
}

private fun getDifficultyColor(difficulty: QuestDifficulty): Color {
    return getDifficultyColorAndEmoji(difficulty).first
}

private fun getDifficultyIcon(difficulty: QuestDifficulty): ImageVector {
    return when (difficulty) {
        QuestDifficulty.EASY -> Icons.Filled.SentimentVerySatisfied
        QuestDifficulty.MEDIUM -> Icons.Filled.Extension
        QuestDifficulty.HARD -> Icons.Filled.Whatshot
        QuestDifficulty.ADEPT -> Icons.Filled.Psychology
        QuestDifficulty.EXPERT -> Icons.Filled.EmojiEvents
        QuestDifficulty.LEGENDARY -> Icons.Filled.Diamond
        QuestDifficulty.MULTIVERSE_BREAKER -> Icons.Filled.Public
    }
}

private fun getRewardTierColor(tier: RewardTier): Color {
    return when (tier) {
        RewardTier.COMMON -> Color(0xFF78909C)
        RewardTier.RARE -> Color(0xFF29B6F6)
        RewardTier.EPIC -> Color(0xFF9C27B0)
        RewardTier.LEGENDARY -> Color(0xFFFF9800)
        RewardTier.MYTHICAL -> Color(0xFFE91E63)
    }
}

private fun getRewardTypeIcon(type: RewardType): ImageVector {
    return when (type) {
        RewardType.UNIQUE_CHARACTER -> Icons.Filled.Person
        RewardType.COSMETIC_ITEM -> Icons.Filled.Palette
        RewardType.MULTIVERSE_FRAGMENT -> Icons.Filled.Extension
        RewardType.RARE_ACHIEVEMENT -> Icons.Filled.EmojiEvents
        RewardType.EXCLUSIVE_BACKGROUND -> Icons.Filled.Wallpaper
    }
}