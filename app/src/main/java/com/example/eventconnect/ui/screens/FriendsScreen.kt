package com.example.eventconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eventconnect.models.Friend
import com.example.eventconnect.ui.components.FriendCard
import com.example.eventconnect.ui.components.InvitationCard

@Composable
fun FriendsScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val allFriends = listOf(
        Friend("John Doe", "john.doe@example.com", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d", isInvitation = true),
        Friend("Jane Smith", "jane.smith@example.com", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80"),
        Friend("Alice Johnson", "alice.johnson@example.com", "https://images.unsplash.com/photo-1544005313-94ddf0286df2"),
        Friend("Michael Brown", "michael.brown@example.com", "https://images.unsplash.com/photo-1552058544-f2b08422138a"),
        Friend("Sarah Wilson", "sarah.wilson@example.com", "https://images.unsplash.com/photo-1541823709867-1b206113eafd", isInvitation = true),
        Friend("David Lee", "david.lee@example.com", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d")
    )

    val friends = allFriends.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    val onAccept: () -> Unit = {}
    val onDecline: () -> Unit = {}

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search friends...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFF2C2C2E),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                textColor = Color.White,
                placeholderColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            itemsIndexed(friends) { index, friend ->
                if (index == 0 && friend.isInvitation) {
                    InvitationCard(friend = friend, onAccept = onAccept, onDecline = onDecline)
                } else {
                    FriendCard(friend = friend)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
