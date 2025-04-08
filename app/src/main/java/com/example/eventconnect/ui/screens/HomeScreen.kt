package com.example.eventconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eventconnect.ui.components.EventCard
import com.example.eventconnect.ui.data.Event

@Composable
fun HomeScreen() {
    val events = listOf(
        Event("Art Expo", "January 15, 2024", null),
        Event("Joga retreat", "January 15, 2024", null),
        Event("Art Expo", "January 15, 2024", "https://images.unsplash.com/photo-1549576490-b0b4831ef60a"),
        Event("Art Expo", "January 15, 2024", "https://images.unsplash.com/photo-1509223197845-458d87318791")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        items(events) { event ->
            EventCard(
                title = event.title,
                date = event.date,
                imageUrl = event.imageUrl
            )
        }
    }
}
