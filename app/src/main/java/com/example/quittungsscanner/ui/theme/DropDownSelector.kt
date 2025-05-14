package com.example.quittungsscanner.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>, // z. B. Monat "01" to "Januar"
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    Column {
        Text(label)
        Box {
            Button(onClick = { expanded.value = true }) {
                Text(selectedOption?.let { key ->
                    options.find { it.first == key }?.second ?: key
                } ?: "Alle")
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Alle") },
                    onClick = {
                        onOptionSelected(null)
                        expanded.value = false
                    }
                )
                options.forEach { (key, value) ->
                    DropdownMenuItem(
                        text = { Text(value) },
                        onClick = {
                            onOptionSelected(key)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}
