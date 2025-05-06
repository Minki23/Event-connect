package com.example.eventconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventconnect.ui.data.Event
import com.example.eventconnect.ui.data.SimpleUser
import com.example.eventconnect.ui.components.EventCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

enum class EventFilter {
    ALL,
    MY_EVENTS,
    PARTICIPATING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedFilter by remember { mutableStateOf(EventFilter.ALL) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch events from Firestore
    LaunchedEffect(selectedFilter) {
        isLoading = true

        try {
            val db = FirebaseFirestore.getInstance()
            val eventsCollection = db.collection("events")

            // Apply different queries based on selected filter
            val querySnapshot = when (selectedFilter) {
                EventFilter.ALL -> eventsCollection.get().await()
                EventFilter.MY_EVENTS -> eventsCollection.whereEqualTo("host", userId).get().await()
                // For participating events, we need to fetch all and filter in code since participants is a list of SimpleUser objects
                EventFilter.PARTICIPATING -> eventsCollection.get().await()
            }

            // Convert Firestore documents to Event objects
            val allEvents = querySnapshot.documents.mapNotNull { document ->
                try {
                    // Extract participants list from Firestore
                    val participantsData = document.get("participants") as? List<Map<String, Any>> ?: emptyList()
                    val participants = participantsData.map { participantMap ->
                        SimpleUser(
                            uid = participantMap["uid"] as? String,
                            displayName = participantMap["displayName"] as? String,
                            email = participantMap["email"] as? String,
                            photoUrl = participantMap["photoUrl"] as? String
                        )
                    }

                    Event(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        location = document.getString("location") ?: "",
                        description = document.getString("description") ?: "",
                        date = document.getString("date") ?: "",
                        time = document.getString("time") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        host = document.getString("host") ?: "",
                        participants = participants
                    )
                } catch (e: Exception) {
                    println("Error parsing event document: ${e.message}")
                    null
                }
            }

            // Apply filter for participating events if needed
            events = when (selectedFilter) {
                EventFilter.PARTICIPATING -> {
                    allEvents.filter { event ->
                        event.participants.any { it.uid == userId }
                    }
                }
                else -> allEvents
            }

        } catch (e: Exception) {
            // Handle errors
            println("Error fetching events: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter tabs at the top
        FilterTabs(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        // Events list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedFilter) {
                        EventFilter.ALL -> "No events available"
                        EventFilter.MY_EVENTS -> "You haven't created any events yet"
                        EventFilter.PARTICIPATING -> "You aren't participating in any events"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventItem(
                        event = event,
                        isUserEvent = event.host == userId,
                        isParticipating = event.participants.any { it.uid == userId },
                        onClick = {
                            if (event.host == userId) {
                                navController.navigate("edit_event/${event.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTabs(
    selectedFilter: EventFilter,
    onFilterSelected: (EventFilter) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedFilter.ordinal,
        edgePadding = 16.dp,
        divider = { Divider(thickness = 2.dp) }
    ) {
        FilterTab(
            title = "All Events",
            selected = selectedFilter == EventFilter.ALL,
            onClick = { onFilterSelected(EventFilter.ALL) }
        )

        FilterTab(
            title = "My Events",
            selected = selectedFilter == EventFilter.MY_EVENTS,
            onClick = { onFilterSelected(EventFilter.MY_EVENTS) }
        )

        FilterTab(
            title = "Attending",
            selected = selectedFilter == EventFilter.PARTICIPATING,
            onClick = { onFilterSelected(EventFilter.PARTICIPATING) }
        )
    }
}

@Composable
fun FilterTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

// Event Item component for display in the list
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(
    event: Event,
    isUserEvent: Boolean,
    isParticipating: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = "Event image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Event details
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = event.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${event.date} at ${event.time}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                // Location if available
                if (event.location.isNotEmpty()) {
                    Text(
                        text = event.location,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                // Participants count
                Text(
                    text = "${event.participants.size} attending",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            // Status badges
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isUserEvent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            "Host",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isParticipating) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            "Attending",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}