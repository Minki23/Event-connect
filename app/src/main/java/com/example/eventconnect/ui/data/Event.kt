package com.example.eventconnect.ui.data

data class SimpleUser(
    val userId: String? = "",
    val name: String? = "",
    val email: String? = "",
    val photoUrl: String? = ""
)

data class Event(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val date: String,
    val time: String,
    val imageUrl: String,
    val host: String,
    val participants: List<SimpleUser>,
    val photoUrls: List<String> = emptyList()
)
