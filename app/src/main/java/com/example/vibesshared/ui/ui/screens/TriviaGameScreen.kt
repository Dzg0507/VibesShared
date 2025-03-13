package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.LightningBolt
import com.example.vibesshared.ui.ui.components.LightningBoltAnimation
import com.example.vibesshared.ui.ui.components.TriviaScene
import com.example.vibesshared.ui.ui.components.TriviaSceneRenderer
import com.example.vibesshared.ui.ui.components.createLightningPath
import com.example.vibesshared.ui.ui.components.rememberTriviaModelsManager
import com.example.vibesshared.ui.ui.data.PlayerProgress
import com.example.vibesshared.ui.ui.data.xpForNextLevel
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TriviaGameScreen(
    navController: NavController,
    viewModel: TriviaGameViewModel = hiltViewModel()
) {
    val questState by viewModel.questState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val powerUpState by viewModel.powerUpState.collectAsState()
    val streakState by viewModel.streakState.collectAsState()
    val haptic = LocalHapticFeedback.current

    var currentScreen by remember { mutableStateOf("menu") }
    var isGameInProgress by remember { mutableStateOf(false) }
    var sceneRenderer by remember { mutableStateOf<TriviaSceneRenderer?>(null) }
    val modelsManager = rememberTriviaModelsManager()

    // 3D scene visible state
    var showScene by remember { mutableStateOf(true) }

    // Game over animation
    var showGameOverAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showSettings) {
        if (!uiState.showSettings) {
            isGameInProgress = true
        }
    }

    // Handle game over
    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            showGameOverAnimation = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            // No more auto-dismiss, we let the user dismiss with the button
        }
    }


    // Main box for the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0026), Color(0xFF26004D))
                )
            )
    ) {
        // 3D Scene Background
        if (showScene) {
            TriviaScene(
                modifier = Modifier.fillMaxSize(),
                modelsManager = modelsManager,  // Add this parameter
                onSceneReady = { renderer ->
                    sceneRenderer = renderer
                },
                onInteraction = { rotX, rotY ->
                    // Update camera when user interacts with scene
                    sceneRenderer?.rotateCameraSlightly(rotX, rotY)
                }
            )
        }



        // Game content with scaffolding
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                "menu" -> "Multiverse Trivia"
                                "quests" -> "Multiverse Quests"
                                "forge" -> "Multiverse Forge"
                                "shop" -> "Power-Up Shop"
                                "game" -> "Trivia Game"
                                else -> "Multiverse Trivia"
                            },
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (currentScreen == "menu") {
                                navController.popBackStack()
                            } else if (isGameInProgress) {
                                // Show confirmation dialog before exiting game
                                currentScreen = "menu"
                                viewModel.resetGame()
                                isGameInProgress = false
                            } else {
                                currentScreen = "menu"
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Show audiovisual control
                        IconButton(onClick = { viewModel.toggleAudio() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = "Toggle Audio",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent // Make scaffold background transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    isGameInProgress -> {
                        if (uiState.showSettings) {
                            TriviaMenuScreen(viewModel = viewModel)
                        } else {
                            TriviaGameUI(
                                viewModel = viewModel,
                                uiState = uiState,
                                questState = questState,
                                powerUpState = powerUpState
                            )
                        }
                    }
                    else -> {
                        when (currentScreen) {
                            "menu" -> {
                                MainMenu3D(
                                    questState = questState,
                                    onQuickPlay = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.resetGame()
                                        isGameInProgress = true
                                    },
                                    onShowQuests = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentScreen = "quests"
                                    },
                                    onShowForge = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentScreen = "forge"
                                    },
                                    onShowShop = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentScreen = "shop"
                                    },
                                    sceneRenderer = sceneRenderer
                                )
                            }
                            "quests" -> {
                                QuestSelectionScreenDetailed(
                                    viewModel = viewModel,
                                    onStartQuest = {
                                        isGameInProgress = true
                                        currentScreen = "game"
                                    },
                                    onBackToMenu = { currentScreen = "menu" }
                                )
                            }
                            "forge" -> {
                                MultiverseForgeScreenDetailed(
                                    viewModel = viewModel,
                                    onBackToMenu = { currentScreen = "menu" }
                                )
                            }
                            "shop" -> {
                                PowerUpShopScreen(
                                    viewModel = viewModel,
                                    powerUpState = powerUpState,
                                    questState = questState,
                                    onBackToMenu = { currentScreen = "menu" }
                                )
                            }
                        }
                    }
                }

                // Streak notification
                // Game over overlay
                AnimatedVisibility(
                    visible = showGameOverAnimation,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    StreakNotification(
                        streakCount = streakState.streakCount,
                        bonusXp = streakState.bonusXp,
                        bonusDescription = streakState.bonusDescription
                    )
                    GameOverOverlay(
                        score = uiState.score,
                        totalQuestions = uiState.questions.size,
                        questCompleted = questState.activeQuest?.let {
                            uiState.score >= it.completionThreshold
                        } == true,
                        questTitle = questState.activeQuest?.title ?: "",
                        awardedRewards = uiState.awardedRewards,
                        onDismiss = {
                            // Handle dismiss - close overlay and reset game
                            showGameOverAnimation = false
                            isGameInProgress = false
                            currentScreen = "menu"
                            viewModel.resetGame()
                        }
                    )
                }
            }
        }

        // Add floating particles to the scene for more visual interest
        GameFloatingParticles(
            particleCount = Random.nextInt(50, 90), // Random number between 30 and 50 (inclusive)
            modifier = Modifier.fillMaxSize()
        )    }
}




