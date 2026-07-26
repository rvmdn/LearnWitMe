package com.learnwitme.core.storage.repository

import android.content.Context
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.QuizRepository
import com.learnwitme.core.storage.local.dao.QuizDao
import com.learnwitme.core.storage.local.entity.*
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
    @ApplicationContext private val context: Context,
    private val quizDao: QuizDao
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val quizzesFile = File(context.filesDir, "quizzes.json")
    private val resultsFile = File(context.filesDir, "quiz_results.json")

    private fun loadLegacyQuizzes(): MutableMap<String, QuizDto> {
        if (!quizzesFile.exists()) return mutableMapOf()
        return try {
            val content = quizzesFile.readText()
            json.decodeFromString<Map<String, QuizDto>>(content).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun loadLegacyResults(): MutableList<QuizResultDto> {
        if (!resultsFile.exists()) return mutableListOf()
        return try {
            val content = resultsFile.readText()
            json.decodeFromString<List<QuizResultDto>>(content).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    override suspend fun getQuiz(id: QuizId): Quiz? {
        // 1. Try fetching from Room Database
        val quizEntity = quizDao.getQuizById(id.value)
        if (quizEntity != null) {
            val questionsEntity = quizDao.getQuestionsForQuiz(id.value)
            val optionsEntity = quizDao.getOptionsForQuiz(id.value)
            
            val questions = questionsEntity.map { q ->
                val opts = optionsEntity
                    .filter { it.questionId == q.id }
                    .sortedBy { it.optionIndex }
                    .map { QuizOption(it.text, it.explanation) }
                QuizQuestion(q.id, q.questionText, opts, q.correctOptionIndex)
            }
            return Quiz(
                id = QuizId(quizEntity.id),
                title = quizEntity.title,
                questions = questions,
                targetDifficulty = DifficultyLevel.valueOf(quizEntity.targetDifficulty)
            )
        }

        // 2. Coexistence Mode: Fallback to legacy JSON storage & migrate to Room
        val legacyQuiz = loadLegacyQuizzes()[id.value]?.toDomain()
        if (legacyQuiz != null) {
            saveQuizToRoom(legacyQuiz)
            return legacyQuiz
        }

        return null
    }

    override suspend fun saveQuiz(quiz: Quiz) {
        // Save exclusively into Room Database (No raw file writes for new quizzes)
        saveQuizToRoom(quiz)
    }

    private suspend fun saveQuizToRoom(quiz: Quiz) {
        val quizEntity = QuizEntity(
            id = quiz.id.value,
            title = quiz.title,
            targetDifficulty = quiz.targetDifficulty.name
        )
        val questionEntities = mutableListOf<QuizQuestionEntity>()
        val optionEntities = mutableListOf<QuizOptionEntity>()

        quiz.questions.forEach { question ->
            questionEntities.add(
                QuizQuestionEntity(
                    quizId = quiz.id.value,
                    id = question.id,
                    questionText = question.questionText,
                    correctOptionIndex = question.correctOptionIndex
                )
            )
            question.options.forEachIndexed { index, option ->
                optionEntities.add(
                    QuizOptionEntity(
                        quizId = quiz.id.value,
                        questionId = question.id,
                        optionIndex = index,
                        text = option.text,
                        explanation = option.explanation
                    )
                )
            }
        }

        quizDao.insertQuiz(quizEntity)
        quizDao.insertQuestions(questionEntities)
        quizDao.insertOptions(optionEntities)
    }

    override suspend fun getQuizResult(id: QuizResultId): QuizResult? {
        // 1. Try fetching from Room Database
        val resultEntity = quizDao.getQuizResultById(id.value)
        if (resultEntity != null) {
            val answers = json.decodeFromString<List<QuestionAnswerDto>>(resultEntity.answersJson).map { it.toDomain() }
            return QuizResult(
                id = QuizResultId(resultEntity.id),
                quizId = QuizId(resultEntity.quizId),
                score = resultEntity.score,
                answers = answers,
                completedTimestamp = resultEntity.completedTimestamp
            )
        }

        // 2. Coexistence Mode: Fallback to legacy JSON storage & migrate to Room
        val legacyResult = loadLegacyResults().find { it.id == id.value }?.toDomain()
        if (legacyResult != null) {
            saveQuizResultToRoom(legacyResult, "current_user")
            return legacyResult
        }

        return null
    }

    override suspend fun saveQuizResult(result: QuizResult) {
        // Save exclusively into Room Database (No raw file writes for new results)
        saveQuizResultToRoom(result, "current_user")
    }

    private suspend fun saveQuizResultToRoom(result: QuizResult, userId: String) {
        val answersJson = json.encodeToString(result.answers.map { it.toDto() })
        val resultEntity = QuizResultEntity(
            id = result.id.value,
            quizId = result.quizId.value,
            userId = userId,
            score = result.score,
            answersJson = answersJson,
            completedTimestamp = result.completedTimestamp
        )
        quizDao.insertQuizResult(resultEntity)
    }

    override suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult> {
        val roomResults = quizDao.getQuizResultsByUserId(userId.value)
        if (roomResults.isNotEmpty()) {
            return roomResults.map { entity ->
                val answers = json.decodeFromString<List<QuestionAnswerDto>>(entity.answersJson).map { it.toDomain() }
                QuizResult(
                    id = QuizResultId(entity.id),
                    quizId = QuizId(entity.quizId),
                    score = entity.score,
                    answers = answers,
                    completedTimestamp = entity.completedTimestamp
                )
            }
        }

        // Coexistence fallback for legacy results
        val legacyResults = loadLegacyResults().map { it.toDomain() }
        legacyResults.forEach { saveQuizResultToRoom(it, userId.value) }
        return legacyResults
    }
}
