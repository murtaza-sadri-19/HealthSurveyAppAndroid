package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel

@Composable
fun CreateUserScreen(viewModel: AdminViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Role (admin/user)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                isLoading = true
                viewModel.createUser(email, password, role) { success, error ->
                    isLoading = false
                    resultMessage = if (success) "User created!" else error ?: "Failed"
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
            else Text("Create User")
        }
        resultMessage?.let {
            Text(text = it, color = if (it == "User created!") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}