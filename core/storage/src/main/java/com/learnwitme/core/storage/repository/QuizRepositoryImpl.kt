package com.learnwitme.core.storage.repository

import android.content.Context
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.QuizRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class QuizOptionDto(val text: String, val explanation: String)

@Serializable
data class QuizQuestionDto(
    val id: String,
    val questionText: String,
    val options: List<QuizOptionDto>,
    val correctOptionIndex: Int
)

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val questions: List<QuizQuestionDto>,
    val targetDifficulty: String
)

@Serializable
data class QuestionAnswerDto(
    val questionId: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean
)

@Serializable
data class QuizResultDto(
    val id: String,
    val quizId: String,
    val score: Double,
    val answers: List<QuestionAnswerDto>,
    val completedTimestamp: Long
)

fun QuizOption.toDto() = QuizOptionDto(text, explanation)
fun QuizOptionDto.toDomain() = QuizOption(text, explanation)

fun QuizQuestion.toDto() = QuizQuestionDto(id, questionText, options.map { it.toDto() }, correctOptionIndex)
fun QuizQuestionDto.toDomain() = QuizQuestion(id, questionText, options.map { it.toDomain() }, correctOptionIndex)

fun Quiz.toDto() = QuizDto(id.value, title, questions.map { it.toDto() }, targetDifficulty.name)
fun QuizDto.toDomain() = Quiz(QuizId(id), title, questions.map { it.toDomain() }, DifficultyLevel.valueOf(targetDifficulty))

fun QuestionAnswer.toDto() = QuestionAnswerDto(questionId, selectedOptionIndex, isCorrect)
fun QuestionAnswerDto.toDomain() = QuestionAnswer(questionId, selectedOptionIndex, isCorrect)

fun QuizResult.toDto() = QuizResultDto(id.value, quizId.value, score, answers.map { it.toDto() }, completedTimestamp)
fun QuizResultDto.toDomain() = QuizResult(QuizResultId(id), QuizId(quizId), score, answers.map { it.toDomain() }, completedTimestamp)

@Singleton
class QuizRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val quizzesFile = File(context.filesDir, "quizzes.json")
    private val resultsFile = File(context.filesDir, "quiz_results.json")

    private fun loadQuizzes(): MutableMap<String, QuizDto> {
        if (!quizzesFile.exists()) return mutableMapOf()
        return try {
            val content = quizzesFile.readText()
            json.decodeFromString<Map<String, QuizDto>>(content).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun saveQuizzes(quizzes: Map<String, QuizDto>) {
        try {
            val content = json.encodeToString(quizzes)
            quizzesFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadResults(): MutableList<QuizResultDto> {
        if (!resultsFile.exists()) return mutableListOf()
        return try {
            val content = resultsFile.readText()
            json.decodeFromString<List<QuizResultDto>>(content).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveResults(results: List<QuizResultDto>) {
        try {
            val content = json.encodeToString(results)
            resultsFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getQuiz(id: QuizId): Quiz? {
        return loadQuizzes()[id.value]?.toDomain()
    }

    override suspend fun saveQuiz(quiz: Quiz) {
        val quizzes = loadQuizzes()
        quizzes[quiz.id.value] = quiz.toDto()
        saveQuizzes(quizzes)
    }

    override suspend fun getQuizResult(id: QuizResultId): QuizResult? {
        return loadResults().find { it.id == id.value }?.toDomain()
    }

    override suspend fun saveQuizResult(result: QuizResult) {
        val results = loadResults()
        results.removeAll { it.id == result.id.value }
        results.add(result.toDto())
        saveResults(results)
    }

    override suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult> {
        return loadResults().map { it.toDomain() }
    }
}
