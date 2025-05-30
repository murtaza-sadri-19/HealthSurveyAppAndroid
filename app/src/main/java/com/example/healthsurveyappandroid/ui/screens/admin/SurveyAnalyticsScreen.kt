package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@Composable
fun SurveyAnalyticsScreen(viewModel: SurveyViewModel) {
    val surveys by viewModel.surveys.collectAsState(emptyList())

    // Compute analytics
    val ageGroups = surveys.groupingBy { it.age }.eachCount()
    val genderGroups = surveys.groupingBy { it.gender }.eachCount()
    val educationGroups = surveys.groupingBy { it.highestEducation }.eachCount()
    LaunchedEffect(Unit) {
        viewModel.loadSurveys()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Survey Analytics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Surveys", style = MaterialTheme.typography.titleMedium)
                    Text("${surveys.size}", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        item {
            Text("Distribution by Age", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(ageGroups.entries.toList()) { (age, count) ->
            Text("$age: $count", style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Distribution by Gender", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(genderGroups.entries.toList()) { (gender, count) ->
            Text("$gender: $count", style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Distribution by Education", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(educationGroups.entries.toList()) { (edu, count) ->
            Text("$edu: $count", style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "For pie/bar charts, integrate a chart library such as MPAndroidChart.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}