package com.example.eventconnect.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventconnect.models.User
import com.example.eventconnect.models.UserRequest
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    var user: User? = User(id = 0,username = "Michal", email = "Michal@gmail.com", password = "12345678", avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
        internal set

    fun fetchUser(userId: Int) {
        viewModelScope.launch {
            try {
                user = RetrofitInstance.api.getUser(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
