package com.example.eventconnect.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventconnect.models.User
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    var user: User? = null
        private set

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
