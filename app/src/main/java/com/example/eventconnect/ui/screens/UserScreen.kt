package com.example.eventconnect.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventconnect.ui.data.UserViewModel

@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.fetchUser(1)
    }

    if (viewModel.user == null) {
        Text("Loading...")
    } else {
        Text("User: ${viewModel.user?.username}")
    }
}
