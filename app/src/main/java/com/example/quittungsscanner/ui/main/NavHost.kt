package com.example.quittungsscanner.ui.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quittungsscanner.ui.bands.BandInfo
import com.example.quittungsscanner.ui.bands.BandInfoScreen
import com.example.quittungsscanner.ui.bands.BandScreen
import com.example.quittungsscanner.ui.bands.BandsViewModel
import com.example.quittungsscanner.ui.components.ComponentScreen
import com.example.quittungsscanner.ui.user.UserScreen

@Composable
fun MyNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val bandsViewModel: BandsViewModel = viewModel()

    val slideInAnimation = slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
    val slideOutAnimation = slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
    NavHost(
        navController = navHostController,
        startDestination = Screens.Home.name,
        modifier = modifier

    ) {
        composable(
            route = "${Screens.Profil.name}/{exp}/{gold}",
            enterTransition = { slideInAnimation },
            exitTransition = { slideOutAnimation },
            arguments = listOf(
                navArgument("exp") {
                    type = NavType.IntType
                },
                navArgument("gold") {
                    type = NavType.IntType
                }
            )
        ) { navBackStackEntry ->
            val exp = navBackStackEntry.arguments?.getInt("exp") ?: 0
            val gold = navBackStackEntry.arguments?.getInt("gold") ?: 0
            ProfileScreen(exp = exp, gold = gold)
        }
        composable(route = Screens.Home.name) {
            HomeScreen(navHostController)
        }
        composable(
            route = Screens.Game.name,
        ) {
            GameScreen()
        }
        composable(
            route = Screens.Users.name,
        ) {
            UserScreen()
        }
        composable(
            route = Screens.Bands.name,
        ) {
            BandScreen(bandsViewModel, navHostController)
        }
        composable(
            route = Screens.Components.name,
        ) {
            ComponentScreen()
        }
        composable(
            route = "${Screens.BandInfo.name}/{bandNumber}",
            arguments = listOf(
                navArgument("bandNumber") {
                    type = NavType.StringType
                }
            )
        ) { navBackStackEntry ->
            val bandNumber = navBackStackEntry.arguments?.getString("bandNumber") ?: "null"

            bandsViewModel.requestBandInfoFromServer(bandNumber)
            val currentBand = bandsViewModel.currentBand.collectAsState(initial = BandInfo("Loading", 0, "Unknown", null)).value
            if (currentBand != null) {
                BandInfoScreen(currentBand)
            }
        }
    }
}

