// ui/screens/admin/SurveyAnalyticsScreen.kt
package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.data.Survey

@Composable
fun SurveyAnalyticsScreen(viewModel: SurveyViewModel) {
    val surveys by viewModel.surveys.collectAsState()

    // Compute analytics (simple counts for demonstration)
    val ageGroups = surveys.groupingBy { it.age }.eachCount()
    val genderGroups = surveys.groupingBy { it.gender }.eachCount()
    val educationGroups = surveys.groupingBy { it.highestEducation }.eachCount()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Survey Analytics", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Surveys: ${surveys.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Distribution by Age:", style = MaterialTheme.typography.titleSmall)
        ageGroups.forEach { (age, count) ->
            Text("$age: $count")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Distribution by Gender:", style = MaterialTheme.typography.titleSmall)
        genderGroups.forEach { (gender, count) ->
            Text("$gender: $count")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Distribution by Education:", style = MaterialTheme.typography.titleSmall)
        educationGroups.forEach { (edu, count) ->
            Text("$edu: $count")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // For real charts, integrate a chart library such as MPAndroidChart or ComposeCharts.
        Text(
            text = "For pie/bar charts, integrate a chart library such as MPAndroidChart.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}