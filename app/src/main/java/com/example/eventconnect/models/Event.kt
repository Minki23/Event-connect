package com.example.eventconnect.models

data class Event(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val date: String,
    val time: String,
    val imageUrl: String,
    val host: String,
    val participants: List<UserSimple>,
    val photoUrls: List<String> = emptyList()
)