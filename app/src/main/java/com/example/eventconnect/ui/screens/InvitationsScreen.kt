package com.example.eventconnect.ui.screens

import LocalFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventconnect.ui.data.FriendsViewModel
import com.example.eventconnect.ui.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Screen for finding and sending friend invitations to users
 * @param onBack lambda to pop back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val db = LocalFirestore.current
    val currentUser = Firebase.auth.currentUser
    val viewModel = remember { FriendsViewModel(db, currentUser?.uid ?: "", currentUser?.email ?: "") }

    val searchResults by viewModel.searchResults.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Fetch all users when the screen is first shown
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {
                isLoading -> { LoadingIndicator() }
                error.isNotEmpty() -> { ErrorMessage(error) }
                searchQuery.isNotEmpty() && searchResults.isEmpty() -> {
                    Text(
                        "No users found with that name",
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    // Display header text based on whether we're showing search results or all users
                    Text(
                        text = if (searchQuery.isEmpty()) "All Users" else "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Show either search results or all users
                    val usersToDisplay = if (searchQuery.isEmpty()) allUsers else searchResults

                    if (usersToDisplay.isEmpty() && !isLoading) {
                        Text(
                            "No users found",
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(usersToDisplay) { user ->
                                UserCard(
                                    user = user,
                                    alreadyFriend = friends.any { it.userId == user.uid },
                                    onSendRequest = {
                                        viewModel.addFriend(currentUser?.uid ?: "", user.email)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    alreadyFriend: Boolean,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            Column {
                Text(
                    text = user.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!alreadyFriend) {
                Button(
                    onClick = onSendRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Add Friend", fontSize = 14.sp)
                }
            } else {
                Text(
                    "Already Friends",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
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