package com.example.healthsurveyappandroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user", // "admin" or "user"
    val createdAt: com.google.firebase.Timestamp? = null
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", "user", null)

    // Convert to HashMap for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "name" to name,
            "role" to role,
            "createdAt" to createdAt ?: com.google.firebase.Timestamp.now()
        )
    }
}
