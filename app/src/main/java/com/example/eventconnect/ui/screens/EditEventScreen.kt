package com.example.eventconnect.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventconnect.R
import com.example.eventconnect.ui.data.EventViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Form state
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val calendar = remember { Calendar.getInstance() }

    // ViewModel state
    val event by viewModel.currentEvent
    val isLoading by viewModel.isLoadingEvent
    val isSaving by viewModel.isSaving

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> newImageUri = uri }


    // Load event once on enter
    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    // Populate form when event loads
    LaunchedEffect(event) {
        event?.let {
            name = it.name
            location = it.location
            description = it.description
            date = it.date
            time = it.time
            currentImageUrl = it.imageUrl
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveEvent(
                        event = event,
                        name = name,
                        location = location,
                        description = description,
                        date = date,
                        time = time,
                        imageUrl = event!!.imageUrl,
                        context = context,
                        navController = navController,
                        scope = scope,
                        selectedImageUri = newImageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isSaving && name.isNotBlank() && date.isNotBlank() && time.isNotBlank()
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Event Image area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        newImageUri != null -> AsyncImage(
                            model = newImageUri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        currentImageUrl.isNotEmpty() -> AsyncImage(
                            model = currentImageUrl,
                            contentDescription = "Event image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Icon(
                            Icons.Default.Edit,
                            contentDescription = "Add image",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Input fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            DatePickerDialog(
                                ContextThemeWrapper(context, R.style.CustomDatePickerDialog),
                                { _, y, m, d ->
                                    date = "%02d/%02d/%04d".format(d, m + 1, y)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            TimePickerDialog(
                                ContextThemeWrapper(context, R.style.CustomTimePickerDialog),
                                { _, h, min ->
                                    time = "%02d:%02d".format(h, min)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show() }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    trailingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
                val additionalPhotoUri = remember { mutableStateOf<Uri?>(null) }
                val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        additionalPhotoUri.value = uri
                        viewModel.uploadPhotoForEvent(eventId, uri, context) {
                            Toast.makeText(context, "Photo uploaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Text("Event Photos", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Existing photos
                    event?.photoUrls?.forEach { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Add button
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                            .clickable { galleryPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Add photo")
                    }
                }
            }
        }
    }
}
