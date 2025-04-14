package com.example.eventconnect.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
import com.example.eventconnect.ui.components.BottomNavBar
import com.example.eventconnect.ui.components.BottomNavItem
import com.example.eventconnect.ui.components.SearchTopAppBar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.eventconnect.ui.theme.EventConnectTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("add_event", "Add Event", Icons.Default.Add),
        BottomNavItem("friends", "Friends", Icons.Default.People)
    )

    Scaffold(
        topBar = {
            SearchTopAppBar(
                navController = navController,
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("add_event") {
                AddEventScreen()
            }
            composable("friends") {
                FriendsScreen()
            }
            composable("profile") {
                UserScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Text("Profile Screen")
}
