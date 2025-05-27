package com.example.eventconnect.ui.data

import androidx.lifecycle.ViewModel
import com.example.eventconnect.models.FriendRequest
import com.example.eventconnect.models.FriendSimple
import com.example.eventconnect.models.UserSimple
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
    private val _friends = MutableStateFlow<List<FriendSimple>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserSimple>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserSimple>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())

    init {
        fetchFriends()
        fetchFriendRequests()
    }

    fun fetchAllUsers() {
        _isLoading.value = true
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    doc.toObject<UserSimple>().copy(uid = doc.id)
                }.filter { user ->
                    user.uid != currentUserId
                }
                _allUsers.value = users
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load users: ${e.message}"
                _isLoading.value = false
            }
    }

    fun fetchFriendRequests() {
        _isLoading.value = true
        db.collection("friendRequests")
            .whereEqualTo("receiverEmail", currentUserEmail)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshots ->
                val requests = snapshots.mapNotNull { it.toObject<FriendRequest>() }
                _friendRequests.value = requests
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load invitations: ${e.message}"
                _isLoading.value = false
            }
    }

    fun fetchFriends() {
        _isLoading.value = true
        db.collection("users").document(currentUserId).collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                _friends.value = documents.mapNotNull { it.toObject<FriendSimple>() }
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
            _isLoading.value = false // Ensure isLoading is false for an empty query
            return
        }

        _isLoading.value = true

        val lowercaseQuery = query.lowercase()

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    val user = doc.toObject<UserSimple>().copy(uid = doc.id)
                    if ((user.displayName.lowercase().contains(lowercaseQuery) ||
                                user.email.lowercase().contains(lowercaseQuery)) &&
                        user.uid != currentUserId) {
                        user
                    } else {
                        null
                    }
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
        if (receiverEmail.isEmpty()) {
            _error.value = "Receiver email cannot be empty"
            return
        }

        db.collection("friendRequests")
            .whereEqualTo("senderEmail", currentUserEmail)
            .whereEqualTo("receiverEmail", receiverEmail)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { existingRequests ->
                if (!existingRequests.isEmpty) {
                    _error.value = "You already sent a request to this user"
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereEqualTo("email", receiverEmail)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        if (userDocs.isEmpty) {
                            _error.value = "User with email $receiverEmail not found"
                            return@addOnSuccessListener
                        }

                        val receiverId = userDocs.documents.first().id

                        val request = hashMapOf(
                            "senderId" to currentUserId,
                            "senderEmail" to currentUserEmail,
                            "receiverId" to receiverId,
                            "receiverEmail" to receiverEmail,
                            "status" to "pending",
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        val requestId = currentUserId + "_" + receiverId

                        db.collection("friendRequests").document(requestId)
                            .set(request)
                            .addOnSuccessListener {
                                _error.value = "Friend request sent successfully"
                            }
                            .addOnFailureListener { e ->
                                _error.value = "Failed to send request: ${e.message}"
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Failed to find user: ${e.message}"
                    }
            }
    }

    fun createUser(name: String, email: String) {
        if (name.isEmpty() || email.isEmpty()) {
            _error.value = "Please fill all fields"
            return
        }

        val newUser = hashMapOf(
            "displayName" to name,
            "email" to email,
            "photoUrl" to ""
        )

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _error.value = "User with this email already exists"
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .add(newUser)
                    .addOnSuccessListener {
                        _error.value = "User created successfully"
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error creating user: ${e.message}"
                    }
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

                db.collection("users").document(currentUserId).collection("friends")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { friendDocs ->
                        if (!friendDocs.isEmpty) {
                            _error.value = "This user is already your friend"
                            return@addOnSuccessListener
                        }

                        sendFriendRequest(email)
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Search failed: ${e.message}"
            }
    }

    fun addFriend(userId: String, email: String) {
        if(userId.isEmpty()){
            _error.value = "Id cannot be empty"
            return
        }
        if (email.isEmpty()) {
            _error.value = "Email cannot be empty"
            return
        }

        db.collection("users").document(currentUserId).collection("friends")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _error.value = "User is already your friend"
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        if (userDocs.isEmpty) {
                            _error.value = "User with this email not found"
                            return@addOnSuccessListener
                        }

                        val user = userDocs.first().toObject<UserSimple>()

                        db.collection("users").document(currentUserId).collection("friends")
                            .document(userId)
                            .set(mapOf(
                                "userId" to userId,
                                "name" to user.displayName,
                                "email" to user.email,
                                "imageUrl" to user.photoUrl
                            ))
                            .addOnSuccessListener {
                                fetchFriends()
                                _error.value = "Friend added successfully"
                            }
                            .addOnFailureListener { e ->
                                _error.value = "Failed to add friend: ${e.message}"
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Failed to find user with email $email: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to check existing friends: ${e.message}"
            }
    }

    fun deleteFriend(friendEmail: String) {
        val userDoc = db.collection("users").document(currentUserId)

        userDoc.collection("friends")
            .whereEqualTo("email", friendEmail)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (doc in documents) {
                    batch.delete(doc.reference)
                }

                db.collection("users")
                    .whereEqualTo("email", friendEmail)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        val otherUserId = userDocs.firstOrNull()?.id
                        if (otherUserId != null) {
                            db.collection("users").document(otherUserId)
                                .collection("friends")
                                .whereEqualTo("email", currentUserEmail)
                                .get()
                                .addOnSuccessListener { backDocs ->
                                    for (doc in backDocs) {
                                        batch.delete(doc.reference)
                                    }
                                    batch.commit().addOnSuccessListener {
                                        fetchFriends()
                                        _error.value = "Friend removed successfully"
                                    }
                                }
                        } else {
                            batch.commit().addOnSuccessListener {
                                fetchFriends()
                                _error.value = "Friend removed from your list"
                            }
                        }
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to remove friend: ${e.message}"
            }
    }
}