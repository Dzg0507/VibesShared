package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibesshared.ui.ui.components.TriviaScene
import com.example.vibesshared.ui.ui.components.TriviaSceneRenderer
import com.example.vibesshared.ui.ui.components.rememberTriviaModelsManager
import com.example.vibesshared.ui.ui.data.PowerUp
import com.example.vibesshared.ui.ui.data.PowerUpType
import com.example.vibesshared.ui.ui.viewmodel.PowerUpUiState
import com.example.vibesshared.ui.ui.viewmodel.QuestUiState
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Fix for the PowerUp Shop screen scrolling issues in PowerUpShopScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerUpShopScreen(
    viewModel: TriviaGameViewModel,
    powerUpState: PowerUpUiState,
    questState: QuestUiState,
    onBackToMenu: () -> Unit
) {
    var showPurchaseConfirmation by remember { mutableStateOf<String?>(null) }
    var showPurchaseSuccess by remember { mutableStateOf<PowerUp?>(null) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var sceneRenderer by remember { mutableStateOf<TriviaSceneRenderer?>(null) }
    val modelsManager = rememberTriviaModelsManager()

    // Add scroll state for the main content
    val scrollState = rememberScrollState()

    // Filter for type of power-ups
    var selectedType by remember { mutableStateOf<PowerUpType?>(null) }

    // Filter available power-ups
    val filteredPowerUps = if (selectedType != null) {
        powerUpState.availablePowerUps.filter { it.type == selectedType }
    } else {
        powerUpState.availablePowerUps
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF000C24), Color(0xFF26004D))
                )
            )
    ) {
        // 3D background
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
            particleCount = 30,
            modifier = Modifier.fillMaxSize()
        )

        // IMPORTANT: Use a direct Column instead of a Scaffold to avoid duplicate headers
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)  // Add some top padding to compensate for removed AppBar
        ) {
            // Custom back button in the header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Back button
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackToMenu()
                    }
                ) {444
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Spacing
                Spacer(modifier = Modifier.weight(1f))
            }

            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)  // Make the entire column scrollable
            ) {
                // Token display with 3D effect
                TokenBalanceCard3D(questState.playerProgress.multiverseTokens)

                Spacer(modifier = Modifier.height(16.dp))

                // Filter chips for power-up types
                PowerUpTypeFilters(
                    selectedType = selectedType,
                    onTypeSelected = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedType = if (selectedType == it) null else it
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Available power-ups
                Text(
                    text = "Available Power-Ups",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredPowerUps.isEmpty()) {
                    EmptyPowerUpsList()
                } else {
                    // Replace LazyVerticalGrid with a normal grid layout inside the scroll
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Create rows of power-ups, 2 per row
                        filteredPowerUps.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { powerUp ->
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        PowerUpShopItem3D(
                                            powerUp = powerUp,
                                            count = powerUpState.playerPowerUps.getPowerUpCount(powerUp.id),
                                            playerTokens = questState.playerProgress.multiverseTokens,
                                            onPurchase = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showPurchaseConfirmation = powerUp.id
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

        // Purchase confirmation dialog
        if (showPurchaseConfirmation != null) {
            val powerUp = powerUpState.availablePowerUps.find { it.id == showPurchaseConfirmation }
            powerUp?.let {
                PurchaseConfirmationDialog(
                    powerUp = it,
                    currentTokens = questState.playerProgress.multiverseTokens,
                    onConfirm = {
                        viewModel.purchasePowerUp(powerUp.id)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showPurchaseConfirmation = null

                        // Show success animation
                        showPurchaseSuccess = powerUp

                        // Create 3D power-up model
                        coroutineScope.launch {
                            sceneRenderer?.createPowerUpModel(
                                powerUpId = powerUp.id,
                                position = io.github.sceneview.math.Position(x = 0f, y = 1.5f, z = 0f)
                            )
                        }
                    },
                    onDismiss = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showPurchaseConfirmation = null
                    }
                )
            }
        }

        // Purchase success animation
        AnimatedVisibility(
            visible = showPurchaseSuccess != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            showPurchaseSuccess?.let { powerUp ->
                PurchaseSuccessOverlay(
                    powerUp = powerUp,
                    onDismiss = { showPurchaseSuccess = null }
                )
            }
        }
    }
}

@Composable
fun PowerUpTypeFilters(
    selectedType: PowerUpType?,
    onTypeSelected: (PowerUpType) -> Unit
) {
    val types = remember { PowerUpType.values() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { type ->
            val isSelected = selectedType == type
            val color = getPowerUpColor(type)

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                enabled = true,
                label = {
                    Text(
                        text = type.name.replace('_', ' ').split(' ').joinToString(" ") {
                            it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = getPowerUpIcon(type),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = color.copy(alpha = 0.3f),
                    labelColor = Color.White,
                    selectedLabelColor = Color.White,
                    iconColor = color,
                    selectedLeadingIconColor = color
                ),
                border = if (isSelected) {
                    BorderStroke(
                        width = 1.dp,
                        color = color
                    )
                } else {
                    BorderStroke(
                        width = 1.dp,
                        color = color.copy(alpha = 0.5f)
                    )
                }
            )
        }
    }
}

@Composable
fun TokenBalanceCard3D(tokens: Int) {
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
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                shadowElevation = elevation.value
                rotationZ = rotate.value
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3F0071)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TokenIcon(size = 48.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Multiverse Tokens",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Use tokens to purchase power-ups",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E676),
                                Color(0xFF00C853)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "$tokens",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TokenIcon(size: Dp) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotate = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        )
    )

    val innerRotate = infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .rotate(rotate.value)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676),
                        Color(0xFF00E676).copy(alpha = 0.6f),
                        Color(0xFF00E676).copy(alpha = 0.2f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .rotate(innerRotate.value)
                .background(
                    color = Color(0xFF3F0071),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Token,
                contentDescription = "Tokens",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}

@Composable
fun EmptyPowerUpsList() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Power-Ups Found",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Try changing the filter or complete more quests to unlock different power-ups",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PowerUpShopItem3D(
    powerUp: PowerUp,
    count: Int,
    playerTokens: Int,
    onPurchase: () -> Unit
) {
    val canAfford = playerTokens >= powerUp.tokenCost
    val ownedCount = count

    // Animations
    val infiniteTransition = rememberInfiniteTransition()
    val elevation = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Define animation for affordable items
    val scaleAnimation = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val powerUpColor = getPowerUpColor(powerUp.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (canAfford) scaleAnimation.value else 1f)
            .graphicsLayer {
                shadowElevation = if (canAfford) elevation.value else 0f
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26004D).copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = powerUpColor.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Power-up icon with glow
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                powerUpColor,
                                powerUpColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPowerUpIcon(powerUp.type),
                    contentDescription = powerUp.name,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                // Only show counter if user has any
                if (ownedCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(
                                color = Color(0xFF3F0071),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$ownedCount",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = powerUp.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = powerUp.description,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier.height(60.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration info if applicable
            if (powerUp.durationSeconds > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Duration: ${powerUp.durationSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Cooldown info if applicable
            if (powerUp.cooldownSeconds > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Cooldown: ${powerUp.cooldownSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Cost display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = if (canAfford) Color(0xFF00E676).copy(alpha = 0.2f)
                        else Color.Gray.copy(alpha = 0.2f)
                    )
                    .clickable(
                        enabled = canAfford,
                        onClick = onPurchase
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = if (canAfford) Color(0xFF00E676) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${powerUp.tokenCost}",
                        color = if (canAfford) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseConfirmationDialog(
    powerUp: PowerUp,
    currentTokens: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val remainingTokens = currentTokens - powerUp.tokenCost
    val powerUpColor = getPowerUpColor(powerUp.type)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF26004D),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = {
            Icon(
                imageVector = getPowerUpIcon(powerUp.type),
                contentDescription = null,
                tint = powerUpColor,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Confirm Purchase",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to purchase ${powerUp.name}?",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                TokenBalanceRow(
                    label = "Current balance:",
                    value = currentTokens
                )

                TokenBalanceRow(
                    label = "Cost:",
                    value = -powerUp.tokenCost,
                    isNegative = true
                )

                @Suppress("DEPRECATION")
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )

                TokenBalanceRow(
                    label = "Remaining balance:",
                    value = remainingTokens,
                    isRemainder = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TokenBalanceRow(
    label: String,
    value: Int,
    isNegative: Boolean = false,
    isRemainder: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Token,
                contentDescription = null,
                tint = when {
                    isNegative -> Color.Red
                    isRemainder -> Color(0xFF00E676)
                    else -> Color(0xFF00E676)
                },
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (isNegative) "$value" else value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    isNegative -> Color.Red
                    isRemainder -> Color(0xFF00E676)
                    else -> Color.White
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PurchaseSuccessOverlay(
    powerUp: PowerUp,
    onDismiss: () -> Unit
) {
    val powerUpColor = getPowerUpColor(powerUp.type)
    val textAlpha = remember { Animatable(0f) }

    // Auto-dismiss after animation completes
    LaunchedEffect(powerUp) {
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, delayMillis = 500)
        )

        delay(2000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated power-up icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                powerUpColor,
                                powerUpColor.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPowerUpIcon(powerUp.type),
                    contentDescription = powerUp.name,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = powerUp.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Text(
                text = "Purchased Successfully!",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF00E676),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

// Helper functions
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