// FriendsScreen.kt
package com.example.eventconnect.ui.screens

import LocalFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.eventconnect.ui.data.FriendsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen() {
    val db = LocalFirestore.current
    val currentUser = Firebase.auth.currentUser
    val viewModel = remember { FriendsViewModel(db, currentUser?.uid ?: "", currentUser?.email ?: "") }

    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
        viewModel.fetchFriendRequests()
    }

    val filteredFriends = if (searchQuery.isNotEmpty()) {
        friends.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    } else {
        friends
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Friends", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Filtered Friends" else "Your Friends",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (filteredFriends.isEmpty() && !isLoading) {
                    item { Text("No matching friends found", color = Color.Gray) }
                } else {
                    items(filteredFriends) { friend ->
                        FriendItem(name = friend.name, email = friend.email) {
                            viewModel.deleteFriend(friend.email)
                        }
                    }
                }
            }
        }

        if (showCreateUserDialog) {
            var userName by remember { mutableStateOf("") }
            var userEmail by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateUserDialog = false },
                title = { Text("Create New User") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createUser(userName, userEmail)
                        showCreateUserDialog = false
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateUserDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showAddFriendDialog) {
            var friendEmail by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddFriendDialog = false },
                title = { Text("Add Friend by Email") },
                text = {
                    OutlinedTextField(
                        value = friendEmail,
                        onValueChange = { friendEmail = it },
                        label = { Text("Friend's Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addFriendByEmail(friendEmail)
                        showAddFriendDialog = false
                    }) { Text("Send Request") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFriendDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search friends...") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun FriendItem(name: String, email: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                Text(email, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete", color = Color.White)
            }
        }
    }
}
