package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    surveyViewModel: SurveyViewModel,

    onBack: () -> Unit
) {
    val surveys by surveyViewModel.surveys.collectAsState()
    val isLoading by surveyViewModel.isLoading.collectAsState()
    val error by surveyViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        surveyViewModel.loadSurveys()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Survey History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                surveys.isEmpty() -> {
                    Text("No survey history available.")
                }
                else -> {
                    LazyColumn {
                        items(surveys) { survey ->
                            SurveyHistoryItem(survey)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurveyHistoryItem(survey: Survey) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${survey.name}")
            Text("Date: ${survey.surveyDateTime}")
            //Text("Location: ${survey.village}, ${survey.district}")
            Text("Registration ID: ${survey.registrationId}")
        }
    }
}
