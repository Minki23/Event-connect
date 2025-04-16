package com.example.eventconnect.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.eventconnect.ui.data.UserViewModel
import com.example.eventconnect.ui.screens.LoginScreen
import com.example.eventconnect.ui.screens.MainScreen


@Composable
fun MainNavigation(innerPadding: PaddingValues, navController: NavHostController, onGoogleLogin: () -> Unit) {
    val viewModel = UserViewModel()
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onSignUpClick = { navController.navigate("register") },
                onLoginClick = { onGoogleLogin() }
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}
