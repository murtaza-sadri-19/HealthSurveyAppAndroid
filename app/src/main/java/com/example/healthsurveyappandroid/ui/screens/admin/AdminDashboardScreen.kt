package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel

@Composable
fun AdminDashboardScreen(
    onUserManagement: () -> Unit,
    onCreateUser: () -> Unit,
    onSurveyAnalytics: () -> Unit,
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit
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
        )

        ElevatedButton(
            onClick = onCreateUser,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New User")
        }

        ElevatedButton(
            onClick = onUserManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Existing Users")
        }

        ElevatedButton(
            onClick = onSurveyAnalytics,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Survey Analytics")
        }

//        Button(
//            onClick = {
//                authViewModel.signOut()
//                onSignOut()
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//        ) {
//            Text("Sign Out", color = MaterialTheme.colorScheme.onError)
//        }
    }
}