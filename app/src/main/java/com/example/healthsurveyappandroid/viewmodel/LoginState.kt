package com.example.healthsurveyappandroid.viewmodel

import com.example.healthsurveyappandroid.data.User

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = "",
    val user: User? = null,
    val isGoogleSignIn: Boolean = false
)