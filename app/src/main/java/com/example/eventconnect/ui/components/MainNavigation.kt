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
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MainNavigation(innerPadding: PaddingValues, navController: NavHostController, onGoogleLogin: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "main" else "login"
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { onGoogleLogin() }
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}
