package com.example.healthsurveyappandroid.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToUser: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabs = listOf("User Login", "Admin Login")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val expectedRole = if (selectedTabIndex == 0) "user" else "admin"

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                errorMessage = null
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tabs[selectedTabIndex],
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                isError = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                supportingText = {
                    if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Text("Enter a valid email address", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            TextButton(
                onClick = { onNavigateToForgotPassword() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }

            Button(
                onClick = {
                    isLoading = true
                    viewModel.loginUser(email.trim(), password) { success, error, user ->
                        isLoading = false
                        if (!success) {
                            errorMessage = when {
                                error == null -> "Login failed"
                                error.contains("There is no user record") -> "User not found"
                                error.contains("The password is invalid") -> "Incorrect password"
                                else -> "Login failed"
                            }
                        } else if (user?.role != expectedRole) {
                            errorMessage = "You are not allowed to log in as ${tabs[selectedTabIndex]}"
                            viewModel.signOut()
                        } else {
                            when (user.role) {
                                "admin" -> onNavigateToAdmin()
                                "user" -> onNavigateToUser()
                                else -> errorMessage = "Unknown role"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 8.dp),
                enabled = !isLoading &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login as ${expectedRole.replaceFirstChar { it.uppercase() }}")
                }
            }

            Button(
                onClick = { onGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 16.dp)
            ) {
                Text("Sign in with Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onNavigateToRegister() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? Register")
            }
        }
    }
}
