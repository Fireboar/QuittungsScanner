package com.example.quittungsscanner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quittungsscanner.data.user.UserViewModel
import com.example.quittungsscanner.ui.bands.BandsViewModel
import com.example.quittungsscanner.ui.Bars.BottomNavigation
import com.example.quittungsscanner.ui.Bars.BottomNavigationItem
import com.example.quittungsscanner.ui.Bars.CustomTopBar
import com.example.quittungsscanner.ui.Screens.Screens
import com.example.quittungsscanner.ui.theme.QuittungsScannerTheme
import dagger.hilt.android.AndroidEntryPoint


var lvl = 3
var exp = 200
var gold = 20

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channel = NotificationChannel(
            ".music.musicChannel",
            "Music-Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        enableEdgeToEdge()
        setContent {
            // UserCount
            val userViewModel: UserViewModel by viewModels()
            val userList by userViewModel.getUsers().collectAsState(initial = emptyList())
            val userCount = userList.size

            // BandCount
            val bandsViewModel: BandsViewModel by viewModels()
            val bandCount by bandsViewModel.bandCount.collectAsState()

            QuittungsScannerTheme {
                val navigationItems = listOf(
                    BottomNavigationItem(
                        route = Screens.Home.name,
                        title = Screens.Home.name,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        route = Screens.Auswertungen.name,
                        title = Screens.Auswertungen.name,
                        selectedIcon = Icons.Filled.InsertChart,
                        unselectedIcon = Icons.Outlined.InsertChart,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        route = Screens.AddQuittung.name,
                        title = Screens.AddQuittung.name,
                        selectedIcon = Icons.Filled.Add,
                        unselectedIcon = Icons.Outlined.Add,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        route = Screens.Quittungen.name,
                        title = Screens.Quittungen.name,
                        selectedIcon = Icons.Filled.Receipt,
                        unselectedIcon = Icons.Outlined.Receipt,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        route = Screens.Profil.name,
                        title = Screens.Profil.name,
                        selectedIcon = Icons.Filled.Person,
                        unselectedIcon = Icons.Outlined.Person,
                        hasNews = false,
                    ),

                )

                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        CustomTopBar(navController = navController)
                    },
                    content = { paddingValues ->
                        MyNavHost(
                            navHostController = navController,
                            modifier = Modifier.padding(paddingValues)
                        )
                    },
                    bottomBar = {
                        BottomNavigation(
                            navController = navController,
                            items = navigationItems,
                        )
                    }
                )
            }
        }
    }
}





