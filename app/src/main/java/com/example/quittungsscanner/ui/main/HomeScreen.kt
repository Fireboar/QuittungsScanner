package com.example.quittungsscanner.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quittungsscanner.exp
import com.example.quittungsscanner.gold

@Composable
fun HomeScreen(navHostController: NavHostController) {

    Column(
        modifier = Modifier
            .fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                navHostController.navigate(Screens.Game.name)
            }
        ) {
            Text(
                text = "Play",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navHostController.navigate("${Screens.Profil.name}/$exp/$gold")
            }
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navHostController.navigate(Screens.Users.name)
            }
        ) {
            Text(
                text = "UserCreation",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navHostController.navigate(Screens.Bands.name)
            }
        ) {
            Text(
                text = Screens.Bands.name,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navHostController.navigate(Screens.Components.name)
            }
        ) {
            Text(
                text = Screens.Components.name,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}