package com.example.eventconnect.ui.screens

import EventViewModelFactory
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventconnect.R
import com.example.eventconnect.ui.data.EventViewModel
import com.example.eventconnect.ui.data.SimpleUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: String
) {
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(auth, db, storage)
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser = SimpleUser(
        userId = viewModel.currentUser?.uid ?: "",
        name = viewModel.currentUser?.displayName ?: "",
        email = viewModel.currentUser?.email ?: "",
        photoUrl = viewModel.currentUser?.photoUrl.toString()
    )

    val selectedParticipants = remember { mutableStateListOf<SimpleUser>() }

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val friends by viewModel.friends.collectAsState()
    val calendar = remember { Calendar.getInstance() }
    val loading = remember { mutableStateOf(false) }
    val isUploadingPhoto = remember { mutableStateOf(false) }

    val event by viewModel.currentEvent
    val isLoading by viewModel.isLoadingEvent
    val isSaving by viewModel.isSaving

    val isHost = event?.host == currentUser.userId

    var isPhotoViewerOpen by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadUserFriends(Firebase.auth.currentUser?.uid ?: "")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        newImageUri = uri
        if (uri != null) {
            loading.value = true
            viewModel.uploadPhotoForEvent(eventId, uri, context) {
                Toast.makeText(context, "Photo uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }


    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    LaunchedEffect(event) {
        event?.let {
            name = it.name
            location = it.location
            description = it.description
            date = it.date
            time = it.time
            currentImageUrl = it.imageUrl

            selectedParticipants.clear()
            val eventUsers = it.participants.map { simple ->
                SimpleUser(
                    userId = simple.userId.orEmpty(),
                    name = simple.name.orEmpty(),
                    email = simple.email.orEmpty(),
                    photoUrl = simple.photoUrl.orEmpty()
                )
            }

            selectedParticipants.addAll(eventUsers.filter { newUser ->
                selectedParticipants.none { existing -> existing.userId == newUser.userId }
            })

        }
    }


    Scaffold(
        bottomBar = {
            if (isHost) {
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
                            selectedImageUri = newImageUri,
                            participants = selectedParticipants
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !isSaving && !viewModel.isUploadingPhoto.value && name.isNotBlank() && date.isNotBlank() && time.isNotBlank()
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save Changes",
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
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
                }

                if (isHost) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Event Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    DatePickerDialog(
                                        ContextThemeWrapper(context, R.style.CustomDatePickerDialog),
                                        { _, y, m, d -> date = "%02d/%02d/%04d".format(d, m + 1, y) },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    TimePickerDialog(
                                        ContextThemeWrapper(context, R.style.CustomTimePickerDialog),
                                        { _, h, min -> time = "%02d:%02d".format(h, min) },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Select time")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
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
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 5
                        )
                    }

                } else {
                    item {
                        Text("Event Name: $name", style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text("Date: $date", style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text("Time: $time", style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text("Location: $location", style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text("Description:", style = MaterialTheme.typography.bodyLarge)
                        Text(description, style = MaterialTheme.typography.bodyMedium)
                    }
                    item {
                        Text(
                            "You can only add photos to this event.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                item {
                    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                        if (uris.isNotEmpty()) {
                            isUploadingPhoto.value = true
                            loading.value = true
                            uris.forEach { uri ->
                                viewModel.uploadPhotoForEvent(eventId, uri, context) {
                                    Toast.makeText(context, "Photo uploaded", Toast.LENGTH_SHORT).show()
                                }
                            }
                            loading.value = false
                            isUploadingPhoto.value = false
                        }
                    }


                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Event Photos", style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            event?.photoUrls?.forEachIndexed { index, photoUrl ->
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedPhotoIndex = index
                                            isPhotoViewerOpen = true
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }

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

                item {
                    Text("Participants", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedParticipants.forEach { participant ->
                            AssistChip(
                                onClick = {
                                    if (isHost && participant.userId != currentUser.userId) {
                                        selectedParticipants.removeAll { it.userId == participant.userId }
                                    }
                                },
                                enabled = isHost && participant.userId != currentUser.userId,
                                label = { Text(participant.name.toString()) }
                            )
                        }
                    }
                }

                if (isHost) {
                    item {
                        Text("Invite Friends", style = MaterialTheme.typography.titleMedium)
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            friends
                                .filter { friend ->
                                    selectedParticipants.none { it.userId == friend.userId }
                                }
                                .forEach { friend ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            selectedParticipants.add(friend)
                                        },
                                        label = { Text(friend.name.toString()) }
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
    if (isPhotoViewerOpen && event != null) {
        val photoUrls = event!!.photoUrls
        val pagerState = rememberPagerState(
            initialPage = selectedPhotoIndex,
            pageCount = { photoUrls.size }
        )
        val context = LocalContext.current

        LaunchedEffect(selectedPhotoIndex) {
            pagerState.scrollToPage(selectedPhotoIndex)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = photoUrls[page],
                    contentDescription = "Photo $page",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            IconButton(
                onClick = { isPhotoViewerOpen = false },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close viewer",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = {
                        val url = photoUrls[pagerState.currentPage]
                        val request = DownloadManager.Request(url.toUri()).apply {
                            setTitle("Downloading image")
                            setDescription("Saving image from EventConnect")
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationInExternalPublicDir(
                                android.os.Environment.DIRECTORY_PICTURES,
                                "EventConnect_${System.currentTimeMillis()}.jpg"
                            )
                            setAllowedOverMetered(true)
                            setAllowedOverRoaming(true)
                        }

                        val downloadManager =
                            context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)

                        Toast.makeText(context, "Downloading image...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
                if(event!!.host == Firebase.auth.currentUser?.uid) {
                    IconButton(
                        onClick = {
                            val photoToRemove = photoUrls[pagerState.currentPage]
                            viewModel.removePhotoFromEvent(eventId, photoToRemove)
                            isPhotoViewerOpen =
                                false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Photo",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}