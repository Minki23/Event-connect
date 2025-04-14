package com.example.eventconnect.models

data class Friend(
    val name: String,
    val email: String,
    val avatarUrl: String,
    val isInvitation: Boolean = false
)
