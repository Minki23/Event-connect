package com.example.eventconnect.ui.screens

import LocalFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventconnect.ui.components.EventCard
import com.example.eventconnect.ui.data.Event
import com.example.eventconnect.ui.theme.blue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun HomeScreen() {
    val db = LocalFirestore.current
    val viewModel = remember { HomeViewModel(db) }
    val eventsState = viewModel.events.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val error = viewModel.error.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadEvents()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = blue
            )
        } else if (error.value.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading events",
                    color = Color.Red,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error.value,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        } else if (eventsState.value.isEmpty()) {
            Text(
                text = "No events found",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 18.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(eventsState.value) { event ->
                    EventCard(
                        title = event.name,
                        date = event.date,
                        imageUrl = event.imageUrl
                    )
                }
            }
        }
    }
}

class HomeViewModel(private val db: FirebaseFirestore) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    fun loadEvents() {
        _isLoading.value = true
        _error.value = ""

        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val eventsList = documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                }
                _events.value = eventsList
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
    }
}