package com.learnwitme.core.domain.model

/**
 * Responsibility: Records the outcome of a user's quiz attempt, detailing
 * correctness of answers, overall score, and timestamp of completion.
 */
data class QuizResult(
    override val id: QuizResultId,
    val quizId: QuizId,
    val score: Double,
    val answers: List<QuestionAnswer>,
    val completedTimestamp: Long
) : Entity<QuizResultId> {
    init {
        require(score in 0.0..1.0) { "Quiz score must be between 0.0 and 1.0" }
        require(answers.isNotEmpty()) { "Quiz result must contain answers" }
        require(completedTimestamp > 0) { "Completed timestamp must be positive" }
    }
}

/**
 * Responsibility: Represents the recorded answer for a single question.
 */
data class QuestionAnswer(
    val questionId: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean
) : ValueObject {
    init {
        require(questionId.isNotBlank()) { "Question ID cannot be blank" }
        require(selectedOptionIndex >= 0) { "Selected option index cannot be negative" }
    }
}
