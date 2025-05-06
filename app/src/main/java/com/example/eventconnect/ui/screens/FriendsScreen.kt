// FriendsScreen.kt
package com.example.eventconnect.ui.screens

import LocalFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.eventconnect.ui.data.FriendsViewModel
import com.example.eventconnect.ui.data.User
import com.example.eventconnect.ui.theme.blue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navigateToInvitations: () -> Unit) {
    val db = LocalFirestore.current
    val currentUser = Firebase.auth.currentUser
    val viewModel = remember { FriendsViewModel(db, currentUser?.uid ?: "", currentUser?.email ?: "") }

    val friends by viewModel.friends.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
        viewModel.fetchFriendRequests()
    }
    LaunchedEffect(searchQuery) {
        viewModel.searchUsers(searchQuery)
    }

    Scaffold{ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(text = "Friends", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showCreateUserDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) { Text("Create User") }

                    Button(
                        onClick = { showAddFriendDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) { Text("Add Friend") }
                }
            }
            when {
                isLoading -> item { LoadingIndicator() }
                error.isNotEmpty() -> item { ErrorMessage(error) }
                searchQuery.isNotEmpty() -> {
                    if (searchResults.isEmpty()) {
                        item { Text("No users found", color = Color.White) }
                    } else {
                        items(searchResults) { user ->
                            UserResultItem(user = user, onAdd = { viewModel.sendFriendRequest(user.id) })
                        }
                    }
                }
                else -> {
                    item { Text("Your Friends", style = MaterialTheme.typography.titleMedium) }
                    if (friends.isEmpty()) {
                        item { Text("No friends yet", color = Color.White) }
                    } else {
                        items(friends) { friend ->
                            FriendItem(friend.name)
                        }
                    }
                }
            }
        }

        // Create User Dialog
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

        // Add Friend Dialog
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
private fun UserResultItem(user: User, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onAdd) { Text("Add Friend") }
        }
    }
}

@Composable
private fun FriendItem(name: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

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