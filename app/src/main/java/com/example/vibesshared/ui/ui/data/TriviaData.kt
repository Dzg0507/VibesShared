
package com.example.vibesshared.ui.ui.data

// Data class representing a single trivia question
data class Question(
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>
)

// Data class representing the API response from Open Trivia Database
data class TriviaResponse(
    val results: List<Result>
) {
    data class Result(
        val question: String,
        val correct_answer: String,
        val incorrect_answers: List<String>
    )
}

// Extension function to map API response to our Question model
fun TriviaResponse.Result.toQuestion(): Question {
    return Question(
        question = question,
        correctAnswer = correct_answer,
        incorrectAnswers = incorrect_answers
    )
}

// Enum class for answer feedback (Correct, Incorrect, or None)
enum class AnswerFeedback {
    NONE, CORRECT, INCORRECT
}