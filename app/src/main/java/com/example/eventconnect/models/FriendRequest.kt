package com.example.eventconnect.models

data class FriendRequest(
    val senderId: String = "",
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val status: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
