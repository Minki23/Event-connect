package com.example.eventconnect.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventconnect.ui.screens.LoginScreen
import com.example.eventconnect.ui.screens.MainScreen
import com.example.eventconnect.ui.screens.RegistrationScreen
import com.example.eventconnect.ui.screens.WelcomeScreen

@Composable
fun MainNavigation(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                innerPadding = innerPadding,
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                onSignUpClick = { navController.navigate("register") },
                onLoginClick = { navController.navigate("main") }
            )
        }
        composable("register") {
            RegistrationScreen(
                onSignUpClick = { navController.navigate("main") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}
