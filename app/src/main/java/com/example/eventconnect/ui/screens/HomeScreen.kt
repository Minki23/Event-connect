package com.example.eventconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.eventconnect.ui.data.Event
import com.example.eventconnect.ui.data.EventFilter
import com.example.eventconnect.ui.data.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val events by viewModel.events
    val isLoading by viewModel.isLoadingEvents
    val selectedFilter by viewModel.selectedFilter
    val currentUserUid = viewModel.currentUserUid
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchEvents()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter
        // tabs
        FilterTabs(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setFilter(it) }
        )

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            events.isEmpty() -> {
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
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(events) { event ->
                        val isUserEvent = event.host == currentUserUid
                        val isParticipating = event.participants.any { it.userId == currentUserUid }
                        EventItem(
                            event = event,
                            isUserEvent = isUserEvent,
                            isParticipating = isParticipating,
                            onClick = {
                                if (isUserEvent || isParticipating) {
                                    navController.navigate("edit_event/${event.id}")
                                }
                            }
                        )
                    }
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
    TabRow(
        selectedTabIndex = selectedFilter.ordinal,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        FilterTab(
            title = "All Events",
            selected = selectedFilter == EventFilter.ALL,
            onClick = { onFilterSelected(EventFilter.ALL) },
            modifier = Modifier.fillMaxWidth()
        )

        FilterTab(
            title = "My Events",
            selected = selectedFilter == EventFilter.MY_EVENTS,
            onClick = { onFilterSelected(EventFilter.MY_EVENTS) },
            modifier = Modifier.fillMaxWidth()
        )

        FilterTab(
            title = "Attending",
            selected = selectedFilter == EventFilter.PARTICIPATING,
            onClick = { onFilterSelected(EventFilter.PARTICIPATING) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FilterTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${event.date} at ${event.time}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                if (event.location.isNotEmpty()) {
                    Text(
                        text = event.location,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${event.participants.size} attending",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isUserEvent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
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
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            "Attending",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}