package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibesshared.ui.ui.components.TriviaScene
import com.example.vibesshared.ui.ui.components.TriviaSceneRenderer
import com.example.vibesshared.ui.ui.components.rememberTriviaModelsManager
import com.example.vibesshared.ui.ui.data.*
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiverseForgeScreenDetailed(
    viewModel: TriviaGameViewModel,
    onBackToMenu: () -> Unit
) {
    val questState by viewModel.questState.collectAsState()
    val forgeState by viewModel.multivergeForgeState.collectAsState()
    val levelUpState by viewModel.levelUpState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var sceneRenderer by remember { mutableStateOf<TriviaSceneRenderer?>(null) }
    val modelsManager = rememberTriviaModelsManager()

    // Animation states
    var isForging by remember { mutableStateOf(false) }
    var showNewItem by remember { mutableStateOf(false) }
    var forgeProgress by remember { mutableFloatStateOf(0f) }
    var forgeComplete by remember { mutableStateOf(false) }

    // Add scroll state for main content
    val scrollState = rememberScrollState()

    // Get available rewards directly from player progress
    val unlockedRewardIds = questState.playerProgress.unlockedRewards

    // Debug log for how many rewards we have
    LaunchedEffect(unlockedRewardIds) {
        println("MultiverseForge: Found ${unlockedRewardIds.size} unlocked reward IDs")
        unlockedRewardIds.forEach { println("MultiverseForge: Unlocked reward ID: $it") }
    }

    // Convert the IDs to actual Reward objects using our public method
    // Make sure unlockedRewards is not cached too aggressively by adding playerProgress as a key
    val unlockedRewards = remember(unlockedRewardIds, questState.playerProgress) {
        unlockedRewardIds.mapNotNull { rewardId ->
            val reward = viewModel.getRewardById(rewardId)
            if (reward != null) {
                println("MultiverseForge: Found reward: ${reward.name} (${reward.id})")
            } else {
                println("MultiverseForge: Couldn't find reward for ID: $rewardId")
            }
            reward
        }
    }

    // Log how many rewards we actually found
    LaunchedEffect(unlockedRewards) {
        println("MultiverseForge: Successfully loaded ${unlockedRewards.size} rewards for display")
    }

    // Add a specific LaunchedEffect to detect when a new item is forged and force recomposition
    LaunchedEffect(forgeState.forgedReward) {
        if (forgeState.forgedReward != null) {
            println("MultiverseForge: New item forged, inventory should refresh")
        }
    }

    // Handle forging animation and logic
    LaunchedEffect(isForging) {
        if (isForging) {
            // Reset progress
            forgeProgress = 0f

            // Animate the progress from 0 to 100%
            for (i in 1..100) {
                delay(30)
                forgeProgress = i / 100f
            }

            // When animation completes
            forgeComplete = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            delay(1000) // Pause at completion
            showNewItem = true
            isForging = false
            forgeProgress = 0f
            forgeComplete = false
        }
    }

    // Check for new forged reward
    LaunchedEffect(forgeState.forgedReward) {
        if (forgeState.forgedReward != null && !isForging && !showNewItem) {
            showNewItem = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),  // Darker color at the top
                        Color(0xFF3F0071)   // Deep purple at the bottom
                    )
                )
            )
    ) {
        // 3D background scene
        TriviaScene(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f),
            modelsManager = modelsManager,
            onSceneReady = { renderer ->
                sceneRenderer = renderer
            }
        )

        // Floating particles
        FloatingParticles(
            particleCount = 40,
            modifier = Modifier.fillMaxSize()
        )

        // IMPORTANT: Replace Scaffold with direct Column layout to avoid duplicate headers
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)  // Add some top padding to compensate for removed AppBar
        ) {
            // Custom header with back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Back button
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBackToMenu()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Spacing
                Spacer(modifier = Modifier.weight(1f))
            }

            if (isForging) {
                ForgingAnimation3D(
                    progress = forgeProgress,
                    isComplete = forgeComplete
                )
            } else if (showNewItem && forgeState.forgedReward != null) {
                ForgedItemReveal3D(
                    reward = forgeState.forgedReward!!,
                    onDismiss = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showNewItem = false
                    }
                )
            } else {
                // Main forge content - wrap in a scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)  // Make the entire column scrollable
                ) {
                    // Add top padding for better scrolling
                    Spacer(modifier = Modifier.height(8.dp))

                    // Card with forge instructions
                    ForgeInstructionsCard3D()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Selected rewards section with 3D effects
                    Text(
                        text = "Selected Items (${forgeState.selectedRewardIds.size}/2)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SelectedItemsRow(
                        selectedRewards = forgeState.selectedRewardIds.mapNotNull { id ->
                            unlockedRewards.find { it.id == id }
                        },
                        onRemoveItem = { id ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectRewardForForge(id)
                        }
                    )

                    // Forge button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            // Track the selected reward IDs before forging
                            val selectedIds = forgeState.selectedRewardIds.toList()
                            println("MultiverseForge: Forging items with IDs: $selectedIds")

                            // Perform the forge operation
                            viewModel.forgeMultiverseItem()
                            isForging = true

                            // Trigger 3D effect in scene
                            coroutineScope.launch {
                                sceneRenderer?.createQuestCompletedAnimation()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = forgeState.selectedRewardIds.size == 2
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Forge New Item",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Available rewards section
                    Text(
                        text = "Your Inventory",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (unlockedRewards.isEmpty()) {
                        println("MultiverseForge: No rewards to display in inventory")
                        EmptyInventoryMessage()
                    } else {
                        // Display the inventory grid
                        println("MultiverseForge: Displaying ${unlockedRewards.size} rewards in inventory")

                        // Create rows of rewards, 2 per row
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            unlockedRewards.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { reward ->
                                        val isSelected = forgeState.selectedRewardIds.contains(reward.id)
                                        Box(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RewardCard3D(
                                                reward = reward,
                                                isSelected = isSelected,
                                                onSelect = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.selectRewardForForge(reward.id)
                                                }
                                            )
                                        }
                                    }
                                    // If we have an odd number of items, add an empty space
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Add extra space at the bottom for better scrolling
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Level up animation overlay
        AnimatedVisibility(
            visible = levelUpState.showLevelUpAnimation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LevelUpAnimation(
                previousLevel = levelUpState.previousLevel,
                newLevel = levelUpState.newLevel,
                onDismiss = {
                    // Update the level-up state to hide the animation
                    viewModel.dismissLevelUpAnimation()
                }
            )
        }
    }
}

@Composable
fun LevelUpAnimation(
    previousLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }

    // Animation sequence
    LaunchedEffect(Unit) {
        // First, fade in and scale up
        launch {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseOutBack
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        }

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(500)
            )
        }

        // Add rotation for effect
        launch {
            rotationZ.animateTo(
                targetValue = 720f, // 2 full rotations
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = EaseOutQuart
                )
            )
        }
    }

    // Create a glowing effect
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowSize = infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(glowAlpha.value * 0.8f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),  // Gold
                            Color(0xFFFFD700).copy(alpha = 0.5f),
                            Color(0xFFFFD700).copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .graphicsLayer {
                    this.rotationZ = rotationZ.value
                }
        ) {
            Text(
                text = "LEVEL UP!",
                color = Color(0xFFFFD700),  // Gold
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .shadow(glowSize.value.dp, CircleShape)
                    .padding(bottom = 24.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "$previousLevel",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "to",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(32.dp)
                )

                Text(
                    text = "$newLevel",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)  // Gold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "New abilities and quests unlocked!",
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700)  // Gold
                ),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(200.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Particles effect
        repeat(30) { index ->
            val delay = index * 50
            val particleScale = remember { Animatable(0f) }
            val particleAlpha = remember { Animatable(0f) }
            val particleX = remember { Animatable(0f) }
            val particleY = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                delay(delay.toLong())
                launch {
                    particleScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                    particleScale.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }

                launch {
                    particleAlpha.animateTo(
                        targetValue = 0.8f,
                        animationSpec = tween(500)
                    )
                    particleAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(500)
                    )
                }

                launch {
                    val randomX = (Random.nextFloat() * 300f) - 150f
                    val randomY = (Random.nextFloat() * 300f) - 150f

                    particleX.animateTo(
                        targetValue = randomX,
                        animationSpec = tween(1000)
                    )
                    particleY.animateTo(
                        targetValue = randomY,
                        animationSpec = tween(1000)
                    )
                }
            }



            val particleColor = when (index % 3) {
                0 -> Color(0xFFFFD700)  // Gold
                1 -> Color(0xFFFFFFFF)  // White
                else -> Color(0xFFFFA500)  // Orange
            }

            Box(
                modifier = Modifier
                    .offset(x = particleX.value.dp, y = particleY.value.dp)
                    .size((5 + index % 5).dp * particleScale.value)
                    .alpha(particleAlpha.value)
                    .background(particleColor, CircleShape)
            )
        }
    }
}

