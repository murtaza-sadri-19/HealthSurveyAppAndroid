package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminHomeScreen(surveys: List<com.example.healthsurveyappandroid.data.Survey>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Admin Home", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        surveys.forEach { survey ->
            Text(text = "Survey: ${survey.name} (${survey.registrationId})")
            Divider()
        }
    }
}
