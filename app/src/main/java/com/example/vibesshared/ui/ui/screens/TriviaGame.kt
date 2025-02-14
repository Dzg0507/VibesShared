@file:OptIn(ExperimentalAnimationApi::class)

package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.text.Html
import androidx.annotation.RawRes
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.data.AnswerFeedback
import com.example.vibesshared.ui.ui.data.Question
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.delay

// --- UTILITY FUNCTION ---
@SuppressLint("ObsoleteSdkInt")
fun decodeHtmlEntities(input: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(input, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(input).toString()
    }
}

// --- DATA CLASS ---
data class TriviaGameUiState(
    val timeLeft: Int = 60,
    val currentQuestion: Question? = null,
    val currentAnswers: List<String> = emptyList(),
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answerFeedback: AnswerFeedback = AnswerFeedback.NONE,
    val showSettings: Boolean = true,
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val selectedAnswer: String? = null,
    val leaderboard: List<Int> = emptyList()
)

// --- MAIN COMPOSABLE ---
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriviaGameScreen(viewModel: TriviaGameViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val correctSound = rememberMediaPlayer(context, R.raw.correct_sound)
    val incorrectSound = rememberMediaPlayer(context, R.raw.incorrect_answer)

    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.answerFeedback) {
        when (uiState.answerFeedback) {
            AnswerFeedback.CORRECT -> {
                showConfetti = true
                correctSound?.start()
                delay(2000)
                showConfetti = false
                viewModel.showNextQuestion()

            }
            AnswerFeedback.INCORRECT -> {
                incorrectSound?.start()
                delay(2000)
                viewModel.showNextQuestion()

            }
            AnswerFeedback.NONE -> { /* Do nothing */ }
        }
    }

    if (uiState.showSettings) {
        SettingsScreen { difficulty, category, questionCount ->
            viewModel.startGame(difficulty, category, questionCount)
        }
    } else {
        GameContent(uiState = uiState, viewModel = viewModel, context = context, showConfetti = showConfetti)
    }
}

// --- rememberMediaPlayer FUNCTION ---
@Composable
fun rememberMediaPlayer(context: Context, @RawRes soundRes: Int): MediaPlayer? {
    val mediaPlayer = remember { MediaPlayer.create(context, soundRes) }
    DisposableEffect(mediaPlayer) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }
    return mediaPlayer
}

// --- OTHER COMPOSABLES ---

@Composable
private fun GameContent(
    uiState: TriviaGameUiState,
    viewModel: TriviaGameViewModel,
    context: Context,
    showConfetti: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), // More transparent
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) // More transparent
                    )
                )
            ) // Gradient background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CountdownTimer(timeLeft = uiState.timeLeft)
        QuestionDisplay(question = uiState.currentQuestion?.question ?: stringResource(R.string.loading))
        Spacer(modifier = Modifier.height(16.dp)) // Add some space
        AnswerButtons(uiState, viewModel)

        // Show leaderboard *after* game over, outside the dialog
        if (uiState.isGameOver) {
            Spacer(modifier = Modifier.height(24.dp))
            Leaderboard(scores = uiState.leaderboard)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.restartGame() }) {
                Text(stringResource(R.string.restart_game))
            }
        }

        if (showConfetti) {
            ConfettiAnimation()
        }
        GameOverScreen(uiState, viewModel, context)
    }
}
@Composable
private fun CountdownTimer(timeLeft: Int) {
    AnimatedContent(
        targetState = timeLeft,
        transitionSpec = {
            @Suppress("DEPRECATION")
            (slideInVertically { -it } + fadeIn()) with (slideOutVertically { it } + fadeOut())
        },
        label = "countdownTimerAnimation"
    ) { time ->
        Text(
            text = stringResource(R.string.time_left, time),
            style = MaterialTheme.typography.headlineLarge, // Larger and bolder
            fontWeight = FontWeight.Bold,
            color = if (time < 5) Color.Red else MaterialTheme.colorScheme.onSurface, // Use onSurface for contrast
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun QuestionDisplay(question: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Add a subtle shadow
    ) {
        AnimatedContent(
            targetState = question,
            transitionSpec = {
                @Suppress("DEPRECATION")
                (slideInVertically { -it } + fadeIn()) with (slideOutVertically { it } + fadeOut())
            },
            label = "questionDisplayAnimation"
        ) { displayedQuestion ->
            Text(
                text = decodeHtmlEntities(displayedQuestion),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnswerButtons(uiState: TriviaGameUiState, viewModel: TriviaGameViewModel) {
    uiState.currentQuestion?.let { currentQuestion ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            uiState.currentAnswers.forEach { answer ->
                val buttonColor = when {
                    uiState.answerFeedback != AnswerFeedback.NONE && answer == currentQuestion.correctAnswer -> Color(
                        0xFF4CAF50
                    ).copy(alpha = .8f) // Darker Green
                    uiState.answerFeedback == AnswerFeedback.INCORRECT && answer == uiState.selectedAnswer -> Color(
                        0xFFE91E63
                    ).copy(alpha = .8f) // Darker Red
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = .8f)
                }

                Button(
                    onClick = {
                        if (uiState.answerFeedback == AnswerFeedback.NONE) {
                            viewModel.checkAnswer(answer)
                        }
                    },
                    enabled = uiState.answerFeedback == AnswerFeedback.NONE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(60.dp),
                    colors = buttonColors(
                        containerColor = buttonColor,
                        disabledContainerColor = buttonColor,
                        contentColor = Color.Black // Explicitly set text color
                    ),
                    shape = RoundedCornerShape(12.dp), // Rounded corners
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 2.dp
                    ) // Elevation
                ) {
                    Text(
                        text = decodeHtmlEntities(answer),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        color = Color.Black
                    ) // Black text
                }
            }
        }
    }
}


//Game Over Screen, add dismiss functionality.
@Composable
private fun GameOverScreen(
    uiState: TriviaGameUiState,
    viewModel: TriviaGameViewModel,
    context: Context
) {
    if (uiState.isGameOver) {
        AlertDialog(
            onDismissRequest = {
                viewModel.restartGame() // Go back to settings on dismiss
            },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Game Over") }, // Add an icon
            title = {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally // Center content
                ) {
                    Text(
                        text = "Your Score: ${uiState.score}",
                        style = MaterialTheme.typography.bodyLarge, // Use bodyLarge for the score
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    //Optional, uncomment to add leaderboard to dialog.
                    //Leaderboard(scores = uiState.leaderboard)
                    Spacer(modifier =  Modifier.height(16.dp)) //Give some space for the buttons.
                    ShareScoreButton(context = context, score = uiState.score) //Added share button

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restartGame() // Restart the game (go to settings)
                    },

                    ) {
                    Text("Play Again")
                }
            },
            dismissButton = { // Add a dismiss button
                Button(
                    onClick = {
                        viewModel.restartGame()
                    }
                ) {
                    Text("Dismiss")
                }
            },
            shape = RoundedCornerShape(16.dp) // Rounded corners for the dialog
        )
    }
}



@Composable
private fun ShareScoreButton(context: Context, score: Int) {
    IconButton(onClick = { shareScore(context, score) }) {
        Icon(Icons.Default.Share, contentDescription = "Share Score")
    }
}

private fun shareScore(context: Context, score: Int) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "I scored $score points in the Trivia Game! Can you beat me?")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Share your score"))
}