@Composable
fun GameFloatingParticles(
    particleCount: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Background stars
        repeat(particleCount) {
            // Random position across entire canvas
            val x = Random.nextFloat() * canvasWidth
            val y = Random.nextFloat() * canvasHeight

            // Random properties for variation
            val starSize = Random.nextFloat() * 2.5f + 0.5f
            val brightness = Random.nextFloat() * 0.7f + 0.3f

            // Determine star type
            val starType = when {
                starSize > 2.5f -> "bright" // Larger, brighter stars
                starSize > 1.5f -> "medium"
                else -> "dim" // Small, distant stars
            }

            // Star color with alpha based on brightness
            val color = when (Random.nextInt(7)) {
                0 -> Color(0xFFFF9800) // Orange
                1 -> Color(0xFF00BCD4) // Cyan
                2 -> Color(0xFFFFFFFF) // White
                3 -> Color(0xFFFFD700) // Gold
                4 -> Color(0xFF64B5F6) // Light Blue
                5 -> Color(0xFFE1BEE7) // Light Purple
                else -> Color(0xFF90CAF9) // Very Light Blue
            }.copy(alpha = brightness)

            when (starType) {
                "bright" -> {
                    // Draw star with glow
                    drawCircle(
                        color = color.copy(alpha = brightness * 0.3f),
                        radius = starSize * 2,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                    drawCircle(
                        color = color,
                        radius = starSize,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
                "medium" -> {
                    // Draw medium star
                    drawCircle(
                        color = color,
                        radius = starSize,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
                else -> {
                    // Draw dim star
                    drawCircle(
                        color = color,
                        radius = starSize,
                        center = Offset(x, y)
                    )
                }
            }

            // Add lens flare for some brighter stars
            if (starType == "bright" && Random.nextFloat() > 0.7f) {
                val flareSize = starSize * 4
                drawLine(
                    color = color.copy(alpha = brightness * 0.2f),
                    start = Offset((x - flareSize).toFloat(), y.toFloat()),
                    end = Offset((x + flareSize).toFloat(), y.toFloat()),
                    strokeWidth = starSize * 0.3f
                )
                drawLine(
                    color = color.copy(alpha = brightness * 0.2f),
                    start = Offset(x.toFloat(), (y - flareSize).toFloat()),
                    end = Offset(x.toFloat(), (y + flareSize).toFloat()),
                    strokeWidth = starSize * 0.3f
                )
            }
        }

        // Add a few shooting stars
        val shootingStarCount = 3
        repeat(shootingStarCount) {
            val startX = Random.nextFloat() * canvasWidth
            val startY = Random.nextFloat() * canvasHeight * 0.3f // Top third of screen

            val length = 100f + Random.nextFloat() * 150f
            val angle = 45f + Random.nextFloat() * 90f // Downward angle
            val radians = angle * (Math.PI / 180f)

            val endX = startX + cos(radians).toFloat() * length
            val endY = startY + sin(radians).toFloat() * length

            // Draw shooting star
            drawLine(
                color = Color.White,
                start = Offset(startX.toFloat(), startY.toFloat()),
                end = Offset(endX.toFloat(), endY.toFloat()),
                strokeWidth = 1.5f
            )

            // Add glow
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(startX.toFloat(), startY.toFloat()),
                end = Offset(endX.toFloat(), endY.toFloat()),
                strokeWidth = 3f
            )
        }
    }
}
// Add this function to your TriviaGameScreen.kt file
// Place it near the other UI component functions, like EnhancedMainMenuTitle, EnhancedStatisticItem, etc.

@Composable
fun EnhancedMainMenuButton(
    text: String,
    icon: ImageVector,
    color: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val buttonGlow = infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Border gradient animation
    val borderAlpha = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale.value)
            .graphicsLayer {
                shadowElevation = buttonGlow.value
            }
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        color,
                        secondaryColor.copy(alpha = borderAlpha.value),
                        color
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with pulsating effect
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                color.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text with drop shadow
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MainMenu3D(
    questState: com.example.vibesshared.ui.ui.viewmodel.QuestUiState,
    onQuickPlay: () -> Unit,
    onShowQuests: () -> Unit,
    onShowForge: () -> Unit,
    onShowShop: () -> Unit,
    sceneRenderer: TriviaSceneRenderer?
) {
    // Create a scroll state
    val scrollState = rememberScrollState()
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    // Lightning bolts for visual effects
    val bolts = remember { mutableStateListOf<LightningBolt>() }
    val coroutineScope = rememberCoroutineScope()

    // Animate floating elements
    val infiniteTransition = rememberInfiniteTransition()
    val floatY = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Title 3D animation effects
    val titleGlow = infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val titleScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 3D rotation for title
    var rotationY = infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dynamic color animation for the title
    val titleColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF00FFFF),  // Cyan
        targetValue = Color(0xFFFF00FF),   // Magenta
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Secondary color animation for gradients
    val secondaryColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFD700),  // Gold
        targetValue = Color(0xFF00FF00),   // Green
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Background star twinkle effect
    val starTwinkle = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Create background stars
    val stars = remember {
        List(150) {
            Triple(
                Random.nextFloat() * screenSize.width.coerceAtLeast(1),
                Random.nextFloat() * screenSize.height.coerceAtLeast(1),
                Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }

    // Create occasional auto-generated lightning
    LaunchedEffect(Unit) {
        scheduleAutomaticLightning(screenSize, bolts, coroutineScope)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050014), // Darker deep space color
                        Color(0xFF0D0026),
                        Color(0xFF26004D)
                    )
                )
            )
            .drawBehind {
                // Draw stars
                stars.forEach { (x, y, brightness) ->
                    val starSize = brightness * 4f
                    val glow = (brightness * 0.5f + starTwinkle.value * 0.5f) * 255

                    drawCircle(
                        color = Color.White.copy(alpha = glow / 255f),
                        radius = starSize,
                        center = Offset(x, y)
                    )

                    // Occasional lens flare for brighter stars
                    if (brightness > 0.8f) {
                        drawLine(
                            color = Color.White.copy(alpha = (glow / 255f) * 0.3f),
                            start = Offset(x - starSize * 2, y),
                            end = Offset(x + starSize * 2, y),
                            strokeWidth = starSize * 0.5f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = (glow / 255f) * 0.3f),
                            start = Offset(x, y - starSize * 2),
                            end = Offset(x, y + starSize * 2),
                            strokeWidth = starSize * 0.5f
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val startEdge = Random.nextInt(4)
                    val start = when (startEdge) {
                        0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                        1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                        2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                        else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                    }

                    val color = when (Random.nextInt(3)) {
                        0 -> Color(0xFFFF1744)
                        1 -> Color(0xFF00E676)
                        else -> Color(0xFF2979FF)
                    }

                    val path = createLightningPath(start, offset)
                    bolts.add(LightningBolt(Random.nextInt(), start, offset, color, path))

                    // Add secondary smaller lightning after a short delay
                    coroutineScope.launch {
                        delay(100)
                        val secondaryStart = Offset(
                            start.x + Random.nextFloat() * 50 - 25,
                            start.y + Random.nextFloat() * 50 - 25
                        )
                        val secondaryPath = createLightningPath(secondaryStart, offset)
                        bolts.add(LightningBolt(Random.nextInt(), secondaryStart, offset, color.copy(alpha = 0.7f), secondaryPath))
                    }

                    // Trigger a special animation in the 3D scene
                    sceneRenderer?.triggerLightningEffect(offset.x, offset.y)
                }
            }
            .onSizeChanged { screenSize = it }
    ) {
        // Draw lightning bolts
        bolts.forEach { bolt ->
            key(bolt.id) {
                LightningBoltAnimation(bolt) { bolts.remove(bolt) }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)  // Make the entire column scrollable
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add some top padding for better scrolling
            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced 3D game title
            EnhancedMainMenuTitle(
                titleScale = titleScale.value,
                titleGlow = titleGlow.value,
                titleColor = titleColor,
                secondaryColor = secondaryColor,
                rotationY = rotationY.value
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Player Stats Card - with 3D floating effect
            Box(
                modifier = Modifier
                    .offset(y = floatY.value.dp)
                    .fillMaxWidth()
            ) {
                EnhancedStatisticsCard(
                    playerProgress = questState.playerProgress,
                    primaryColor = titleColor,
                    secondaryColor = secondaryColor
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Main Menu Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedMainMenuButton(
                    text = "Quick Play",
                    icon = Icons.Filled.SportsEsports,
                    color = Color(0xFF4CAF50),
                    secondaryColor = secondaryColor,
                    onClick = onQuickPlay
                )

                EnhancedMainMenuButton(
                    text = "Multiverse Quests",
                    icon = Icons.Filled.Explore,
                    color = Color(0xFF2196F3),
                    secondaryColor = titleColor,
                    onClick = onShowQuests
                )

                EnhancedMainMenuButton(
                    text = "Multiverse Forge",
                    icon = Icons.Filled.Build,
                    color = Color(0xFFFF9800),
                    secondaryColor = secondaryColor,
                    onClick = onShowForge
                )

                EnhancedMainMenuButton(
                    text = "Power-Up Shop",
                    icon = Icons.Filled.ShoppingCart,
                    color = Color(0xFFE91E63),
                    secondaryColor = titleColor,
                    onClick = onShowShop
                )
            }

            // Add significant bottom padding for better scrolling
            if (questState.playerProgress.completedQuests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Quests Completed: ${questState.playerProgress.completedQuests.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Add extra space at bottom for scrolling
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
private fun scheduleAutomaticLightning(
    screenSize: IntSize,
    bolts: MutableList<LightningBolt>,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        while(true) {
            // Decreased delay - random delay between 1-4 seconds
            delay(Random.nextLong(1000, 4000))

            // 80% chance of lightning (increased from previous)
            if (Random.nextFloat() > 0.2f) {
                val startEdge = Random.nextInt(4)
                val start = when (startEdge) {
                    0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                    1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                    2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                    else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                }

                // Slightly guide the lightning toward the center area for better visibility
                val centerOffsetX = Random.nextFloat() * screenSize.width * 0.6f + screenSize.width * 0.2f
                val centerOffsetY = Random.nextFloat() * screenSize.height * 0.6f + screenSize.height * 0.2f

                val end = Offset(
                    centerOffsetX,
                    centerOffsetY
                )

                val color = when (Random.nextInt(5)) {
                    0 -> Color(0xFFFF1744) // Red
                    1 -> Color(0xFF00E676) // Green
                    2 -> Color(0xFF2979FF) // Blue
                    3 -> Color(0xFFFFEB3B) // Yellow
                    else -> Color(0xFFE040FB) // Purple
                }

                val path = createLightningPath(start, end)
                bolts.add(LightningBolt(Random.nextInt(), start, end, color, path))

                // 50% chance of a second bolt following shortly after
                if (Random.nextFloat() > 0.5f) {
                    delay(100)
                    val secondaryStart = Offset(
                        start.x + Random.nextFloat() * 50 - 25,
                        start.y + Random.nextFloat() * 50 - 25
                    )
                    val secondaryPath = createLightningPath(secondaryStart, end)
                    bolts.add(LightningBolt(Random.nextInt(), secondaryStart, end, color.copy(alpha = 0.8f), secondaryPath))
                }

                // 25% chance of a third bolt for a proper storm effect
                if (Random.nextFloat() > 0.75f) {
                    delay(200)
                    val tertiaryStart = when (startEdge) {
                        0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                        1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                        2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                        else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                    }
                    val tertiaryPath = createLightningPath(tertiaryStart, Offset(
                        centerOffsetX + Random.nextFloat() * 100f - 50f,
                        centerOffsetY + Random.nextFloat() * 100f - 50f
                    ))

                    val tertiaryColor = when (Random.nextInt(5)) {
                        0 -> Color(0xFFFF1744) // Red
                        1 -> Color(0xFF00E676) // Green
                        2 -> Color(0xFF2979FF) // Blue
                        3 -> Color(0xFFFFEB3B) // Yellow
                        else -> Color(0xFFE040FB) // Purple
                    }

                    bolts.add(LightningBolt(Random.nextInt(), tertiaryStart, end, tertiaryColor, tertiaryPath))
                }
            }
        }
    }
}

// Other composables remain unchanged (EnhancedMainMenuTitle, EnhancedStatisticItem, etc.)
// To keep the file length manageable, I've omitted them here, but they should be included in your actual implementation

@Composable
fun EnhancedMainMenuTitle(
    titleScale: Float,
    titleGlow: Float,
    titleColor: Color,
    secondaryColor: Color,
    rotationY: Float
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Saturn ring rotation animation
    val ringRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart


        )
    )


    // Subtle pulsing animation for the rings
    val ringPulse = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        val centerX = maxWidth.value / 2
        val centerY = maxHeight.value / 2

        // Background glow
        Box(
            modifier = Modifier
                .size(350.dp, 180.dp)
                .graphicsLayer {
                    shadowElevation = titleGlow
                    shape = RoundedCornerShape(24.dp)
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            titleColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Particles explicitly drawn centered around the title
        CosmicParticles(centerX, centerY, titleColor, secondaryColor)

        ShootingStars(centerX, centerY)


        // Saturn's Ring - Outer
        Box(
            modifier = Modifier
                .size(380.dp, 100.dp)
                .graphicsLayer {
                    rotationZ = ringRotation.value
                    alpha = ringPulse.value * 0.8f
                }
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.8f),
                            secondaryColor.copy(alpha = 0.6f),
                            secondaryColor.copy(alpha = 0.3f),
                            secondaryColor.copy(alpha = 0.1f),
                            secondaryColor.copy(alpha = 0.3f),
                            secondaryColor.copy(alpha = 0.6f),
                            secondaryColor.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(50.dp)
                )
        )

        // Saturn's Ring - Inner (rotating opposite direction)
        Box(
            modifier = Modifier
                .size(320.dp, 80.dp)
                .graphicsLayer {
                    rotationZ = -ringRotation.value * 0.7f
                    alpha = ringPulse.value * 0.7f
                }
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            titleColor.copy(alpha = 0.7f),
                            titleColor.copy(alpha = 0.5f),
                            titleColor.copy(alpha = 0.2f),
                            titleColor.copy(alpha = 0.1f),
                            titleColor.copy(alpha = 0.2f),
                            titleColor.copy(alpha = 0.5f),
                            titleColor.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(40.dp)
                )
        )

        // Main title with 3D effect
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = titleScale
                    scaleY = titleScale
                    this.rotationY = rotationY
                    cameraDistance = 12f * density
                    shadowElevation = titleGlow
                }
        ) {
            // MULTIVERSE text
            Text(
                text = "MULTIVERSE",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            titleColor,
                            Color.White,
                            titleColor
                        )
                    ),
                    shadow = Shadow(
                        color = titleColor.copy(alpha = 0.7f),
                        offset = Offset(2f, 4f),
                        blurRadius = titleGlow
                    )
                )
            )

            // TRIVIA text
            Text(
                text = "TRIVIA",
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),  // Gold
                            Color.White,
                            Color(0xFFFFD700)   // Gold
                        )
                    ),
                    shadow = Shadow(
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        offset = Offset(3f, 5f),
                        blurRadius = titleGlow
                    )
                )
            )
        }
    }
}


