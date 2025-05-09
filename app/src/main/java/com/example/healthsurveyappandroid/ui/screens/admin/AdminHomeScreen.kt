package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@Composable
fun AdminHomeScreen(viewModel: SurveyViewModel) {
    val surveys by viewModel.surveys.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Admin Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Show total surveys
        Text("Total Surveys: ${surveys.size}")

        // List surveys
        surveys.forEach { survey ->
            Text(text = "Survey: ${survey.name} (${survey.registrationId})")
            HorizontalDivider() // Updated for Material3
        }

        // TODO: Add charts using MPAndroidChart (via AndroidView) or Compose chart library

    }
}
