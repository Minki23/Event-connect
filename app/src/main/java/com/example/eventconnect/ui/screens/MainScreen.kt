package com.example.eventconnect.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventconnect.ui.components.BottomNavBar
import com.example.eventconnect.ui.components.BottomNavItem
import com.example.eventconnect.ui.components.SearchTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onGoogleLogin: () -> Unit) {
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )

                "edit_event/{eventId}" -> TopAppBar(
                    title = { Text("Edit Event") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )

                "login" -> null

                else -> SearchTopAppBar(
                    title = "",
                    onProfileClick = {
                        navController.navigate("profile")
                    }
                )
            }

        },
        bottomBar = {
            when (currentRoute) {
                "login" -> null
                else -> BottomNavBar(
                    navController = navController,
                    items = bottomNavItems
                )
            }
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
                AddEventScreen(onCreate = {navController.popBackStack()})
            }
            composable("profile") {
                UserScreen(onLogout = {navController.navigate("login")})
            }
            composable("friends"){
                FriendsScreen()
            }
            composable("invitations"){
                InvitationsScreen(onBack = {
                    navController.popBackStack()
                })
            }
            composable("login") {
                LoginScreen(onLoginClick = {onGoogleLogin()})
            }
        }
    }
}
