package com.example.eventconnect.ui.screens

import EventViewModelFactory
import com.example.eventconnect.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedTextField
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventconnect.ui.data.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {},
    onCreate: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(auth, db, storage)
    )

    var eventName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("18/03/2025") }
    var selectedTime by remember { mutableStateOf("18:30") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }

    val colors = MaterialTheme.colorScheme

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = currentPhotoUri
        else currentPhotoUri = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = "Choose Option",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Choose from gallery",
                                color = colors.onBackground
                            )
                        }

                        TextButton(
                            onClick = {
                                showDialog = false
                                val photoFile = viewModel.createImageFile(context)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                currentPhotoUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Take a photo",
                                color = colors.onBackground
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDialog = false },
                    ) {
                        Text(
                            text = "Cancel",
                            color = colors.onBackground
                        )
                    }
                },
                modifier = Modifier.padding(16.dp)
            )

        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Upload Photo")
        }

        Spacer(Modifier.height(16.dp))

        photoUri?.let { uri ->
            Box(
                Modifier
                    .size(200.dp)
                    .border(2.dp,  color = MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = uri, contentDescription = "Selected event photo",
                    Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        OutlinedTextField(
            value = eventName, onValueChange = { eventName = it },
            label = { Text("Event Name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = location, onValueChange = { location = it },
            label = { Text("Location") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(), maxLines = 5
        )

        Spacer(Modifier.height(24.dp))

        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Date:",  color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Button(
                    onClick = {
                        DatePickerDialog(
                            ContextThemeWrapper(context, R.style.CustomDatePickerDialog),
                            { _, y, m, d ->
                                selectedDate = "%02d/%02d/%04d".format(d, m + 1, y)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("date_picker")
                ) { Text(selectedDate, color = Color.White) }
            }
            Spacer(Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Time:",  color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Button(
                    onClick = {
                        TimePickerDialog(
                            ContextThemeWrapper(context, R.style.CustomTimePickerDialog),
                            { _, h, min ->
                                selectedTime = "%02d:%02d".format(h, min)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("time_picker")
                ) { Text(
                    selectedTime,
                    color = Color.White
                )  }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = {
                    if (eventName.isBlank()) {
                        Toast.makeText(context, "Please enter event name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (location.isBlank()) {
                        Toast.makeText(context, "Please enter location", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val uploadUri: Uri? = photoUri?.let { uri ->
                        if (uri.scheme == "content") {
                            viewModel.copyUriToFile(context, uri)?.let { fileUri ->
                                Uri.fromFile(fileUri)
                            }
                        } else {
                            uri
                        }
                    }

                    isLoading = true
                    if (user != null) {
                        viewModel.addEvent(
                            name = eventName,
                            location = location,
                            description = description,
                            date = selectedDate,
                            time = selectedTime,
                            imageUri = uploadUri,
                            host = user,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                                eventName = ""
                                location = ""
                                description = ""
                                selectedDate = "18/03/2025"
                                selectedTime = "18:30"
                                photoUri = null
                                currentPhotoUri = null
                                onCreate()
                            },
                            onError = { errorMessage ->
                                isLoading = false
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(width = 120.dp, height = 48.dp)
                    .testTag("save_event_button")
            ) {
                Text("Add Event", color = Color.White)
            }
        }
    }
}




