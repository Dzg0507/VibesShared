package com.example.vibesshared.ui.ui.cardgame

// --- Imports (Keep ALL original imports from your V4/V8 file) ---
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Add any other imports that were in your original BattleScreen.kt
// --- End Imports ---

// --- BattleScreen Composable (Root Structure from V8 - Modified Opponent Layout) ---
@Composable
fun BattleScreen(viewModel: BattleViewModel) {
    // --- State collection and handlers (Exactly as in V4/V8) ---
    val gamePhase by viewModel.gamePhase.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val showGameOverDialog by viewModel.showGameOverDialog.collectAsState()
    val gameOverData by viewModel.gameOverData.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val currentAnimation by viewModel.currentAnimation.collectAsState()
    val awaitingPlayerAction by viewModel.awaitingPlayerAction.collectAsState()
    val battleState by viewModel.battleState.collectAsState()
    val playerHand by viewModel.playerHand.collectAsState()
    val opponentHand by viewModel.opponentHand.collectAsState()
    val activePlayerCard by viewModel.activePlayerCard.collectAsState()
    val activeOpponentCard by viewModel.activeOpponentCard.collectAsState()
    val isPlayerTurnState by viewModel.isPlayerTurn.collectAsState()
    val turn by viewModel.turn.collectAsState()
    val battleLog by viewModel.battleLog.collectAsState()
    val currentPlayerEnergy by viewModel.currentPlayerEnergy.collectAsState()
    val maxPlayerEnergy by viewModel.maxPlayerEnergy.collectAsState()
    var showHandDialog by remember { mutableStateOf(false) }
    var showBattleLog by remember { mutableStateOf(false) }
    var isTargetSelectionActive by remember { mutableStateOf(false) }
    var pendingAbilityIndex by remember { mutableIntStateOf(-1) }

    // Helper lambdas (Exactly as in V4/V8)
    val canAffordAbility = { abilityIndex: Int ->
        val ability = activePlayerCard?.abilities?.getOrNull(abilityIndex)
        val cost = ability?.ability?.energyCost ?: 0
        cost <= currentPlayerEnergy
    }

    val handleUseAbility = { abilityIndex: Int ->
        val ability = activePlayerCard?.abilities?.getOrNull(abilityIndex)
        if (ability != null) { when (ability.ability.targetType) {
            TargetType.SELF, TargetType.ALL_ALLIES, TargetType.RANDOM_ALLY -> { viewModel.onAbilityUsed(abilityIndex, null) }
            TargetType.SINGLE, TargetType.RANDOM_ENEMY, TargetType.ALL_ENEMIES -> {
                if (ability.ability.targetType == TargetType.SINGLE) { if (activeOpponentCard != null) { isTargetSelectionActive = true; pendingAbilityIndex = abilityIndex } else { viewModel.postStatusMessage("No valid target for ability.") } }
                else { viewModel.onAbilityUsed(abilityIndex, null) } // Non-single target handled by VM/Repo
            }
        }
        }
    }

    // State derived booleans (Exactly as in V4/V8)
    val canPlayerAct = isPlayerTurnState && gamePhase == BattlePhase.PLAYER_ACTION && !isAnimating && awaitingPlayerAction
    val isTurnOwner = isPlayerTurnState && !isAnimating
    // --- End State collection and handlers ---

    // Root Box for background and overlays
    Box(modifier = Modifier.fillMaxSize()) {
        ThemedBackground( modifier = Modifier.fillMaxSize() )

        // Main Column for UI layers
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar (Use V8 implementation)
            TopBar(
                turn = turn,
                isPlayerTurn = isPlayerTurnState,
                currentEnergy = currentPlayerEnergy,
                maxEnergy = maxPlayerEnergy,
                onLogClick = { showBattleLog = true }
            )

            // --- Battle Area using Box for Alignment (Reduced Padding) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Reduced vertical padding
            ) {

                // --- Opponent Display (Info Left, Card Right) - Top Right Area ---
                // --- MODIFIED OpponentCardDisplayV7 to reflect new layout ---
                OpponentCardDisplayModified( // <<<<< RENAMED FOR CLARITY
                    cardInPlay = activeOpponentCard,
                    opponentHand = opponentHand, // Pass hand for inline display
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Align to Top Center for full width usage
                        .fillMaxWidth() // Take full width
                        .padding(top = 8.dp) // Add top padding
                )

                // Player Display (Card Left, Info Right) - Bottom Left Area (Keep V8 layout)
                PlayerCardDisplayV8( // <<<<< RENAMED FOR CLARITY (Assuming this matches your V8)
                    cardInPlay = activePlayerCard,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 0.dp, bottom = 0.dp) // Keep V8 padding
                )
                // --- End Display Components ---

                // Status message overlay (Center) - Use V8 implementation
                StatusMessage(
                    message = statusMessage,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Animation overlays (Fill and on top) - Use V8 logic
                if (isAnimating && currentAnimation != null) {
                    BattleAnimation(
                        animationType = currentAnimation!!,
                        onComplete = viewModel::onAnimationComplete,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            } // --- End Battle Area Box ---

            // Bottom bar (Use V8 implementation with min height)
            BottomBar(
                playerHand = playerHand,
                isPlayerTurn = canPlayerAct,
                isTurnOwner = isTurnOwner,
                canAttack = activePlayerCard != null && activeOpponentCard != null,
                onAttack = viewModel::onAttackAction,
                onEndTurn = viewModel::onEndTurnAction,
                onCardSelected = { cardId ->
                    if (canPlayerAct) {
                        val ctp = playerHand.find { it.id == cardId }
                        if (ctp != null && currentPlayerEnergy >= ctp.energy) {
                            viewModel.onCardPlayed(cardId)
                        } else if (ctp != null) {
                            viewModel.postStatusMessage("Not enough energy!")
                        }
                    }
                },
                onShowFullHand = { showHandDialog = true },
                activeCard = activePlayerCard,
                canAffordAbility = canAffordAbility,
                onUseAbility = handleUseAbility
            )
        } // End Main Column

        // --- Dialogs remain overlaid on top (Use V8 logic/structure) ---
        if (isTargetSelectionActive) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                val pt = listOfNotNull(activeOpponentCard)
                TargetSelector(
                    targetType = activePlayerCard?.abilities?.getOrNull(pendingAbilityIndex)?.ability?.targetType ?: TargetType.SINGLE,
                    availableTargets = pt,
                    onTargetSelected = { tid ->
                        viewModel.onAbilityUsed(pendingAbilityIndex, tid)
                        isTargetSelectionActive = false
                        pendingAbilityIndex = -1
                    },
                    onCancel = {
                        isTargetSelectionActive = false
                        pendingAbilityIndex = -1
                    }
                )
            }
        }
        if (showHandDialog) {
            BattleHandDialog(
                cards = playerHand,
                onCardSelected = { cardId ->
                    val ctp = playerHand.find { it.id == cardId }
                    if (canPlayerAct) {
                        if (ctp != null && currentPlayerEnergy >= ctp.energy) {
                            viewModel.onCardPlayed(cardId)
                            showHandDialog = false
                        } else if (ctp != null) {
                            viewModel.postStatusMessage("Not enough energy!")
                        } else {
                            showHandDialog = false
                        }
                    }
                },
                onDismiss = { showHandDialog = false }
            )
        }
        if (showBattleLog) {
            BattleLogDialog(
                battleLog = battleLog,
                onDismiss = { showBattleLog = false }
            )
        }
        if (showGameOverDialog && gameOverData != null) {
            BattleGameOverDialog(
                result = gameOverData!!.first,
                summary = gameOverData!!.second,
                onConfirm = viewModel::onGameOverDialogDismiss
            )
        }
        // --- End Dialogs ---

    } // End Root Box

    // Start battle on first composition (Exactly as in V8)
    LaunchedEffect(Unit) { viewModel.startNewBattle() }
}

