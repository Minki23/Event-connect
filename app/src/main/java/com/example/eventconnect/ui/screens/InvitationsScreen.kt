package com.example.eventconnect.ui.screens

import LocalFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eventconnect.ui.data.FriendsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Screen dedicated to viewing and managing incoming friend invitations
 * @param onBack lambda to pop back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(onBack: () -> Unit) {
    val db = LocalFirestore.current
    val currentUser = Firebase.auth.currentUser
    val viewModel = remember { FriendsViewModel(db, currentUser?.uid ?: "", currentUser?.email ?: "") }

    val friendRequests by viewModel.friendRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchFriendRequests() }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> item { LoadingIndicator() }
                error.isNotEmpty() -> item { ErrorMessage(error) }
                friendRequests.isEmpty() -> item { Text("No invitations", color = Color.Gray) }
                else -> items(friendRequests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("From: ${request.senderEmail}")
                            Row {
                                Button(
                                    onClick = { viewModel.acceptRequest(request) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                                ) { Text("Accept") }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.declineRequest(request) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) { Text("Decline") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Shared components below...

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}
