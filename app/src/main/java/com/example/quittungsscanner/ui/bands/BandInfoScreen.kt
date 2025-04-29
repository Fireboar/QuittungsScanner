package com.example.quittungsscanner.ui.bands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun BandInfoScreen(currentBand: BandInfo) {
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = currentBand.name
        )
        Text(
            modifier = Modifier.padding(5.dp),
            text = "${currentBand.homeCountry}, ${currentBand.foundingYear}",

            )
        AsyncImage(
            modifier = Modifier.padding(5.dp).height(height = 200.dp),
            model = currentBand.bestOfCdCoverImageUrl,
            contentDescription = null
        )
    }

}