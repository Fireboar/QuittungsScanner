package com.example.quittungsscanner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.quittungsscanner.lvl

@Composable
fun ProfileScreen(exp: Int, gold: Int) {
    Box(modifier = Modifier.padding(20.dp)) {
        Column {
            AsyncImage(
                model = "https://preview.redd.it/mxzes0dktbib1.jpg?auto=webp&s=6a42caedf2a33c316d93fcc8b399724c6a08282d",
                modifier = Modifier.height(height = 200.dp),
                contentDescription = "null"
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Name: Katarina",
            )
            Text(
                text = "Exp: $exp",
            )
            Text(
                text = "Gold: $gold",
            )
            Text(
                text = "Level: $lvl",
            )
            ShowLevelBar()
        }
    }
}