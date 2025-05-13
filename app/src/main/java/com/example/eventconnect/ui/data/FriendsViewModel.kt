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

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

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

    fun fetchAllUsers() {
        _isLoading.value = true
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    doc.toObject<User>().copy(uid = doc.id)
                }.filter { user ->
                    // Filter out the current user from the list
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
                    doc.toObject<User>().copy(uid = doc.id)
                }.filter { user ->
                    user.uid != currentUserId
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
            "senderId" to currentUserId,
            "senderEmail" to currentUserEmail,
            "receiverEmail" to receiverEmail,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Show a success message after sending the request
        db.collection("friendRequests").add(request)
            .addOnSuccessListener {
                _error.value = "Friend request sent successfully"
            }
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

    fun addFriend(userId: String, email: String) {
        if(userId.isEmpty()){
            _error.value = "Id cannot be empty"
            return
        }
        if (email.isEmpty()) {
            _error.value = "Email cannot be empty"
            return
        }


        // First check if the user is already a friend
        db.collection("users").document(currentUserId).collection("friends")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _error.value = "User is already your friend"
                    return@addOnSuccessListener
                }

                // Check if the user with the given email exists
                db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        if (userDocs.isEmpty) {
                            _error.value = "User with this email not found"
                            return@addOnSuccessListener
                        }

                        val user = userDocs.first().toObject<User>()

                        // Add the user to current user's friends collection
                        db.collection("users").document(currentUserId).collection("friends")
                            .document(userId)
                            .set(mapOf(
                                "userId" to user.uid,
                                "name" to user.displayName,
                                "email" to user.email,
                                "imageUrl" to user.photoUrl
                            ))
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


data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = ""
)

data class Friend(
    val userId: String = "",
    val name: String = "",
    val email: String = ""
)