package com.example.eventconnect.ui.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class EventViewModel(private val auth: FirebaseAuth,
                     private val db: FirebaseFirestore,
                     private val storage: FirebaseStorage
) : ViewModel() {
    val currentUserUid: String
        get() = auth.currentUser?.uid.orEmpty()
    val currentUser = auth.currentUser

    private val _selectedFilter = mutableStateOf(EventFilter.ALL)
    val selectedFilter: State<EventFilter> = _selectedFilter

    private val _events = mutableStateOf<List<Event>>(emptyList())
    val events: State<List<Event>> = _events

    private val _isLoadingEvents = mutableStateOf(false)
    val isLoadingEvents: State<Boolean> = _isLoadingEvents

    private val _currentEvent = mutableStateOf<Event?>(null)
    val currentEvent: State<Event?> = _currentEvent

    private val _isLoadingEvent = mutableStateOf(false)
    val isLoadingEvent: State<Boolean> = _isLoadingEvent

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    val _eventParticipants = MutableStateFlow<List<SimpleUser>>(emptyList())

    val _userFriends = MutableStateFlow<List<SimpleUser>>(emptyList())
    val friends = _userFriends.asStateFlow()

    private val _isUploadingPhoto = mutableStateOf(false)
    val isUploadingPhoto: State<Boolean> = _isUploadingPhoto


    init {
        fetchEvents()
        loadUserFriends(currentUserUid)
    }

    fun setFilter(filter: EventFilter) {
        _selectedFilter.value = filter
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            _isLoadingEvents.value = true
            try {
                val snapshot = when (_selectedFilter.value) {
                    EventFilter.ALL -> db.collection("events").get().await()
                    EventFilter.MY_EVENTS -> db.collection("events").whereEqualTo("host", currentUserUid).get().await()
                    EventFilter.PARTICIPATING -> db.collection("events").get().await()
                }
                val all = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val participantsData = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                        val participants = participantsData.map { map ->
                            SimpleUser(
                                userId = map["userId"] as? String,
                                name = map["name"] as? String,
                                email = map["email"] as? String,
                                photoUrl = map["photoUrl"] as? String
                            )
                        }
                        Event(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            location = doc.getString("location") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            host = doc.getString("host") ?: "",
                            participants = participants
                        )
                    }.getOrNull()
                }
                _events.value = if (_selectedFilter.value == EventFilter.PARTICIPATING) {
                    all.filter { e -> e.participants.any { it.userId == currentUserUid } }
                } else all
            } catch (_: Exception) {
            } finally {
                _isLoadingEvents.value = false
            }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _isLoadingEvent.value = true
            try {
                val doc = db.collection("events").document(eventId).get().await()
                val participantsData = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                val participantIds = participantsData.mapNotNull { it["userId"] as? String }
                loadEventParticipants(participantIds)
                if (doc.exists()) {
                    val participantsData = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                    val participants = participantsData.map { map ->
                        SimpleUser(
                            userId = map["userId"] as? String,
                            name = map["name"] as? String,
                            email = map["email"] as? String,
                            photoUrl = map["photoUrl"] as? String
                        )
                    }
                    val photoUrls = doc.get("photoUrls") as? List<String> ?: emptyList()
                    _currentEvent.value = Event(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = doc.getString("location") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        host = doc.getString("host") ?: "",
                        participants = participants,
                        photoUrls = photoUrls
                    )
                }
            } catch (_: Exception) {
            } finally {
                _isLoadingEvent.value = false
            }
        }
    }

    fun saveEvent(
        event: Event?,
        name: String,
        location: String,
        description: String,
        date: String,
        time: String,
        imageUrl: String,
        context: Context,
        navController: NavController,
        scope: CoroutineScope,
        selectedImageUri: Uri?,
        participants: List<SimpleUser>
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        if (event?.host != userId) {
            Toast.makeText(context, "Only the event host can edit this event", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            _isSaving.value = true

            try {
                val eventRef = db.collection("events").document(event.id)

                var finalImageUrl = imageUrl
                selectedImageUri?.let { uri ->
                    val storageRef = FirebaseStorage.getInstance().reference.child("event_images/${UUID.randomUUID()}")
                    storageRef.putFile(uri).await()
                    finalImageUrl = storageRef.downloadUrl.await().toString()
                }
                val participantsMap = participants.map {
                    mapOf(
                        "userId" to it.userId,
                        "name" to it.name,
                        "email" to it.email,
                        "photoUrl" to it.photoUrl
                    )
                }

                val eventData = hashMapOf(
                    "name" to name,
                    "location" to location,
                    "description" to description,
                    "date" to date,
                    "time" to time,
                    "imageUrl" to finalImageUrl,
                    "participants" to participantsMap
                )

                eventRef.update(eventData as Map<String, Any>).await()

                navController.popBackStack()
            } catch (e: Exception) {
                println("Error updating event: ${e.message}")
            } finally {
                _isSaving.value = false
            }
        }
    }
    fun addEvent(
        name: String,
        location: String,
        description: String,
        date: String,
        time: String,
        imageUri: Uri?,
        host: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventId = UUID.randomUUID().toString()
        if (imageUri != null) {
            val storageRef = storage.reference.child("event_images/$eventId")
            storageRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    storageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveEventToFirestore(
                            eventId, name, location, description, date, time,
                            task.result.toString(), host, onSuccess, onError
                        )
                    } else {
                        onError("Failed to upload image: ${task.exception?.message}")
                    }
                }
        } else {
            saveEventToFirestore(eventId, name, location, description, date, time, "", host, onSuccess, onError)
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
        host: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val event = Event(eventId, name, location, description, date, time, imageUrl, host.uid, listOf(
            SimpleUser(host.uid, host.displayName, host.email, host.photoUrl.toString())
        ))
        db.collection("events")
            .document(eventId)
            .set(event)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError("Error saving event: ${e.message}")
            }
    }

    fun copyUriToFile(context: Context, uri: Uri): File? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createImageFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    fun uploadPhotoForEvent(eventId: String, uri: Uri, context: Context, onSuccess: () -> Unit) {
        _isUploadingPhoto.value = true
        viewModelScope.launch {
            try {
                val fileName = UUID.randomUUID().toString()
                val photoRef = storage.reference.child("event_photos/$eventId/$fileName.jpg")
                photoRef.putFile(uri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()

                val eventDoc = db.collection("events").document(eventId)
                eventDoc.update("photoUrls", FieldValue.arrayUnion(downloadUrl)).await()

                _currentEvent.value?.let {
                    val updatedEvent = it.copy(
                        photoUrls = it.photoUrls + downloadUrl
                    )
                    _currentEvent.value = updatedEvent
                }

                _isUploadingPhoto.value = false
                onSuccess()
            } catch (e: Exception) {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                _isUploadingPhoto.value = false
            }
        }
    }

    fun loadUserFriends(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { friendDocs ->
                val friendIds = friendDocs.mapNotNull { it.getString("userId") }
                if (friendIds.isEmpty()) {
                    _userFriends.value = emptyList()
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereIn("uid", friendIds)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        val friends = userDocs.map {
                            SimpleUser(
                                userId = it.getString("uid"),
                                name = it.getString("displayName"),
                                email = it.getString("email"),
                                photoUrl = it.getString("photoUrl")
                            )
                        }
                        _userFriends.value = friends
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Failed to load user profiles", it)
                    }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to load friend references", it)
            }
    }

    fun loadEventParticipants(participantIds: List<String>) {
        if (participantIds.isEmpty()) {
            _eventParticipants.value = emptyList()
            return
        }

        db.collection("users")
            .whereIn("uid", participantIds)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull {
                    SimpleUser(
                        userId = it.getString("uid"),
                        name = it.getString("displayName"),
                        email = it.getString("email"),
                        photoUrl = it.getString("photoUrl")
                    )
                }
                _eventParticipants.value = users
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to load event participants", it)
            }
    }

    fun removePhotoFromEvent(eventId: String, photoUrl: String) {
        _isUploadingPhoto.value = true
        viewModelScope.launch {
            try {
                db.collection("events")
                    .document(eventId)
                    .update("photoUrls", FieldValue.arrayRemove(photoUrl))
                    .await()

                _currentEvent.value = _currentEvent.value?.copy(
                    photoUrls = _currentEvent.value!!.photoUrls.filter { it != photoUrl }
                )

            } catch (e: Exception) {
                Log.e("EventViewModel", "Error removing photo", e)
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }
}