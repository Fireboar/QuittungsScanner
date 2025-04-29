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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quittungsscanner.data.user.UserViewModel
import com.example.quittungsscanner.ui.bands.BandsViewModel
import com.example.quittungsscanner.ui.main.BottomNavigation
import com.example.quittungsscanner.ui.main.BottomNavigationItem
import com.example.quittungsscanner.ui.main.CustomTopBar
import com.example.quittungsscanner.ui.main.MyNavHost
import com.example.quittungsscanner.ui.main.Screens
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
                        hasNews = bandCount > 0,
                        badgeCount = bandCount
                    ),
                    BottomNavigationItem(
                        route = Screens.Components.name,
                        title = Screens.Components.name,
                        selectedIcon = Icons.Filled.Star,
                        unselectedIcon = Icons.Outlined.Star,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        route = Screens.Users.name,
                        title = Screens.Users.name,
                        selectedIcon = Icons.Filled.Face,
                        unselectedIcon = Icons.Outlined.Face,
                        hasNews = userCount > 0,
                        badgeCount = userCount
                    )
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





