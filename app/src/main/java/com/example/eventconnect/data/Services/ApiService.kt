package com.example.eventconnect.data.Services

import com.example.eventconnect.models.User
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
}