package com.example.quittungsscanner.ui.bands

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quittungsscanner.ui.Screens.Screens

@Composable
fun BandScreen(bandsViewModel: BandsViewModel, navController: NavController) {
    bandsViewModel.requestBandCodesFromServer()
    val bands = bandsViewModel.bandsFlow.collectAsState()
    Column (modifier = Modifier.padding(10.dp)){
        bands.value.forEach{ band ->
            Row {
                Text(
                    modifier = Modifier.padding(5.dp).clickable {
                        navController.navigate(
                            route = "${Screens.BandInfo.name}/${band.code}"
                        )
                    },
                    text = band.name
                )
            }

        }
    }
}