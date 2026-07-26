package com.learnwitme.core.domain.model

import com.learnwitme.core.domain.service.Clock


/**
 * Responsibility: Represents a dynamic assessment containing a list of questions
 * targeting the user's current weak topics or learning focus.
 */
data class Quiz(
    override val id: QuizId,
    val title: String,
    val questions: List<QuizQuestion>,
    val targetDifficulty: DifficultyLevel
) : Entity<QuizId> {
    init {
        require(title.isNotBlank()) { "Quiz title cannot be blank" }
        require(questions.isNotEmpty()) { "Quiz must contain at least one question" }
    }
}

/**
 * Responsibility: Represents an individual question in a Quiz.
 */
data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<QuizOption>,
    val correctOptionIndex: Int
) : ValueObject {
    init {
        require(id.isNotBlank()) { "Question ID cannot be blank" }
        require(questionText.isNotBlank()) { "Question text cannot be blank" }
        require(options.size >= 2) { "Question must have at least 2 options" }
        require(correctOptionIndex in options.indices) { "Correct option index must be a valid option index" }
    }
}

/**
 * Responsibility: Represents an answer option for a QuizQuestion.
 */
data class QuizOption(
    val text: String,
    val explanation: String
) : ValueObject {
    init {
        require(text.isNotBlank()) { "Option text cannot be blank" }
    }
}

data class QuizAnswer(
    val questionId: String,
    val selectedOptionIndex: Int
) : ValueObject {
    init {
        require(questionId.isNotBlank()) { "Question ID cannot be blank" }
        require(selectedOptionIndex >= 0) { "Selected option index cannot be negative" }
    }
}

data class QuizAttempt(
    override val id: QuizAttemptId,
    val userId: UserId,
    val quiz: Quiz,
    val answers: List<QuizAnswer> = emptyList(),
    val isCompleted: Boolean = false
) : Entity<QuizAttemptId> {
    
    fun submitAnswer(questionId: String, selectedOptionIndex: Int): QuizAttempt {
        require(!isCompleted) { "Cannot submit answers to a completed quiz attempt" }
        val question = quiz.questions.find { it.id == questionId }
            ?: throw IllegalArgumentException("Question $questionId does not belong to this quiz")
        require(selectedOptionIndex in question.options.indices) { "Selected option index is out of bounds" }

        val newAnswers = answers.filter { it.questionId != questionId } + QuizAnswer(questionId, selectedOptionIndex)
        return this.copy(answers = newAnswers)
    }

    fun complete(clock: Clock): QuizResult {
        require(!isCompleted) { "Quiz attempt is already completed" }
        
        val answeredIds = answers.map { it.questionId }.toSet()
        val unanswered = quiz.questions.filter { !answeredIds.contains(it.id) }
        require(unanswered.isEmpty()) { "All questions must be answered before completing the quiz" }

        val questionAnswers = quiz.questions.map { question ->
            val answer = answers.find { it.questionId == question.id }!!
            val isCorrect = answer.selectedOptionIndex == question.correctOptionIndex
            QuestionAnswer(question.id, answer.selectedOptionIndex, isCorrect)
        }

        val correctCount = questionAnswers.count { it.isCorrect }
        val score = if (quiz.questions.isNotEmpty()) {
            correctCount.toDouble() / quiz.questions.size
        } else {
            0.0
        }

        return QuizResult(
            id = QuizResultId("qres_${id.value}"),
            quizId = quiz.id,
            score = score,
            answers = questionAnswers,
            completedTimestamp = clock.currentTimeMillis()
        )
    }
}

data class QuizDecision(
    val result: QuizResult,
    val updatedProgress: ProgressSummary,
    val updatedRecommendation: RecommendationDecision
)

