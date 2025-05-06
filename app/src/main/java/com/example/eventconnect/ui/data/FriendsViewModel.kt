// FriendsViewModel.kt
package com.example.eventconnect.ui.data

import androidx.lifecycle.ViewModel
import com.example.eventconnect.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendsViewModel(
    private val db: FirebaseFirestore,
    private val currentUserId: String,
    private val currentUserEmail: String
) : ViewModel() {
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests = _friendRequests.asStateFlow()

    init {
        fetchFriends()
        fetchFriendRequests()
    }

    fun fetchFriendRequests() {
        _isLoading.value = true
        db.collection("friendRequests")
            .whereEqualTo("receiverEmail", currentUserEmail)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshots ->
                val requests = snapshots.mapNotNull { it.toObject<FriendRequest>()
                }
                _friendRequests.value = requests
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load invitations: ${e.message}"
                _isLoading.value = false
            }
    }

    fun acceptRequest(request: FriendRequest) {
        val senderFriends = db.collection("users").document(request.senderEmail).collection("friends")
        val receiverFriends = db.collection("users").document(currentUserEmail).collection("friends")

        senderFriends.document(currentUserId).set(
            mapOf("userId" to currentUserId, "name" to FirebaseAuth.getInstance().currentUser?.displayName, "email" to FirebaseAuth.getInstance().currentUser?.email)
        )
        receiverFriends.document(request.senderEmail).set(
            mapOf("userId" to request.senderEmail)
        )

        db.collection("friendRequests").document(request.senderEmail)
            .update("status", "accepted")

        fetchFriends()
        fetchFriendRequests()
    }

    fun declineRequest(request: FriendRequest) {
        db.collection("friendRequests").document(request.senderEmail)
            .update("status", "declined")
            .addOnSuccessListener {
                fetchFriendRequests()
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to decline: ${e.message}"
            }
    }

    fun fetchFriends() {
        _isLoading.value = true
        db.collection("users").document(currentUserId).collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                _friends.value = documents.mapNotNull { it.toObject<Friend>() }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load friends: ${e.message}"
                _isLoading.value = false
            }
    }

    fun searchUsers(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        _isLoading.value = true
        db.collection("users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    doc.toObject<User>().copy(id = doc.id)
                }.filter { user ->
                    user.id != currentUserId && !_friends.value.any { it.userId == user.id }
                }
                _searchResults.value = users
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
            }
    }

    fun sendFriendRequest(receiverEmail: String) {
        val request = hashMapOf(
            "senderId" to currentUserEmail,
            "receiverId" to receiverEmail,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("friendRequests").add(request)
            .addOnFailureListener { e ->
                _error.value = "Failed to send request: ${e.message}"
            }
    }
    fun createUser(name: String, email: String) {
        if (name.isEmpty() || email.isEmpty()) {
            _error.value = "Please fill all fields"
            return
        }

        val newUser = hashMapOf(
            "name" to name,
            "email" to email
        )

        db.collection("users")
            .add(newUser)
            .addOnSuccessListener {
                _error.value = "User created successfully"
            }
            .addOnFailureListener { e ->
                _error.value = "Error creating user: ${e.message}"
            }
    }

    fun addFriendByEmail(email: String) {
        if (email.isEmpty()) {
            _error.value = "Please enter an email"
            return
        }

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    _error.value = "User not found"
                    return@addOnSuccessListener
                }

                val user = documents.first().toObject(User::class.java)
                sendFriendRequest(user.email)
            }
            .addOnFailureListener { e ->
                _error.value = "Search failed: ${e.message}"
            }
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)

data class Friend(
    val userId: String = "",
    val name: String = "",
    val email: String = ""
)