@Composable
fun ForgeInstructionsCard3D() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 8f
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF512DA8).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFD700).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Forge Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Select two items from your inventory to forge them into a more powerful item. Higher tier items have a better chance of creating legendary gear!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SelectedItemsRow(
    selectedRewards: List<Reward>,
    onRemoveItem: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val elevation = infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotate = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = Color(0xFF26004D).copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // First item slot
            if (selectedRewards.isNotEmpty()) {
                val firstReward = selectedRewards.first()
                SelectedForgeItem(
                    reward = firstReward,
                    onRemove = { onRemoveItem(firstReward.id) },
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            shadowElevation = elevation.value
                            rotationZ = rotate.value
                        }
                )
            } else {
                EmptyForgeSlot(modifier = Modifier.weight(1f))
            }

            // Plus icon with glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Second item slot
            if (selectedRewards.size > 1) {
                val secondReward = selectedRewards[1]
                SelectedForgeItem(
                    reward = secondReward,
                    onRemove = { onRemoveItem(secondReward.id) },
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            shadowElevation = elevation.value
                            rotationZ = -rotate.value
                        }
                )
            } else {
                EmptyForgeSlot(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EmptyForgeSlot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .height(80.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF9800).copy(alpha = borderAlpha.value),
                        Color(0xFFE91E63).copy(alpha = borderAlpha.value)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color(0xFF1A1A1A).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AddCircleOutline,
            contentDescription = "Empty Slot",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun SelectedForgeItem(
    reward: Reward,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rewardColor = getRewardTierColor(reward.tier)
    val infiniteTransition = rememberInfiniteTransition()
    val glowStrength = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        rewardColor.copy(alpha = 0.5f),
                        Color(0xFF26004D).copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        rewardColor.copy(alpha = glowStrength.value),
                        Color.White.copy(alpha = glowStrength.value * 0.5f),
                        rewardColor.copy(alpha = glowStrength.value)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = reward.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = reward.tier.name,
                style = MaterialTheme.typography.labelSmall,
                color = rewardColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RewardCard3D(
    reward: Reward,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val rewardColor = getRewardTierColor(reward.tier)
    val haptic = LocalHapticFeedback.current

    // Animations
    val infiniteTransition = rememberInfiniteTransition()
    val scale = remember { Animatable(1f) }

    // Define elevation animation unconditionally
    val elevationAnimation = infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateZ = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(isSelected) {
        scale.animateTo(
            targetValue = if (isSelected) 1.05f else 1f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .graphicsLayer {
                if (isSelected) {
                    shadowElevation = elevationAnimation.value
                    rotationZ = rotateZ.value * 2f
                }
            }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelect()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) rewardColor.copy(alpha = 0.3f) else Color(0xFF26004D).copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) rewardColor else rewardColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reward icon with tray effect
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                rewardColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getRewardTypeIcon(reward.type),
                    contentDescription = null,
                    tint = rewardColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reward.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) rewardColor else Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.height(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reward stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (reward.xpValue > 0) {
                    RewardStat(
                        icon = Icons.Filled.Star,
                        value = "+${reward.xpValue}",
                        color = Color(0xFF4CAF50)
                    )
                }

                if (reward.multiverseTokenValue > 0) {
                    RewardStat(
                        icon = Icons.Filled.Token,
                        value = "+${reward.multiverseTokenValue}",
                        color = Color(0xFF00FFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tier badge
            Surface(
                color = rewardColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, rewardColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = reward.tier.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = rewardColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Selection indicator
            if (isSelected) {
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = rewardColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "SELECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardStat(
    icon: ImageVector,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyInventoryMessage() {
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
                imageVector = Icons.Filled.Inventory,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your inventory is empty",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Complete quests to earn rewards that you can forge into powerful items!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = { /* Navigate to quests */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Find Quests")
            }
        }
    }
}

@Composable
fun ForgingAnimation3D(
    progress: Float,
    isComplete: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    val scale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val particleAlpha = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Flash effect when complete
    val flash = remember { Animatable(0f) }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            flash.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
            flash.animateTo(
                targetValue = 0f,
                animationSpec = tween(300)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Flash effect overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(flash.value)
                .background(Color.White)
        )

        // Particle effects - scale with progress
        repeat(20) { index ->
            val angle = (index / 20f) * 360f
            val radius = 150f * progress
            val x = kotlin.math.cos(Math.toRadians(angle.toDouble() + rotationAngle.value)).toFloat() * radius
            val y = kotlin.math.sin(Math.toRadians(angle.toDouble() + rotationAngle.value)).toFloat() * radius

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size((5 + index % 10).dp)
                    .alpha(particleAlpha.value * progress)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                getForgeParticleColor(index),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Background glow - scales with progress
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(progress)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9800),
                            Color(0xFFFF9800).copy(alpha = 0.5f),
                            Color(0xFFFF9800).copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Rotating magic circle - scales with progress
        Box(
            modifier = Modifier
                .size(150.dp)
                .rotate(rotationAngle.value)
                .scale(progress)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFE91E63),
                            Color(0xFF9C27B0),
                            Color(0xFF3F51B5),
                            Color(0xFF00BCD4),
                            Color(0xFF4CAF50),
                            Color(0xFFFFEB3B),
                            Color(0xFFFF9800),
                            Color(0xFFE91E63)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner circle - scales with progress and animation
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale.value * progress)
                .rotate(-rotationAngle.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFD700)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Forge icon - scales with progress and animation
        Icon(
            imageVector = Icons.Filled.AutoFixHigh,
            contentDescription = "Forging",
            tint = Color(0xFF512DA8),
            modifier = Modifier
                .size(48.dp)
                .scale(scale.value * progress)
        )

        // Progress text
        Text(
            text = if (isComplete) "Forging complete!" else "Forging: ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )

        // Progress indicator
        if (!isComplete) {
            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF9800),
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ForgedItemReveal3D(
    reward: Reward,
    onDismiss: () -> Unit
) {
    val initialScale = remember { Animatable(0.1f) }
    val rewardColor = getRewardTierColor(reward.tier)
    val infiniteTransition = rememberInfiniteTransition()

    val glowRadius = infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateZ = infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val particleAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(reward) {
        initialScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Particle effects around the card
        repeat(30) { index ->
            val angle = (index / 30f) * 360f
            val radius = 200f
            val x = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size((3 + index % 5).dp)
                    .alpha(particleAlpha.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                rewardColor,
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Card(
            modifier = Modifier
                .width(300.dp)
                .scale(initialScale.value)
                .graphicsLayer {
                    shadowElevation = glowRadius.value
                    rotationZ = rotateZ.value
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF3F0071).copy(alpha = 0.95f)
            ),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        rewardColor,
                        Color.White,
                        rewardColor
                    )
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "NEW ITEM FORGED!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Item icon/representation with glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    rewardColor,
                                    rewardColor.copy(alpha = 0.5f),
                                    Color(0xFF3F0071).copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getRewardTypeIcon(reward.type),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reward tier badge
                Surface(
                    modifier = Modifier.padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = rewardColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, rewardColor)
                ) {
                    Text(
                        text = reward.tier.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = rewardColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = reward.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats with 3D effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ForgedItemStat(
                        icon = Icons.Filled.Star,
                        value = "+${reward.xpValue}",
                        label = "XP"
                    )

                    if (reward.multiverseTokenValue > 0) {
                        ForgedItemStat(
                            icon = Icons.Filled.Token,
                            value = "+${reward.multiverseTokenValue}",
                            label = "Tokens"
                        )
                    }

                    ForgedItemStat(
                        icon = Icons.Filled.Diamond,
                        value = "${(reward.rarity * 100).toInt()}%",
                        label = "Rarity"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rewardColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Claim Reward")
                }
            }
        }
    }
}


@Composable
fun ForgedItemStat(
    icon: ImageVector,
    value: String,
    label: String
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale.value)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

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

// Helper functions
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

private fun getForgeParticleColor(index: Int): Color {
    return when (index % 5) {
        0 -> Color(0xFFFF5722) // Orange
        1 -> Color(0xFFFFEB3B) // Yellow
        2 -> Color(0xFF4CAF50) // Green
        3 -> Color(0xFF03A9F4) // Blue
        else -> Color(0xFFE91E63) // Pink
    }
}