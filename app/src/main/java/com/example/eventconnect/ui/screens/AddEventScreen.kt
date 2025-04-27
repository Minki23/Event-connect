package com.example.eventconnect.ui.screens

import LocalFirestore
import LocalStorage
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.eventconnect.ui.data.Event
import com.example.eventconnect.ui.theme.blue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*


@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {}
) {
    val db = LocalFirestore.current
    val storage = LocalStorage.current

    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(db, storage)
    )
    var eventName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Event",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected image",
                modifier = Modifier
                    .padding(16.dp)
                    .height(150.dp)
            )
        }
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Photo", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5  // Allow multiple lines for description
        )

        Spacer(modifier = Modifier.height(24.dp))

        context.setTheme(android.R.style.Theme_DeviceDefault)
        val calendar = remember { Calendar.getInstance() }

        var selectedDate by remember { mutableStateOf("18/03/2025") }
        var selectedTime by remember { mutableStateOf("18:30") }
        Row {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Date:", color = Color.White, fontSize = 16.sp)

                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val formattedDate =
                                    String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                                selectedDate = formattedDate
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = blue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(selectedDate, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Time:", color = Color.White, fontSize = 16.sp)

                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                                selectedTime = formattedTime
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = blue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(selectedTime, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = blue)
        } else {
            Button(
                onClick = {
                    // Validate inputs
                    if (eventName.isBlank()) {
                        Toast.makeText(context, "Please enter event name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (location.isBlank()) {
                        Toast.makeText(context, "Please enter location", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Save event to Firebase
                    isLoading = true
                    viewModel.addEvent(
                        name = eventName,
                        location = location,
                        description = description,
                        date = selectedDate,
                        time = selectedTime,
                        imageUri = selectedImageUri,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onError = { errorMessage ->
                            isLoading = false
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = blue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp)
            ) {
                Text(text = "Add Event", color = Color.White)
            }
        }
    }
}

class EventViewModel(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {


    fun addEvent(
        name: String,
        location: String,
        description: String,
        date: String,
        time: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventId = UUID.randomUUID().toString()

        if (imageUri != null) {
            // Upload image first
            val storageRef = storage.reference.child("event_images/$eventId")
            storageRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val imageUrl = task.result.toString()
                        saveEventToFirestore(eventId, name, location, description, date, time, imageUrl, onSuccess, onError)
                    } else {
                        onError("Failed to upload image: ${task.exception?.message}")
                    }
                }
        } else {
            // Save event without image
            saveEventToFirestore(eventId, name, location, description, date, time, "", onSuccess, onError)
        }
    }

    private fun saveEventToFirestore(
        eventId: String,
        name: String,
        location: String,
        description: String,
        date: String,
        time: String,
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val event = Event(
            id = eventId,
            name = name,
            location = location,
            description = description,
            date = date,
            time = time,
            imageUrl = imageUrl
        )

        db.collection("events")
            .document(eventId)
            .set(event)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Error saving event: ${e.message}")
            }
    }
}

class EventViewModelFactory(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(db, storage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}