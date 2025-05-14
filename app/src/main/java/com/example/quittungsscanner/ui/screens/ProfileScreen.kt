package com.example.quittungsscanner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.padding(20.dp)) {
        Column {
            AsyncImage(
                model = "",
                modifier = Modifier.height(height = 200.dp),
                contentDescription = "null"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}