package com.example.healthsurveyappandroid.data

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user" // "user" or "admin"
)