@Composable
fun CosmicParticles(centerX: Float, centerY: Float, titleColor: Color, secondaryColor: Color) {
    // Add animation for orbital rotation
    val infiniteTransition = rememberInfiniteTransition()
    val orbitAngleOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90000, easing = LinearEasing), // Very slow 90-second orbit
            repeatMode = RepeatMode.Restart
        )
    )

    // Fixed positioning approach using Canvas for direct drawing
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Using passed centerX and centerY directly - no need to calculate

        // ORBITAL PARTICLES
        val orbitCount = 50
        for (i in 0 until orbitCount) {
            // Apply the animated angle offset for slow rotation
            val angle = ((i * 360f / orbitCount) + orbitAngleOffset.value) * (Math.PI / 180f)
            val orbitRadius = 120f + Random.nextFloat() * 50f

            // Elliptical orbit
            val xRadius = orbitRadius
            val yRadius = orbitRadius * 0.6f

            val x = centerX + cos(angle) * xRadius
            val y = centerY + sin(angle) * yRadius

            val particleSize = 2f + Random.nextFloat() * 3f

            val particleColor = when (i % 5) {
                0 -> titleColor
                1 -> secondaryColor
                2 -> Color.White
                3 -> Color(0xFFFFD700) // Gold
                else -> Color(0xFF00FFFF) // Cyan
            }

            // Draw the particle
            drawCircle(
                color = particleColor,
                radius = particleSize,
                center = Offset(x.toFloat(), y.toFloat())
            )

            // Add glow for larger particles
            if (particleSize > 3f) {
                drawCircle(
                    color = particleColor.copy(alpha = 0.3f),
                    radius = particleSize * 2,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }

        // STAR PARTICLES in wider area
        val starCount = 70
        for (i in 0 until starCount) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val distance = 100f + Random.nextFloat() * 200f

            val x = centerX + cos(angle) * distance
            val y = centerY + sin(angle) * distance

            val starSize = 1f + Random.nextFloat() * 2f

            val starColor = when (Random.nextInt(6)) {
                0 -> Color.White
                1 -> Color(0xFFFFD700) // Gold
                2 -> titleColor
                3 -> secondaryColor
                4 -> Color(0xFF29B6F6) // Light blue
                else -> Color(0xFFE1BEE7) // Light purple
            }

            // Draw different star shapes
            if (i % 5 == 0) {
                // Create a 4-point star
                val path = Path().apply {
                    val outerRadius = starSize * 3
                    val innerRadius = starSize

                    for (p in 0 until 8) {
                        val pointAngle = p * Math.PI / 4
                        val r = if (p % 2 == 0) outerRadius else innerRadius
                        val px = x + cos(pointAngle).toFloat() * r
                        val py = y + sin(pointAngle).toFloat() * r

                        if (p == 0) moveTo(px, py)
                        else lineTo(px, py)
                    }
                    close()
                }

                drawPath(
                    path = path,
                    color = starColor,
                    style = Fill
                )
            } else {
                // Simple circle star
                drawCircle(
                    color = starColor,
                    radius = starSize,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }

        // Add a few shooting stars
        val shootingStarCount = 1 // Fewer to avoid overwhelm
        (0 until shootingStarCount).forEach {_ ->
            val startAngle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val startDistance = 250f
            val startX = centerX + cos(startAngle).toFloat() * startDistance
            val startY = centerY + sin(startAngle).toFloat() * startDistance

            // Calculate end point (opposite side)
            val endAngle = startAngle + Math.PI.toFloat()
            val endDistance = 250f
            val endX = centerX + cos(endAngle).toFloat() * endDistance
            val endY = centerY + sin(endAngle).toFloat() * endDistance

            // Create trail
            drawLine(
                color = Color.White,
                start = Offset(startX.toFloat(), startY.toFloat()),
                end = Offset(endX.toFloat(), endY.toFloat()),
                strokeWidth = 1.5f
            )

            // Add glow around trail
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(startX.toFloat(), startY.toFloat()),
                end = Offset(endX.toFloat(), endY.toFloat()),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun ShootingStars(centerX: Float, centerY: Float) {
    val coroutineScope = rememberCoroutineScope()

    // Track shooting stars
    val shootingStars = remember { mutableStateListOf<ShootingStar>() }

    // Periodically create new shooting stars
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(Random.nextLong(3000, 8000))

            // Randomly decide if we should create a shooting star
            if (Random.nextFloat() > 0.3f) {
                // Create a new shooting star
                val id = Random.nextInt()
                val startAngle = Random.nextFloat() * 360f
                val angleRad = startAngle * Math.PI.toFloat() / 180f

                // Start from outside the visible area
                val startRadius = 220f + Random.nextFloat() * 80f
                val startX = centerX + cos(angleRad) * startRadius
                val startY = centerY + sin(angleRad) * startRadius

                // Calculate end position (across the center)
                val endAngle = (startAngle + 180f + Random.nextFloat() * 60f - 30f) % 360f
                val endAngleRad = endAngle * Math.PI.toFloat() / 180f
                val endRadius = 220f + Random.nextFloat() * 80f
                val endX = centerX + cos(endAngleRad) * endRadius
                val endY = centerY + sin(endAngleRad) * endRadius

                // Create the shooting star
                val star = ShootingStar(
                    id = id,
                    startX = startX,
                    startY = startY,
                    endX = endX,
                    endY = endY,
                    size = 2f + Random.nextFloat() * 2f,
                    color = when (Random.nextInt(4)) {
                        0 -> Color.White
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFF00FFFF) // Cyan
                        else -> Color(0xFFFF00FF) // Magenta
                    },
                    durationMs = 800 + Random.nextInt(400)
                )

                shootingStars.add(star)

                // Start animation
                coroutineScope.launch {
                    val xAnim = Animatable(star.startX)
                    val yAnim = Animatable(star.startY)
                    val alphaAnim = Animatable(0f)

                    // Store animation values in the star
                    star.xAnim = xAnim
                    star.yAnim = yAnim
                    star.alphaAnim = alphaAnim

                    // Fade in
                    launch {
                        alphaAnim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(200, easing = LinearEasing)
                        )
                    }

                    // Move
                    launch {
                        xAnim.animateTo(
                            targetValue = star.endX,
                            animationSpec = tween(star.durationMs, easing = LinearEasing)
                        )
                    }

                    launch {
                        yAnim.animateTo(
                            targetValue = star.endY,
                            animationSpec = tween(star.durationMs, easing = LinearEasing)
                        )
                    }

                    // Fade out as it approaches the end
                    delay(star.durationMs - 200L)
                    alphaAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(200, easing = LinearEasing)
                    )

                    // Remove from list
                    shootingStars.remove(star)
                }
            }
        }
    }

    // Draw all active shooting stars
    shootingStars.forEach { star ->
        // Only draw if animations are initialized
        if (star.xAnim != null && star.yAnim != null && star.alphaAnim != null) {
            val x = star.xAnim!!.value
            val y = star.yAnim!!.value
            val alpha = star.alphaAnim!!.value

            // Draw shooting star with trail
            Box(
                modifier = Modifier
                    .offset(x = x.dp - (star.size / 2).dp, y = y.dp - (star.size / 2).dp)
                    .graphicsLayer {
                        this.alpha = alpha
                    }
                    .drawBehind {
                        // Calculate trail points
                        val trailLength = 20f * star.size
                        val dx = x - star.startX
                        val dy = y - star.startY
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance > 0) {
                            val normalizedDx = dx / distance
                            val normalizedDy = dy / distance

                            // Draw multiple segments with decreasing opacity
                            for (i in 1..5) {
                                val trailSegment = trailLength * i / 5f
                                val trailX = x - normalizedDx * trailSegment
                                val trailY = y - normalizedDy * trailSegment

                                val trailAlpha = alpha * (1f - i / 5f)
                                val trailWidth = star.size * (1.2f - i / 5f)

                                drawLine(
                                    color = star.color.copy(alpha = trailAlpha),
                                    start = Offset(size.width / 2, size.height / 2),
                                    end = Offset(
                                        trailX - x + size.width / 2,
                                        trailY - y + size.height / 2
                                    ),
                                    strokeWidth = trailWidth
                                )
                            }
                        }
                    }
                    .size(star.size.dp)
                    .background(
                        color = star.color,
                        shape = CircleShape
                    )
            )
        }
    }
}

