package com.example.eventconnect.models

import com.google.type.DateTime

class FriendRequest(
    val receiverEmail: String,
    var senderEmail: String,
    val status: String,
    val timestamp: DateTime
)