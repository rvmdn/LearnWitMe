package com.learnwitme.core.domain.model

/**
 * Responsibility: Serves as the basic block of the curriculum, coordinating
 * a specific KnowledgeCard and its associated assessment Quiz.
 */
data class LearningUnit(
    override val id: LearningUnitId,
    val title: String,
    val cardId: KnowledgeCardId,
    val quizId: QuizId?,
    val estimatedMinutes: Int,
    val topicCode: String
) : Entity<LearningUnitId> {
    init {
        require(title.isNotBlank()) { "Learning unit title cannot be blank" }
        require(estimatedMinutes > 0) { "Estimated minutes must be positive" }
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
    }
}
