package com.example.eventconnect.models

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val password: String,
    val avatarUrl: String?
)
