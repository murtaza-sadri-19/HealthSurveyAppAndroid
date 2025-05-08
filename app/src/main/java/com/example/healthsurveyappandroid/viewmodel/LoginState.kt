package com.example.healthsurveyappandroid.viewmodel

sealed class LoginState {
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
    object Loading : LoginState()
    object Idle : LoginState()
}
