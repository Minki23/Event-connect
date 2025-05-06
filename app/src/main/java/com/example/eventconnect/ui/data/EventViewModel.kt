package com.example.eventconnect.ui.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*

class EventViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // expose current user ID
    val currentUserUid: String
        get() = auth.currentUser?.uid.orEmpty()

    // -- Events list state --
    private val _selectedFilter = mutableStateOf(EventFilter.ALL)
    val selectedFilter: State<EventFilter> = _selectedFilter

    private val _events = mutableStateOf<List<Event>>(emptyList())
    val events: State<List<Event>> = _events

    private val _isLoadingEvents = mutableStateOf(false)
    val isLoadingEvents: State<Boolean> = _isLoadingEvents

    // -- Single event (edit) state --
    private val _currentEvent = mutableStateOf<Event?>(null)
    val currentEvent: State<Event?> = _currentEvent

    private val _isLoadingEvent = mutableStateOf(false)
    val isLoadingEvent: State<Boolean> = _isLoadingEvent

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    init {
        fetchEvents()
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
                                uid = map["uid"] as? String,
                                displayName = map["displayName"] as? String,
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
                    all.filter { e -> e.participants.any { it.uid == currentUserUid } }
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
                if (doc.exists()) {
                    val participantsData = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                    val participants = participantsData.map { map ->
                        SimpleUser(
                            uid = map["uid"] as? String,
                            displayName = map["displayName"] as? String,
                            email = map["email"] as? String,
                            photoUrl = map["photoUrl"] as? String
                        )
                    }
                    _currentEvent.value = Event(
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
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        // Check if user is the host
        if (event?.host != userId) {
            // Show error message - only the host can edit
            Toast.makeText(context, "Only the event host can edit this event", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            _isSaving.value = true

            try {
                val eventRef = db.collection("events").document(event.id)

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
            // upload pliku
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
            // bez zdjęcia
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
        onError: (String) -> Unit
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
    // kopiuj zawartość content:// Uri do pliku w cache
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

    // tworzy pusty plik do aparatu
    fun createImageFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }
}