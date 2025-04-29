package com.example.quittungsscanner

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quittungsscanner.ui.Screens.AddQuittungScreen
import com.example.quittungsscanner.ui.Screens.AuswertungenScreen
import com.example.quittungsscanner.ui.Screens.HomeScreen
import com.example.quittungsscanner.ui.Screens.ProfileScreen
import com.example.quittungsscanner.ui.Screens.QuittungenScreen
import com.example.quittungsscanner.ui.Screens.Screens
import com.example.quittungsscanner.ui.bands.BandsViewModel

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
            AddQuittungScreen()
        }
        composable(
            route = Screens.Quittungen.name,
        ) {
            QuittungenScreen()
        }
        composable(
            route = Screens.Profil.name,
        ) {
            ProfileScreen()
        }
    }
}

