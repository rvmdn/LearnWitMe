package com.learnwitme.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object DailyFeedRoute

@Serializable
data class KnowledgeCardRoute(val cardId: String)

@Serializable
data class QuizRoute(val cardId: String)

@Serializable
object RoadmapRoute

@Serializable
object ProfileRoute

@Serializable
object OnboardingRoute

