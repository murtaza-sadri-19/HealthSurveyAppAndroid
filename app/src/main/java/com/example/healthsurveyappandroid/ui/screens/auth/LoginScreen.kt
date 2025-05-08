package com.example.healthsurveyappandroid.ui.screens.auth

import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.LoginState

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onUserLogin: () -> Unit,
    onAdminLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState: LoginState by viewModel.loginState.collectAsState(initial = LoginState.Idle)

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val role = (loginState as LoginState.Success).role
                if (role == "admin") onAdminLogin() else onUserLogin()
            }
            is LoginState.Error -> {
                errorMessage = (loginState as LoginState.Error).message
            }
            LoginState.Idle, LoginState.Loading -> { /* No action needed */ }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = username, onValueChange = { username = it }, label = { Text("Email") })
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.login(username, password) }) {
            Text("Login")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}