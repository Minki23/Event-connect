package com.example.eventconnect.ui.screens

import LocalFirestore
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventconnect.ui.data.Event
import com.example.eventconnect.ui.data.SimpleUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: String
) {
    val db = LocalFirestore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Form state
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    // Fetch event data
    LaunchedEffect(eventId) {
        try {
            val documentSnapshot = db.collection("events").document(eventId).get().await()

            if (documentSnapshot.exists()) {
                // Extract participants list from Firestore
                val participantsData = documentSnapshot.get("participants") as? List<Map<String, Any>> ?: emptyList()
                val participants = participantsData.map { participantMap ->
                    SimpleUser(
                        uid = participantMap["uid"] as? String,
                        displayName = participantMap["displayName"] as? String,
                        email = participantMap["email"] as? String,
                        photoUrl = participantMap["photoUrl"] as? String
                    )
                }

                event = Event(
                    id = documentSnapshot.id,
                    name = documentSnapshot.getString("name") ?: "",
                    location = documentSnapshot.getString("location") ?: "",
                    description = documentSnapshot.getString("description") ?: "",
                    date = documentSnapshot.getString("date") ?: "",
                    time = documentSnapshot.getString("time") ?: "",
                    imageUrl = documentSnapshot.getString("imageUrl") ?: "",
                    host = documentSnapshot.getString("host") ?: "",
                    participants = participants
                )

                // Set form values
                name = event?.name ?: ""
                location = event?.location ?: ""
                description = event?.description ?: ""
                date = event?.date ?: ""
                time = event?.time ?: ""
                imageUrl = event?.imageUrl ?: ""
            }
        } catch (e: Exception) {
            // Handle error
            println("Error fetching event: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Save event function
    fun saveEvent() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        // Check if user is the host
        if (event?.host != userId) {
            // Show error message - only the host can edit
            Toast.makeText(context, "Only the event host can edit this event", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isSaving = true

            try {
                val eventRef = db.collection("events").document(eventId)

                // Handle image upload if a new image was selected
                var finalImageUrl = imageUrl
                selectedImageUri?.let { uri ->
                    val storageRef = FirebaseStorage.getInstance().reference.child("event_images/${UUID.randomUUID()}")
                    val uploadTask = storageRef.putFile(uri).await()
                    finalImageUrl = storageRef.downloadUrl.await().toString()
                }

                // Update event data
                val eventData = hashMapOf(
                    "name" to name,
                    "location" to location,
                    "description" to description,
                    "date" to date,
                    "time" to time,
                    "imageUrl" to finalImageUrl
                )

                eventRef.update(eventData as Map<String, Any>).await()

                // Navigate back
                navController.popBackStack()
            } catch (e: Exception) {
                // Handle error
                println("Error updating event: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        TextButton(
                            onClick = { saveEvent() },
                            enabled = !isSaving && name.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()
                        ) {
                            Text("SAVE")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected event image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Event image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Add image",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Event Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Event Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                        }
                    },
                    singleLine = true,
                    readOnly = true
                )

                // Event Time
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select time")
                        }
                    },
                    singleLine = true,
                    readOnly = true
                )

                // Event Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { /* Open location picker if needed */ }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Set location")
                        }
                    },
                    singleLine = true
                )

                // Event Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )

                // Participants section
                Text(
                    text = "Participants (${event?.participants?.size ?: 0})",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Note: Participant list cannot be edited directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Save button for mobile-friendly design
                Button(
                    onClick = { saveEvent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = !isSaving && name.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                date = selectedDate
                showDatePicker = false
            }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                time = selectedTime
                showTimePicker = false
            }
        )
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    // Implementation of a custom date picker or use Material3 DatePicker when available
    // For now, we'll use a simple dialog

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = {
            // This is a placeholder for a date picker
            // In a real app, you would implement a proper date picker here
            Text("In a real app, implement a proper date picker here")
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected("May 15, 2025") }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    // Implementation of a custom time picker or use Material3 TimePicker when available
    // For now, we'll use a simple dialog

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = {
            // This is a placeholder for a time picker
            // In a real app, you would implement a proper time picker here
            Text("In a real app, implement a proper time picker here")
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected("7:00 PM") }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}