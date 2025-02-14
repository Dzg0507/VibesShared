package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.AnswerFeedback
import com.example.vibesshared.ui.ui.data.Question
import com.example.vibesshared.ui.ui.repository.TriviaRepository
import com.example.vibesshared.ui.ui.screens.TriviaGameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriviaGameViewModel @Inject constructor(
    private val repository: TriviaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriviaGameUiState())
    val uiState: StateFlow<TriviaGameUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null //Holds the timer job.

    init {
        // Initialize uiState properly.
        _uiState.value = TriviaGameUiState(showSettings = true)
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            val savedScores = repository.getLeaderboard().first()
            _uiState.update { it.copy(leaderboard = savedScores) }
        }
    }

    fun startGame(difficulty: String, category: String, questionCount: Int) {
        viewModelScope.launch {
            try {
                val questions = repository.getQuestions(difficulty, category, questionCount)
                // Reset ALL relevant state.
                _uiState.value = TriviaGameUiState(
                    questions = questions,
                    currentQuestionIndex = 0,
                    showSettings = false,
                    timeLeft = 60, // Initial time
                    isGameOver = false,
                    score = 0,
                    answerFeedback = AnswerFeedback.NONE,
                    selectedAnswer = null
                )
                showNextQuestion()
            } catch (_: Exception) {
                // Handle error
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel() // Cancel any existing timer.  CRITICAL!
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver) {
                delay(1000)
                _uiState.update { currentState ->
                    currentState.copy(timeLeft = currentState.timeLeft - 1) // Decrement time
                }
            }
            if (_uiState.value.timeLeft == 0) {
                handleTimeout()
            }
        }
    }

    private fun handleTimeout() {
        _uiState.update { currentState ->
            currentState.copy(
                answerFeedback = AnswerFeedback.INCORRECT,
                selectedAnswer = null
            )
        }
        timerJob?.cancel() // Stop the timer.
        viewModelScope.launch {
            delay(2000) // Delay before next question.
            showNextQuestion()
        }
    }

    internal fun showNextQuestion() { // 'internal' visibility
        _uiState.update { currentState ->
            if (currentState.currentQuestionIndex < currentState.questions.size) {
                val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
                val shuffledAnswers = shuffleAnswers(currentQuestion)
                currentState.copy(
                    currentQuestion = currentQuestion,
                    currentAnswers = shuffledAnswers,
                    timeLeft = 15,  // Reset timer
                    answerFeedback = AnswerFeedback.NONE,
                    selectedAnswer = null,
                    currentQuestionIndex = currentState.currentQuestionIndex + 1
                )
            } else {
                saveScoreToLeaderboard()
                currentState.copy(isGameOver = true)
            }
        }
        if (!_uiState.value.isGameOver) { // Only start timer if game isn't over.
            startTimer()
        }
    }

    private fun shuffleAnswers(question: Question): List<String> {
        return (question.incorrectAnswers + question.correctAnswer).shuffled()
    }

    fun checkAnswer(selectedAnswer: String) {
        timerJob?.cancel() // Cancel the timer immediately when an answer is chosen.
        if (selectedAnswer == _uiState.value.currentQuestion?.correctAnswer) {
            _uiState.update { currentState ->
                currentState.copy(
                    score = currentState.score + 1,
                    answerFeedback = AnswerFeedback.CORRECT,
                    selectedAnswer = selectedAnswer
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    answerFeedback = AnswerFeedback.INCORRECT,
                    selectedAnswer = selectedAnswer
                )
            }
        }
    }
    fun restartGame() {
        timerJob?.cancel() // Cancel timer.
        _uiState.value = TriviaGameUiState(showSettings = true) // Reset to settings screen.
    }

    private fun saveScoreToLeaderboard() {
        viewModelScope.launch {
            val updatedScores = _uiState.value.leaderboard.toMutableList().apply {
                add(_uiState.value.score)
            }.sortedDescending().take(5)
            repository.saveLeaderboard(updatedScores)
            _uiState.update { it.copy(leaderboard = updatedScores) }
        }
    }
}