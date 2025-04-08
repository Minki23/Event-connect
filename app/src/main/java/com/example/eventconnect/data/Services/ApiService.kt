package com.example.eventconnect.data.Services

import com.example.eventconnect.models.User
import com.example.eventconnect.models.UserRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
    @POST("/users/")
    suspend fun createUser(@Body request: UserRequest): User
}