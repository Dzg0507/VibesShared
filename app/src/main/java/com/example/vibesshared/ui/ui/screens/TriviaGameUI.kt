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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.zIndex
import com.example.vibesshared.ui.ui.components.PowerUpHintOverlay
import com.example.vibesshared.ui.ui.components.TriviaScene
import com.example.vibesshared.ui.ui.components.TriviaSceneRenderer
import com.example.vibesshared.ui.ui.components.rememberTriviaModelsManager
import com.example.vibesshared.ui.ui.data.AnswerFeedback
import com.example.vibesshared.ui.ui.data.PowerUp
import com.example.vibesshared.ui.ui.data.PowerUpType
import com.example.vibesshared.ui.ui.viewmodel.PowerUpUiState
import com.example.vibesshared.ui.ui.viewmodel.QuestUiState
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameUiState
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun TriviaGameUI(
    viewModel: TriviaGameViewModel,
    uiState: TriviaGameUiState,
    questState: QuestUiState,
    powerUpState: PowerUpUiState,
) {
    var showCorrectAnswer by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var sceneRenderer by remember { mutableStateOf<TriviaSceneRenderer?>(null) }
    var showScene by remember { mutableStateOf(true) }

    // Track last answer feedback to trigger animations
    var lastFeedback by remember { mutableStateOf<AnswerFeedback?>(null) }

    // Track when a power-up is activated to show 3D animation
    var lastActivatedPowerUp by remember { mutableStateOf<PowerUp?>(null) }
    val modelsManager = rememberTriviaModelsManager()

    // Get active power-ups
    val activePowerUps = remember(powerUpState.activePowerUps) {
        powerUpState.activePowerUps.mapNotNull { powerUpId ->
            powerUpState.availablePowerUps.find { it.id == powerUpId }
        }
    }

    // Create scroll state for main content
    val scrollState = rememberScrollState()

    // Effect to handle answer feedback animations
    LaunchedEffect(uiState.answerFeedback) {
        if ((uiState.answerFeedback != null) && (uiState.answerFeedback != lastFeedback)) {
            lastFeedback = uiState.answerFeedback
            when (uiState.answerFeedback) {
                AnswerFeedback.CORRECT -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    sceneRenderer?.displayCorrectAnswerAnimation()
                }
                AnswerFeedback.INCORRECT -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    sceneRenderer?.displayIncorrectAnswerAnimation()
                }
                else -> {}
            }
        }
    }

    // Effect to handle quest completion
    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver && questState.activeQuest != null && uiState.score >= questState.activeQuest.completionThreshold) {
            sceneRenderer?.createQuestCompletedAnimation()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Main box containing the game UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0026), Color(0xFF26004D))
                )
            )
    ) {
        // 3D Scene background
        if (showScene) {
            TriviaScene(
                modifier = Modifier.fillMaxSize(),
                modelsManager = modelsManager,  // Add this parameter
                onSceneReady = { renderer ->
                    sceneRenderer = renderer
                }
            )
        }

        // Game UI layers
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Fixed position elements at the top - these don't scroll
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quest Progress Section (only if there's an active quest)
                questState.activeQuest?.let { quest ->
                    QuestProgressBar(
                        quest = quest,
                        score = uiState.score,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Power-Up Bar - Compact layout
                PowerUpBar(
                    viewModel = viewModel,
                    powerUpState = powerUpState,
                    onPowerUpActivated = { powerUp ->
                        // When a power-up is activated, store it for animation
                        lastActivatedPowerUp = powerUp
                        // Create 3D power-up model
                        coroutineScope.launch {
                            sceneRenderer?.createPowerUpModel(
                                powerUpId = powerUp.id,
                                position = io.github.sceneview.math.Position(x = 0f, y = 1.5f, z = 0f)
                            )


                            // Provide haptic feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                // Active Power-Up Effects - Compact
                if (activePowerUps.isNotEmpty()) {
                    ActivePowerUpBadges(
                        powerUps = activePowerUps,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Countdown Timer - Compact
                CountdownTimer(
                    timeLeft = uiState.timeLeft,
                    isTimeFrozen = uiState.isTimeFrozen,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Scrollable content area - this takes the remaining space and scrolls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Question Display
                AnimatedVisibility(
                    visible = uiState.currentQuestion != null,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.currentQuestion?.let {
                        QuestionDisplay(
                            question = it.question,
                            questionNumber = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                            doublePointsActive = uiState.doublePointsActive,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Answer Buttons
                AnswerButtonsList(
                    uiState = uiState,
                    viewModel = viewModel,
                    showCorrectAnswer = showCorrectAnswer,
                    onCorrectAnswerShown = { showCorrectAnswer = false },
                    onButtonPressed = {
                        // Haptic feedback on button press
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )

                // Add padding at the bottom for better scrolling
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Hint overlay if active - covers the whole screen
        if (uiState.hintedAnswer != null) {
            PowerUpHintOverlay(
                correctAnswer = uiState.hintedAnswer,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Power-up activation animation
        lastActivatedPowerUp?.let { powerUp ->
            PowerUpActivationOverlay(powerUp = powerUp) {
                lastActivatedPowerUp = null
            }
        }
    }
}

@Composable
fun QuestProgressBar(
    quest: com.example.vibesshared.ui.ui.data.Quest,
    score: Int,
    modifier: Modifier = Modifier
) {
    val progressAnimation = remember(score) {
        Animatable(0f)
    }

    LaunchedEffect(score) {
        progressAnimation.animateTo(
            targetValue = (score.toFloat() / quest.completionThreshold).coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = quest.title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        // Custom progress indicator with glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnimation.value)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00FFFF),
                                Color(0xFF00CCFF)
                            )
                        )
                    )
                    .graphicsLayer {
                        if (score >= quest.completionThreshold) {
                            this.shadowElevation = 8f
                            this.spotShadowColor = Color(0xFF00FFFF).copy(alpha = 0.8f)
                        }
                    }
            )
        }

        Text(
            text = "${score}/${quest.completionThreshold}",
            style = MaterialTheme.typography.bodySmall,
            color = if (score >= quest.completionThreshold) Color.Green else Color.White.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun PowerUpActivationOverlay(
    powerUp: PowerUp,
    onAnimationComplete: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(powerUp) {
        launch {
            alpha.animateTo(
                targetValue = 0.9f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }

        launch {
            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }

        // Animation complete
        delay(1000)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            getPowerUpColor(powerUp.type),
                            getPowerUpColor(powerUp.type).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = getPowerUpIcon(powerUp.type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = powerUp.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ACTIVATED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ActivePowerUpBadges(
    powerUps: List<PowerUp>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        powerUps.forEach { powerUp ->
            ActivePowerUpBadge(powerUp = powerUp)
        }
    }
}

@Composable
fun CountdownTimer(
    timeLeft: Int,
    isTimeFrozen: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isTimeFrozen -> Color(0xFF29B6F6)  // Light Blue for frozen
        timeLeft < 5 -> Color.Red
        timeLeft < 15 -> Color(0xFFFF9800)
        else -> Color.White
    }

    val timerScale = remember { Animatable(1f) }

    LaunchedEffect(timeLeft) {
        if (timeLeft <= 5) {
            timerScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(100)
            )
            timerScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(100)
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .scale(timerScale.value)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isTimeFrozen) {
            Icon(
                imageVector = Icons.Filled.PauseCircle,
                contentDescription = "Time Frozen",
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = "$timeLeft",
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "seconds",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
fun QuestionDisplay(
    question: String,
    questionNumber: String,
    doublePointsActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shadowSize = infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .graphicsLayer {
                shadowElevation = shadowSize.value
                shape = RoundedCornerShape(16.dp)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A237E).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = questionNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if (doublePointsActive) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF5722),
                                        Color(0xFFFF9800)
                                    )
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExposurePlus2,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DOUBLE POINTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun AnswerButtonsList(
    uiState: TriviaGameUiState,
    viewModel: TriviaGameViewModel,
    showCorrectAnswer: Boolean,
    onCorrectAnswerShown: () -> Unit,
    onButtonPressed: () -> Unit
) {
    val currentQuestion = uiState.currentQuestion
    val isAnswered = uiState.answerFeedback != null

    // Animated content column
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        uiState.currentAnswers.forEach { answer ->
            val isCorrect = answer == currentQuestion?.correctAnswer
            val isUserAnswer = answer == uiState.selectedAnswer
            val isHinted = answer == uiState.hintedAnswer

            // Highlight correct answer when showingCorrectAnswer is true
            val showAsCorrect = isCorrect && (uiState.showingCorrectAnswer || showCorrectAnswer)

            AnswerButton(
                answer = answer,
                onClick = {
                    onButtonPressed()
                    if (!isAnswered) {
                        viewModel.checkAnswer(answer)
                    }
                },
                isEnabled = !isAnswered,
                isCorrectAnswer = showAsCorrect,
                showCorrectAnswer = showAsCorrect,
                isUserAnswer = isUserAnswer,
                isHinted = isHinted,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show answer feedback and continue button
        AnimatedVisibility(
            visible = isAnswered,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (uiState.answerFeedback == AnswerFeedback.CORRECT) "Correct!" else "Incorrect!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (uiState.answerFeedback == AnswerFeedback.CORRECT) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        onButtonPressed()
                        if (uiState.answerFeedback == AnswerFeedback.INCORRECT) {
                            // For incorrect answers, show the correct answer
                            viewModel.showCorrectAnswer()
                        } else {
                            // For correct answers, continue to next question
                            viewModel.showNextQuestion()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.answerFeedback == AnswerFeedback.CORRECT)
                            Color(0xFF4CAF50) else Color(0xFF2196F3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (uiState.answerFeedback == AnswerFeedback.CORRECT)
                            "Continue"
                        else
                            "See Correct Answer",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Continue button after showing correct answer
        AnimatedVisibility(
            visible = uiState.showingCorrectAnswer,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Button(
                onClick = {
                    onButtonPressed()
                    // Reset the showingCorrectAnswer flag
                    viewModel.showNextQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Continue to Next Question",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    LaunchedEffect(showCorrectAnswer) {
        if (showCorrectAnswer) {
            onCorrectAnswerShown()
        }
    }
}


// Enhanced highlighting for correct answer in the AnswerButton function

@Composable
fun AnswerButton(
    answer: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isCorrectAnswer: Boolean = false,
    showCorrectAnswer: Boolean = false,
    isUserAnswer: Boolean = false,
    isHinted: Boolean = false
) {
    val buttonHeight = 60.dp
    val buttonScale = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current

    // Animation for correct answer reveal
    val correctAnswerReveal = remember(showCorrectAnswer) {
        if (showCorrectAnswer && isCorrectAnswer) Animatable(0f) else Animatable(1f)
    }

    LaunchedEffect(showCorrectAnswer, isCorrectAnswer) {
        if (showCorrectAnswer && isCorrectAnswer) {
            correctAnswerReveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(isUserAnswer) {
        if (isUserAnswer) {
            buttonScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(100)
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(100)
            )
        }
    }

    val buttonColor = when {
        showCorrectAnswer && isCorrectAnswer ->
            Color(0xFF4CAF50) // Green for correct answer when revealed
        isHinted ->
            Color(0xFFFFD54F) // Amber for hinted answers
        isUserAnswer ->
            if (isCorrectAnswer) Color(0xFF4CAF50) else Color(0xFFE57373) // Green or red
        else ->
            Color(0xFF2196F3) // Default blue
    }

    val sparkleEffect = showCorrectAnswer && isCorrectAnswer

    Box(
        modifier = modifier
            .scale(buttonScale.value)
    ) {
        // Enhanced glow effect for correct answer with a stronger animation
        if (sparkleEffect) {
            val infiniteTransition = rememberInfiniteTransition()
            val glowAlpha = infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.9f,  // Increased maximum alpha for more visibility
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val glowScale = infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.1f,  // Add a pulsing scale effect
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            // Stronger glow with multiple layers
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(glowScale.value)
                    .alpha(glowAlpha.value * correctAnswerReveal.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),  // Green center
                                Color(0xFF4CAF50).copy(alpha = 0.6f),
                                Color(0xFF4CAF50).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            // Add checkmark icon overlay
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Correct Answer",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                        .alpha(glowAlpha.value)
                )
            }

            // On first reveal, also provide haptic feedback
            // Remove both the LaunchedEffect and the condition
            @Suppress("KotlinConstantConditions")
            if (sparkleEffect) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                }
        }
        // Main button
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor.copy(alpha = if (isEnabled) 1f else 0.6f)
            ),
            enabled = isEnabled,
            border = when {
                isHinted -> BorderStroke(2.dp, Color(0xFFFFD54F))
                showCorrectAnswer && isCorrectAnswer -> BorderStroke(2.dp, Color.White)
                else -> null
            }
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (showCorrectAnswer && isCorrectAnswer) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PowerUpBar(
    viewModel: TriviaGameViewModel,
    powerUpState: PowerUpUiState,
    onPowerUpActivated: (PowerUp) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A).copy(alpha = 0.8f),
                        Color(0xFF000000).copy(alpha = 0.4f)
                    )
                )
            )
            .padding(12.dp)
    ) {
        // Available power-ups
        if (powerUpState.playerPowerUps.powerUpInventory.powerUps.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                powerUpState.availablePowerUps.forEach { powerUp ->
                    val count = powerUpState.playerPowerUps.getPowerUpCount(powerUp.id)
                    val isOnCooldown = powerUpState.powerUpCooldowns.containsKey(powerUp.id)
                    val isActive = powerUpState.activePowerUps.contains(powerUp.id)

                    if (count > 0) {
                        PowerUpButton(
                            powerUp = powerUp,
                            count = count,
                            isOnCooldown = isOnCooldown,
                            isActive = isActive,
                            onClick = {
                                viewModel.usePowerUp(powerUp.id)
                                onPowerUpActivated(powerUp)
                            }
                        )
                    }
                }
            }
        } else {
            // No power-ups
            Text(
                text = "Earn power-ups by completing quests!",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PowerUpButton(
    powerUp: PowerUp,
    count: Int,
    isOnCooldown: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scaleValue = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    } else {
        1f
    }

    val rotationValue = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    } else {
        0f
    }

    val alpha = if (isOnCooldown) 0.5f else 1f
    val powerUpColor = getPowerUpColor(powerUp.type)

    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(4.dp) // Added padding to make room for the badge
    ) {
        // Main power-up button
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(scaleValue)
                .rotate(rotationValue)
                .alpha(alpha)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            powerUpColor,
                            powerUpColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            powerUpColor
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(enabled = !isOnCooldown && count > 0, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getPowerUpIcon(powerUp.type),
                contentDescription = powerUp.name,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            // Cooldown overlay
            if (isOnCooldown) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = "On Cooldown",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Count badge - positioned outside the circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp) // Adjusted offset to position outside
                .clip(CircleShape)
                .background(Color(0xFF3F0071))
                .border(1.dp, Color.White, CircleShape)
                .zIndex(2f), // Ensure badge is on top
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivePowerUpBadge(
    powerUp: PowerUp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val borderWidth = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .alpha(alpha.value)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = getPowerUpColor(powerUp.type).copy(alpha = 0.3f)
            )
            .border(
                width = borderWidth.value.dp,
                color = getPowerUpColor(powerUp.type).copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = getPowerUpIcon(powerUp.type),
                contentDescription = powerUp.name,
                tint = getPowerUpColor(powerUp.type),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = powerUp.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper functions for power-up colors and icons
private fun getPowerUpColor(type: PowerUpType): Color {
    return when (type) {
        PowerUpType.TIME_FREEZE -> Color(0xFF29B6F6)    // Light Blue
        PowerUpType.TIME_BOOST -> Color(0xFF00E676)     // Green
        PowerUpType.FIFTY_FIFTY -> Color(0xFFFFEB3B)    // Yellow
        PowerUpType.CORRECT_ANSWER -> Color(0xFFFF4081) // Pink
        PowerUpType.HINT -> Color(0xFFFFD54F)          // Amber
        PowerUpType.SKIP_QUESTION -> Color(0xFF7C4DFF)  // Deep Purple
        PowerUpType.DOUBLE_POINTS -> Color(0xFFFF5722)  // Deep Orange
    }
}

private fun getPowerUpIcon(type: PowerUpType): ImageVector {
    return when (type) {
        PowerUpType.TIME_FREEZE -> Icons.Filled.PauseCircle
        PowerUpType.TIME_BOOST -> Icons.Filled.AddAlarm
        PowerUpType.FIFTY_FIFTY -> Icons.Filled.Filter2
        PowerUpType.CORRECT_ANSWER -> Icons.Filled.CheckCircle
        PowerUpType.HINT -> Icons.Filled.Lightbulb
        PowerUpType.SKIP_QUESTION -> Icons.Filled.SkipNext
        PowerUpType.DOUBLE_POINTS -> Icons.Filled.ExposurePlus2
    }
}