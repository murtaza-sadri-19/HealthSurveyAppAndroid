package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(username: String, password: String) {
        // Replace with real authentication logic
        _isLoggedIn.value = (username == "admin" && password == "password")
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}