@Composable
fun SettingsScreen(onSettingsApplied: (String, String, Int) -> Unit) {
    var difficulty by remember { mutableStateOf("easy") }
    var category by remember { mutableStateOf("any") }
    var questionCount by remember { mutableIntStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DifficultySelection(difficulty) { selectedDifficulty -> difficulty = selectedDifficulty }
        CategorySelection(category) { selectedCategory -> category = selectedCategory }
        QuestionCountSelection(questionCount) { selectedCount -> questionCount = selectedCount }

        Button(onClick = {
            onSettingsApplied(
                difficulty,
                category,
                questionCount
            )
        }) {
            Text("Start Game")
        }
    }
}
@Composable
private fun DifficultySelection(selectedDifficulty: String, onDifficultySelected: (String) -> Unit) {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Difficulty: ", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { onDifficultySelected("easy") },
            colors = buttonColors(containerColor = if (selectedDifficulty == "easy") Color.Green else MaterialTheme.colorScheme.primary)) { Text("Easy") }
        Button(onClick = { onDifficultySelected("medium") },
            colors = buttonColors(containerColor = if (selectedDifficulty == "medium") Color.Green else MaterialTheme.colorScheme.primary)) { Text("Medium") }
        Button(onClick = { onDifficultySelected("hard") },
            colors = buttonColors(containerColor = if (selectedDifficulty == "hard") Color.Green else MaterialTheme.colorScheme.primary)) { Text("Hard") }
    }
}

@Composable
private fun CategorySelection(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Category: ", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { onCategorySelected("any") },
            colors = buttonColors(containerColor = if (selectedCategory == "any") Color.Green else MaterialTheme.colorScheme.primary)) { Text("Any") }
        Button(onClick = { onCategorySelected("science") },
            colors = buttonColors(containerColor = if (selectedCategory == "science") Color.Green else MaterialTheme.colorScheme.primary)) { Text("Science") }
        Button(onClick = { onCategorySelected("history") },
            colors = buttonColors(containerColor = if (selectedCategory == "history") Color.Green else MaterialTheme.colorScheme.primary)) { Text("History") }
    }
}

@Composable
private fun QuestionCountSelection(selectedCount: Int, onCountSelected: (Int) -> Unit) {
    Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Questions: ", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { onCountSelected(5) },
            colors = buttonColors(containerColor = if (selectedCount == 5) Color.Green else MaterialTheme.colorScheme.primary)) { Text("5") }
        Button(onClick = { onCountSelected(10)},
            colors = buttonColors(containerColor = if (selectedCount == 10) Color.Green else MaterialTheme.colorScheme.primary)) { Text("10") }
        Button(onClick = { onCountSelected(20) },
            colors = buttonColors(containerColor = if (selectedCount == 20) Color.Green else MaterialTheme.colorScheme.primary)) { Text("20") }
    }
}

@Composable
fun ConfettiAnimation() {
    Text(
        text = "🎉🎉🎉",
        style = MaterialTheme.typography.displayLarge,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun Leaderboard(scores: List<Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp), // Add some padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, // Make leaderboard title bold
            modifier = Modifier.padding(bottom = 8.dp)
        )
        scores.forEachIndexed { index, score ->
            Text(
                text = "${index + 1}. $score points",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}