package com.example.eventconnect.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.navigation.NavType
import androidx.navigation.navArgument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("add_event", "Add Event", Icons.Default.Add),
        BottomNavItem("friends", "Friends", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            when (currentRoute) {
                "home" -> SearchTopAppBar(
                    title = "Events",
                    onProfileClick = { navController.navigate("profile") }
                )

                "friends" -> TopAppBar(
                    title = { Text(
                        text = "Friends",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ) },
                    actions = {
                            IconButton(onClick = {navController.navigate("invitations")}) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Invitations")
                        }
                    }
                )

                "add_event" -> SearchTopAppBar(
                    title = "Create Event",
                    onProfileClick = {
                        navController.navigate("profile")
                    }
                )

                "invitations" -> TopAppBar(
                    title = {
                        "Invitations" },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )

                "edit_event/{eventId}" -> TopAppBar(
                    title = { Text("Edit Event") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
                else -> SearchTopAppBar(
                    title = "",
                    onProfileClick = {
                        navController.navigate("profile")
                    }
                )
            }
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
                HomeScreen(navController = navController)
            }
            composable(
                route = "edit_event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EditEventScreen(navController = navController, eventId = eventId)
            }
            composable("add_event") {
                AddEventScreen()
            }
            composable("profile") {
                UserScreen(onLogout = {navController.navigate("login")})
            }
            composable("friends"){
                FriendsScreen(navigateToInvitations = {
                    navController.navigate("invitations")
                })
            }
            composable("invitations"){
                InvitationsScreen(onBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}
