package com.example.healthsurveyappandroid.ui.screens.admin

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel

// ui/screens/admin/AdminDashboardScreen.kt
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    onUserManagement: () -> Unit,
    onCreateUser: () -> Unit,
    onSurveyAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        ) // Added missing closing parenthesis

        ElevatedButton(
            onClick = onCreateUser,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New User")
        } // Added closing brace

        ElevatedButton(
            onClick = onUserManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Existing Users")
        } // Added closing brace

        ElevatedButton(
            onClick = onSurveyAnalytics,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Survey Analytics")
        } // Added closing brace

        Spacer(modifier = Modifier.height(24.dp)) // Add some space

        // --- Logout Button ---
        Button(
            onClick = {
                authViewModel.signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // it is red
        ) {
            Text("Logout")
        }
    }
}