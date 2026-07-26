package com.learnwitme.app.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.learnwitme.app.ui.onboarding.OnboardingScreen
import com.learnwitme.app.ui.onboarding.OnboardingViewModel
import com.learnwitme.core.navigation.*
import com.learnwitme.feature.dailyfeed.DailyFeedScreen
import com.learnwitme.feature.dailyfeed.DailyFeedViewModel
import com.learnwitme.feature.dailyfeed.DailyFeedEvent
import com.learnwitme.feature.dailyfeed.DailyFeedEffect
import com.learnwitme.feature.knowledgecard.*
import com.learnwitme.feature.quiz.QuizScreen
import com.learnwitme.feature.quiz.QuizViewModel
import com.learnwitme.feature.quiz.QuizEvent
import com.learnwitme.feature.onboarding.ProfileScreen
import com.learnwitme.feature.onboarding.ProfileViewModel
import javax.inject.Inject

class HomeScreenProvider @Inject constructor() : ScreenProvider {
    override val route: String = "home"
    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<DailyFeedRoute> {
            val viewModel: DailyFeedViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            LaunchedEffect(Unit) {
                viewModel.handleEvent(DailyFeedEvent.OnRefresh)
            }

            LaunchedEffect(Unit) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is DailyFeedEffect.NavigateToCard -> {
                            navController.navigate(KnowledgeCardRoute(effect.cardId))
                        }
                        is DailyFeedEffect.ShowToast -> {
                            android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            DailyFeedScreen(
                state = state,
                onEvent = viewModel::handleEvent,
                onNavigateToQuiz = { cardId ->
                    navController.navigate(QuizRoute(cardId))
                }
            )
        }
    }
}

class ProfileScreenProvider @Inject constructor() : ScreenProvider {
    override val route: String = "profile"
    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<ProfileRoute> {
            val viewModel: ProfileViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            ProfileScreen(
                state = state,
                onEvent = viewModel::handleEvent,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

class KnowledgeCardScreenProvider @Inject constructor() : ScreenProvider {
    override val route: String = "knowledge_card"
    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<KnowledgeCardRoute> { backStackEntry ->
            val route: KnowledgeCardRoute = backStackEntry.toRoute()
            val viewModel: KnowledgeCardViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            LaunchedEffect(route.cardId) {
                viewModel.handleEvent(KnowledgeCardEvent.OnLoadCard(route.cardId))
            }

            LaunchedEffect(Unit) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is KnowledgeCardEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is KnowledgeCardEffect.ShowToast -> {
                            android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            KnowledgeCardScreen(
                state = state,
                onEvent = viewModel::handleEvent,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

class QuizScreenProvider @Inject constructor() : ScreenProvider {
    override val route: String = "quiz"
    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<QuizRoute> { backStackEntry ->
            val route: QuizRoute = backStackEntry.toRoute()
            val viewModel: QuizViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            val quizId = "qz_" + route.cardId.removePrefix("c_")
            LaunchedEffect(quizId) {
                viewModel.handleEvent(QuizEvent.OnStartQuiz(quizId))
            }

            QuizScreen(
                state = state,
                onEvent = viewModel::handleEvent,
                onBack = {
                    navController.popBackStack(DailyFeedRoute, inclusive = false)
                }
            )
        }
    }
}

class OnboardingScreenProvider @Inject constructor() : ScreenProvider {
    override val route: String = "onboarding"
    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<OnboardingRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(DailyFeedRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}
