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
import androidx.compose.ui.unit.sp
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
    val colors = MaterialTheme.colorScheme
    val db = LocalFirestore.current
    val currentUser = Firebase.auth.currentUser
    val viewModel = remember { FriendsViewModel(db, currentUser?.uid ?: "", currentUser?.email ?: "") }

    val friendRequests by viewModel.friendRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchFriendRequests() }

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> item { LoadingIndicator() }
                error.isNotEmpty() -> item { ErrorMessage(error) }
                friendRequests.isEmpty() -> item {
                    Text(
                        "No invitations",
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> items(friendRequests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "From: ${request.senderEmail}",
                                color = colors.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Row {
                                Button(
                                    onClick = { viewModel.acceptRequest(request) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.onPrimary
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Accept", fontSize = 14.sp)
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.declineRequest(request) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.errorContainer,
                                        contentColor = colors.onErrorContainer
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Decline", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(16.dp)
    )
}
