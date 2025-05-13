package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@Composable
fun AdminHomeScreen(
    viewModel: SurveyViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCreateSurvey: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSurveyDetail: () -> Unit
) {
    val surveys by viewModel.surveys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSurveys()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Admin Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        error?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text("Total Surveys: ${surveys.size}")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { viewModel.loadSurveys() }) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        surveys.forEach { survey ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Survey: ${survey.name} (${survey.registrationId})")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateToCreateSurvey) {
                Text("Create Survey")
            }
            Button(
                onClick = {
                    authViewModel.signOut()
                    onNavigateToLogin()
                }
            ) {
                Text("Logout")
            }
        }
    }
}