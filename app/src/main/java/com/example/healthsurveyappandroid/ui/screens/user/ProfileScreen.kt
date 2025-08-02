package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text("👤 Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        val user = authState.user

        if (user != null) {
            Text("📧 Email: ${user.email}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("🆔 UID: ${user.id}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("🔑 Role: ${user.role}")
        } else {
            Text("No user is currently logged in.")
        }
    }
}