// --- Card Size Constants (Keep from V8 or adjust as needed) ---
private val cardWidthV8 = 95.dp
private val cardHeightV8 = 135.dp
private val healthBarWidthV8 = 130.dp // Bar width constraint

// --- MODIFIED Opponent Layout Components ---

/**
 * Displays the opponent's card and info according to the new layout.
 * Top Row: Health Bar (Left) | Card Component (Right)
 * Second Row: Name (Left) | Level | Hand Cards (Right)
 */
@Composable
private fun OpponentCardDisplayModified(
    cardInPlay: CardInPlay?,
    opponentHand: List<MultiverseCard>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        if (cardInPlay != null) {
            // --- Top Row: Full width Health Bar with Card Component on right ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                // Health Bar (takes full width)
                FloatingHealthBar(
                    currentHealth = cardInPlay.currentHealth,
                    maxHealth = cardInPlay.card.health,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(end = cardWidthV8 + 8.dp) // Reserve space for card
                )

                // Card Component (positioned at the right edge)
                Box(
                    Modifier
                        .width(cardWidthV8)
                        .height(cardHeightV8)
                        .align(Alignment.TopEnd)
                ) {
                    CardComponent(
                        card = cardInPlay.card,
                        displayMode = CardDisplayMode.BOARD,
                        cardInPlay = cardInPlay,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- Second Row: Name (Left) | Level | Hand Cards (Right) ---
            Row(modifier = Modifier
                .align(Alignment.Start)
                .fillMaxWidth()
                .padding(top = 4.dp),

            ) {
                // Name (left)

                Text(
                    text = cardInPlay.card.name.uppercase(),

                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Level (just left of the hand cards)
                Text(
                    text = "Lv ${cardInPlay.card.level}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Hand Cards (right aligned, same width as health bar)
                OpponentHandRowModified(
                    opponentHand = opponentHand,
                    modifier = Modifier.width(cardWidthV8)
                )
            }
        } else {
            // Placeholder if no card - maintain structure
            Spacer(Modifier.height(cardHeightV8 + 16.dp))
        }
    }
}

/**
 * Displays opponent's hand cards (face down) in a Row.
 */
@Composable
private fun OpponentHandRowModified(
    opponentHand: List<MultiverseCard>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(36.dp),
        horizontalArrangement = Arrangement.End, // Right-align the cards
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display count if more than 5 cards
        if (opponentHand.size > 5) {
            Text(
                text = "+${opponentHand.size - 5}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Show cards (up to 5) with overlap
        Row(
            horizontalArrangement = Arrangement.spacedBy((-18).dp) // Overlap cards
        ) {
            opponentHand.take(5).forEach { _ ->
                CardBackView(
                    modifier = Modifier
                        .width(28.dp)
                        .height(36.dp)
                )
            }
        }
    }
}





@Composable
private fun PlayerInfoV8(cardInPlay: CardInPlay, modifier: Modifier = Modifier) {
    // Keep the V8 implementation for player info layout
    // (Name Left | Level Right) above (Health Bar + Text centered)
    Column(
        modifier = modifier.fillMaxWidth() // Use the constraint from V8
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cardInPlay.card.name.uppercase(),
                color = Color.White,
                fontSize = 12.sp, // Adjusted size
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false) // Takes space needed
            )
            // Spacer pushes Level text to the end if name is short
            // Spacer(Modifier.weight(1f)) // Optional: Uncomment if Level should always be far right
            Text(
                text = "Lv ${cardInPlay.card.level}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(12.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingHealthBar(
                currentHealth = cardInPlay.currentHealth,
                maxHealth = cardInPlay.card.health,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "${cardInPlay.currentHealth}/${cardInPlay.card.health}",
                color = Color.Black.copy(alpha = 0.9f),
                fontSize = 8.sp,
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PlayerCardDisplayV8(cardInPlay: CardInPlay?, modifier: Modifier = Modifier) {
    // Keep the V8 Row layout: Card | Spacer | Info
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom // Align card/info to bottom
    ) {
        if (cardInPlay != null) {
            Box(Modifier.width(cardWidthV8).height(cardHeightV8)) { // Card Left (Smaller)
                CardComponent(
                    card = cardInPlay.card,
                    displayMode = CardDisplayMode.BOARD,
                    cardInPlay = cardInPlay,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(8.dp)) // Space
            PlayerInfoV8(cardInPlay = cardInPlay) // Info Right
        } else {
            // Placeholder maintaining structure
            Row(verticalAlignment = Alignment.Bottom) {
                EmptyCardSlot(text = "", modifier = Modifier.width(cardWidthV8).height(cardHeightV8))
                Spacer(Modifier.width(8.dp))
                Spacer(Modifier.width(healthBarWidthV8).height(60.dp)) // Adjusted placeholder height
            }
        }
    }
}


// --- Helper Composables (Keep EXACTLY as defined in V8) ---
// Includes TopBar, EmptyCardSlot, StatusMessage, BottomBar, Dialogs, etc.

@Composable
private fun TopBar(
    turn: Int,
    isPlayerTurn: Boolean,
    currentEnergy: Int,
    maxEnergy: Int,
    onLogClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    (if (isPlayerTurn) Color(0xFF4CAF50) else Color(0xFFE53935))
                        .copy(alpha = 0.8f)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Turn $turn",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlayerTurn) "Your Turn" else "Opponent's Turn",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        EnergyDisplay( // Assumed defined/imported
            currentEnergy = currentEnergy,
            maxEnergy = maxEnergy
        )
        IconButton(
            onClick = onLogClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A).copy(alpha = 0.8f))
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Battle Log",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EmptyCardSlot(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A).copy(alpha = 0.6f))
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center // Center placeholder text
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun BottomBar( // Keep V8 implementation with min height
    playerHand: List<MultiverseCard>,
    isPlayerTurn: Boolean,
    isTurnOwner: Boolean,
    canAttack: Boolean,
    onAttack: () -> Unit,
    onEndTurn: () -> Unit,
    onCardSelected: (String) -> Unit,
    onShowFullHand: () -> Unit,
    activeCard: CardInPlay?,
    canAffordAbility: (Int) -> Boolean,
    onUseAbility: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 176.dp) // Keep min height
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color(0xFF0D0221).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Action row height
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                text = "Attack",
                enabled = isPlayerTurn && canAttack && activeCard != null && !activeCard.isStunned(),
                onClick = onAttack,
                color = Color(0xFFE53935),
                modifier = Modifier.padding(horizontal = 4.dp) // Adjust padding slightly if needed
            )

            // Ability Buttons Row (or other content)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Spacing between abilities
            ) {
                activeCard?.abilities?.take(2)?.forEachIndexed { index, ability ->
                    AbilityButton(
                        ability = ability,
                        modifier = Modifier.height(50.dp).width(100.dp), // Consistent height/width
                        enabled = isPlayerTurn &&
                                !activeCard.isStunned() &&
                                ability.isReady &&
                                canAffordAbility(index),
                        onClick = { onUseAbility(index) }
                    )
                }
                // Fill remaining space if fewer than 2 abilities
                repeat(2 - (activeCard?.abilities?.size ?: 0)) {
                    Spacer(Modifier.width(100.dp + 4.dp)) // Placeholder for button + spacing
                }
            }


            ActionButton(
                text = "End Turn",
                modifier = Modifier.padding(horizontal = 4.dp), // Adjust padding
                enabled = isTurnOwner,
                onClick = onEndTurn,
                color = Color(0xFF4CAF50)
            )
        }

        // Hand Display Box (Keep V8 structure)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Hand preview height
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(vertical = 8.dp)
        ) {
            if (playerHand.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your hand is empty", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(playerHand, key = { it.id }) { card ->
                        CardComponent(
                            card = card,
                            displayMode = CardDisplayMode.HAND,
                            onClick = { onCardSelected(card.id) },
                            modifier = Modifier
                                .fillParentMaxHeight(0.95f)
                                .aspectRatio(0.7f)
                        )
                    }
                }
                // Show Full Hand Button (Keep V8 structure)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A).copy(alpha = 0.8f))
                        .clickable { onShowFullHand() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, "Show Full Hand", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


@Composable
private fun StatusMessage(
    message: String?,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            currentMessage = message
            visible = true
            delay(2500)
            if (currentMessage == message) { // Only hide if message hasn't changed
                visible = false
            }
        } else {
            visible = false // Hide immediately if message becomes null/blank
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier
        // Enter/Exit transitions can be added here if desired
    ) {
        currentMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- Dialog Definitions (Keep EXACTLY as defined in V8) ---
@Composable
private fun BattleLogDialog(
    battleLog: List<BattleEvent>,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(battleLog.size) {
        if (battleLog.isNotEmpty()) {
            launch {
                delay(50) // Allow list to update
                listState.animateScrollToItem(battleLog.size - 1)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // Max height for dialog
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)) // Dark background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Battle Log", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(battleLog) { event ->
                        Text(
                            "[T${event.turn}] ${event.description}",
                            color = when (event.type.uppercase()) {
                                "ERROR", "STUNNED", "DRAW_FAILED", "COOLDOWN" -> Color.Red.copy(alpha = 0.9f)
                                "SYSTEM", "NEW_TURN", "END_TURN" -> Color.Gray
                                "VICTORY", "DEFEAT", "DRAW" -> Color.Yellow
                                "ATTACK", "ABILITY_DAMAGE", "COUNTER_ATTACK" -> Color(0xFFFFA726) // Orange
                                "ABILITY_HEAL", "EFFECT_APPLIED", "STAT_MOD" -> Color(0xFF66BB6A) // Green
                                else -> Color.White.copy(alpha = 0.9f)
                            },
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleHandDialog(
    cards: List<MultiverseCard>,
    onCardSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // Max height
                .padding(horizontal = 16.dp, vertical = 32.dp), // Padding around dialog
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)) // Dark background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Hand (${cards.size})", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.2f))

                if (cards.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Your hand is empty", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(140.dp), // Adaptive columns based on size
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cards) { card ->
                            BattleHandCardItem( // Re-use V8 item
                                card = card,
                                isPlayable = true, // Needs real logic based on energy etc.
                                onClick = { onCardSelected(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleHandCardItem( // Keep V8 version
    card: MultiverseCard,
    isPlayable: Boolean, // Add this parameter
    onClick: () -> Unit
) {
    val alpha = if (isPlayable) 1f else 0.6f // Dim if not playable
    Column(
        modifier = Modifier
            .width(140.dp) // Fixed width for consistency in grid
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A)) // Dark background for item
            .clickable(enabled = isPlayable, onClick = onClick) // Only clickable if playable
            .padding(8.dp)
            .graphicsLayer(alpha = alpha), // Apply alpha based on playability
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardComponent( // Use CardComponent for display
            card = card,
            displayMode = CardDisplayMode.HAND, // Use HAND mode
            modifier = Modifier.height(140.dp).fillMaxWidth() // Fixed height
        )
        Spacer(Modifier.height(8.dp))
        Text(card.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, "Energy", tint = Color(0xFF00FFFF), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("${card.energy}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BattleGameOverDialog( // Keep V8 version
    result: BattleResult,
    summary: String,
    onConfirm: () -> Unit
) {
    val isVictory = result == BattleResult.PLAYER_VICTORY
    val isDraw = result == BattleResult.DRAW
    val backgroundColor = when {
        isVictory -> Color(0xFF1B5E20) // Dark Green
        isDraw -> Color(0xFF424242)    // Grey
        else -> Color(0xFFB71C1C)       // Dark Red
    }
    val title = when {
        isVictory -> "Victory!"
        isDraw -> "Draw"
        else -> "Defeat"
    }
    val icon = when {
        isVictory -> Icons.Default.EmojiEvents
        isDraw -> Icons.Default.Balance
        else -> Icons.Default.SentimentVeryDissatisfied
    }

    Dialog(onDismissRequest = {}) { // Prevent dismissal by clicking outside
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, title, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(summary, color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}