// Helper class to track shooting star properties and animations
class ShootingStar(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val size: Float,
    val color: Color,
    val durationMs: Int
) {
    var xAnim: Animatable<Float, AnimationVector1D>? = null
    var yAnim: Animatable<Float, AnimationVector1D>? = null
    var alphaAnim: Animatable<Float, AnimationVector1D>? = null
}

@Composable
fun EnhancedStatisticItem(
    label: String,
    value: String,
    color: Color,
    glowColor: Color,
    icon: ImageVector
) {
    // Animate icon bounce and rotation
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Subtle rotation for 3D effect
    var rotation = infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glow animation
    val glow = infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale.value)
                .graphicsLayer {
                    rotationZ = rotation.value
                    shadowElevation = glow.value
                    shape = CircleShape
                }
                .shadow(glow.value.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f),
                            color.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.5f),
                            glowColor.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsating glow behind the icon
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value with glow effect
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer {
                shadowElevation = glow.value / 3
            }
        )

        // Label with subtle animation
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.graphicsLayer {
                alpha = 0.9f
            }
        )
    }
}

@Composable
fun EnhancedStatisticsCard(
    playerProgress: PlayerProgress,
    primaryColor: Color,
    secondaryColor: Color
) {
    // Card glow animation
    val infiniteTransition = rememberInfiniteTransition()
    val elevation = infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Card border glow animation
    val borderGlow = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Glow effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(elevation.value.dp, RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.4f * borderGlow.value),
                            secondaryColor.copy(alpha = 0.3f * borderGlow.value),
                            Color.Transparent
                        ),
                        radius = 1.2f
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Card with glass effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    shadowElevation = elevation.value / 2
                    shape = RoundedCornerShape(20.dp)
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A237E).copy(alpha = 0.8f),
                            Color(0xFF4A148C).copy(alpha = 0.8f),
                            Color(0xFF311B92).copy(alpha = 0.8f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 300f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.7f * borderGlow.value),
                            secondaryColor.copy(alpha = 0.7f * borderGlow.value),
                            primaryColor.copy(alpha = 0.7f * borderGlow.value)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatisticItem(
                    label = "Level",
                    value = playerProgress.level.toString(),
                    color = Color(0xFF2196F3),
                    glowColor = primaryColor,
                    icon = Icons.Filled.EmojiEvents
                )

                EnhancedStatisticItem(
                    label = "XP",
                    value = "${playerProgress.xp}/${playerProgress.xpForNextLevel()}",
                    color = Color(0xFFFF9800),
                    glowColor = secondaryColor,
                    icon = Icons.Filled.Star
                )

                EnhancedStatisticItem(
                    label = "Tokens",
                    value = playerProgress.multiverseTokens.toString(),
                    color = Color(0xFF4CAF50),
                    glowColor = primaryColor,
                    icon = Icons.Filled.Toll
                )
            }
        }
    }
}
