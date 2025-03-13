package com.example.vibesshared.ui.ui.repository

import android.annotation.SuppressLint
import android.text.Html
import android.text.Html.fromHtml
import androidx.annotation.WorkerThread
import com.example.vibesshared.ui.ui.data.Question
import com.example.vibesshared.ui.ui.data.TriviaResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TriviaRepository @Inject constructor(
    private val apiService: TriviaApiService,
    context: android.content.Context
) {
    @WorkerThread
    suspend fun getQuestions(
        difficulty: String,
        category: String,
        questionCount: Int
    ): List<Question> {
        println("===== DETAILED QUEST QUESTION FETCH =====")
        println("Difficulty: $difficulty")
        println("Category: $category")
        println("Question Count: $questionCount")

        val categoryId = getCategoryCode(category)
        println("Mapped Category ID: $categoryId")

        return try {
            val response = apiService.getQuestions(
                amount = questionCount,
                difficulty = difficulty.lowercase(),
                category = categoryId,
                type = "multiple"
            )

            println("API Response Raw: $response")
            println("API Response - Results size: ${response.results.size}")

            val questions = response.results.map {
                val cleanQuestion = cleanQuestionText(it.question)
                val cleanCorrectAnswer = cleanQuestionText(it.correct_answer)
                val cleanIncorrectAnswers =
                    it.incorrect_answers.map { answer -> cleanQuestionText(answer) }
                Question(
                    question = cleanQuestion,
                    correctAnswer = cleanCorrectAnswer,
                    incorrectAnswers = cleanIncorrectAnswers
                )
            }

            println("Processed Questions:")
            questions.forEachIndexed { index, question ->
                println("Question $index: ${question.question}")
                println("Correct Answer: ${question.correctAnswer}")
                println("Incorrect Answers: ${question.incorrectAnswers}")
            }

            questions
        } catch (e: Exception) {
            println("QUEST QUESTION FETCH ERROR: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getCategoryCode(category: String): Int? {
        return when (category.lowercase()) {
            "science" -> 17
            "history" -> 23
            "entertainment" -> 12
            "any" -> null
            else -> {
                println("WARNING: Unmapped category: $category")
                null
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun cleanQuestionText(text: String): String {
        val decoded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            fromHtml(text).toString()
        }
        return decoded.replace(Regex("[^\\p{L}\\p{N}\\s.,!?'-]"), "")
    }

    @WorkerThread
    suspend fun getDailyChallengeQuestions(): List<Question> {
        val response = apiService.getDailyChallengeQuestions()
        return response.results.map { it.toQuestion() }
    }
}

interface TriviaApiService {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 10,
        @Query("difficulty") difficulty: String = "easy",
        @Query("category") category: Int? = null,
        @Query("type") type: String = "multiple"
    ): TriviaResponse

    @GET("api_daily_challenge.php")
    suspend fun getDailyChallengeQuestions(): TriviaResponse
}

fun TriviaResponse.Result.toQuestion(): Question {
    return Question(
        question = question,
        correctAnswer = correct_answer,
        incorrectAnswers = incorrect_answers
    )
}

class TriviaRetrofit @Inject constructor() {
    fun createApiService(): TriviaApiService {
        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApiService::class.java)
    }
}