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
        composable(route = Screens.Home.name) {
            HomeScreen(navHostController)
        }
        composable(
            route = Screens.Auswertungen.name,
        ) {
            AuswertungenScreen()
        }
        composable(
            route = Screens.AddQuittung.name,
        ) {
            AddQuittung()
        }
        composable(
            route = Screens.Quittungen.name,
        ) {
            Quittungen()
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

