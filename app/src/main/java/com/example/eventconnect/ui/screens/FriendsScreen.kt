package com.example.eventconnect.ui.screens

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FriendsScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Search for new friends",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // List of friends
        repeat(4) {
            FriendItem(
                name = "Jane Smith amalgamation",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        UserWithWebsite(
            name = "Jane Smith",
            website = "www.johnson.com",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        UserWithWebsite(
            name = "Jane Smith amalgamation",
            website = "www.johnson.com",
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun FriendItem(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        color = Color.White,
        style = MaterialTheme.typography.body1,
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
    )
}

@Composable
private fun UserWithWebsite(
    name: String,
    website: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
    ) {
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = website,
            color = Color.White,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
        )
    }
}