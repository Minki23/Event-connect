package com.example.eventconnect.ui.screens

import LocalFirestore
import LocalStorage
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
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventconnect.ui.data.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val db = LocalFirestore.current
    val storage = LocalStorage.current

    val viewModel: EventViewModel = viewModel()

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

    // Launchery do galerii i aparatu
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
                title = { Text("Choose Option") },
                text = {
                    Column {
                        TextButton(onClick = {
                            showDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) { Text("Choose from gallery") }
                        TextButton(onClick = {
                            showDialog = false
                            val photoFile = viewModel.createImageFile(context)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, context.packageName + ".provider", photoFile
                            )
                            currentPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }) { Text("Take a photo") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = colors.onPrimary)
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

        // TextFields
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
                Text("Date:",  color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
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
                    shape = RoundedCornerShape(8.dp)
                ) { Text(selectedDate, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Spacer(Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Time:",  color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
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
                    shape = RoundedCornerShape(8.dp)
                ) { Text(
                    selectedTime,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    // walidacja
                    if (eventName.isBlank()) {
                        Toast.makeText(context, "Please enter event name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (location.isBlank()) {
                        Toast.makeText(context, "Please enter location", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // przygotuj URI do uploadu
                    val uploadUri: Uri? = photoUri?.let { uri ->
                        if (uri.scheme == "content") {
                            viewModel.copyUriToFile(context, uri)?.let { FileUri ->
                                Uri.fromFile(FileUri)
                            }
                        } else {
                            uri
                        }
                    }

                    // wywołaj dodanie eventu
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
            ) {
                Text("Add Event", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}



