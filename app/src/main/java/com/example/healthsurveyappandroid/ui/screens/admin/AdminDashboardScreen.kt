package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun AdminDashboardScreen(
    onUserManagement: () -> Unit,
    onCreateUser: () -> Unit
) {
    Column {
        Button(onClick = onCreateUser) {
            Text("Create New User")
        }
        Button(onClick = onUserManagement) {
            Text("Manage Existing Users")
        }
    }
}