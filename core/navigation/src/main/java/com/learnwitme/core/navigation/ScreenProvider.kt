package com.learnwitme.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface ScreenProvider {
    val route: String
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    )
}
