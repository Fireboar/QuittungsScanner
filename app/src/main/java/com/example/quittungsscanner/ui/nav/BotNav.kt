package com.example.quittungsscanner.ui.nav

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier


data class BottomNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

@Composable
fun BottomNavigation(
    navController: NavHostController,
    items: List<BottomNavigationItem>
){
    var selectedItem by remember {mutableIntStateOf(0)}



    NavigationBar{
        items.forEachIndexed {index, item ->
            val text = when (item.title) {
                "addReceipt" -> "Quittung hinzufügen"
                "editReceipt" -> "Quittung bearbeiten"
                "Receipts" -> "Quittungen"
                else -> item.title
            }

            NavigationBarItem(
                selected = index == selectedItem,
                onClick = {
                    selectedItem = index
                    navController.navigate(route = item.route){
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                },
                label = {
                    Text(
                        text = text,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                icon = {
                    BadgedBox(
                        badge = {
                            when {
                                item.badgeCount != null -> {
                                    Badge {
                                        Text(
                                            text = item.badgeCount.toString(),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                item.hasNews -> {
                                    Badge()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (index == selectedItem) {
                                item.selectedIcon
                            } else {
                                item.unselectedIcon
                            },
                            contentDescription = item.title,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

            )

        }
    }
}

