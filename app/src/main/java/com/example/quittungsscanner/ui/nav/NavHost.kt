package com.example.quittungsscanner.ui.nav

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quittungsscanner.ui.screens.receipts.AddReceiptScreen
import com.example.quittungsscanner.ui.screens.receipts.ReceiptSavedScreen
import com.example.quittungsscanner.ui.screens.auswertungen.AuswertungenScreen
import com.example.quittungsscanner.ui.screens.receipts.EditReceiptScreen
import com.example.quittungsscanner.ui.screens.auswertungen.HomeScreen
import com.example.quittungsscanner.ui.screens.ProfileScreen
import com.example.quittungsscanner.ui.screens.receipts.ReceiptScreen
import com.example.quittungsscanner.ui.screens.Screens

@Composable
fun MyNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val slideInAnimation = slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
    val slideOutAnimation = slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()

    NavHost(
        navController = navHostController,
        startDestination = Screens.Home.name,
        modifier = modifier

    ) {
        composable(route = Screens.Home.name) {
            HomeScreen()
        }
        composable(
            route = Screens.Auswertungen.name,
        ) {
            AuswertungenScreen()
        }
        composable(
            route = Screens.addReceipt.name,
        ) {
            AddReceiptScreen(navHostController)
        }
        composable(
            route = Screens.Receipts.name,
        ) {
            ReceiptScreen(navHostController)
        }
        composable(
            route = Screens.Profil.name,
        ) {
            ProfileScreen()
        }
        composable(
            Screens.savedReceipt.name
        ) {
            ReceiptSavedScreen(navHostController)
        }
        composable(
            route = "editReceipt/{receiptId}"
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId")?.toLongOrNull() ?: 0L

            // Hier rufst du die EditReceiptScreen-Composable auf und übergibst die erforderlichen Parameter
            EditReceiptScreen(
                receiptId = receiptId,
                navController = navHostController
            )
        }
    }
}



