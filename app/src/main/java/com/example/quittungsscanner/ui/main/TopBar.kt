package com.example.quittungsscanner.ui.main

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quittungsscanner.components.MusicPlayerService
import com.example.quittungsscanner.exp
import com.example.quittungsscanner.lvl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val screenName = currentRoute?.substringBefore("/") ?: "Unknown Screen"
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(
                text = screenName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        },
        navigationIcon = {
            // Back-Button
            IconButton(onClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Zurück",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (screenName == Screens.Components.name) {
                // Play Button (Start service)
                IconButton(
                    onClick = {
                        val intent = Intent(context, MusicPlayerService::class.java)
                        context.startService(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Start Service",
                        tint = Color.White
                    )
                }

                // Stop Button (Stop service)
                IconButton(
                    onClick = {
                        val intent = Intent(context, MusicPlayerService::class.java)
                        context.stopService(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Stop Service",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun ShowLevelBar() {
    val totalPositions = (3 + 1.5 * lvl).toInt()

    val filledPositions = (exp / 100).coerceAtMost(totalPositions)
    val emptyPositions = totalPositions - filledPositions

    val expBar = "#".repeat(filledPositions) + "-".repeat(emptyPositions)

    Text(text = "[$expBar] $exp/${100 * totalPositions} XP")
}