package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel


@Composable
fun SurveyFormScreen(
    surveyViewModel: SurveyViewModel,
    onSubmitSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 🔷 TOP PANEL
        TopAppBar(
            title = { Text("Health Survey", style = MaterialTheme.typography.h5) },
            modifier = Modifier.fillMaxWidth()
        )

        // 🔷 SURVEY STEP CONTENT
        when (step) {
            0 -> PersonalDetailsPage(
                survey = surveyViewModel.currentSurvey.collectAsState().value,
                onNext = {
                    surveyViewModel.updateSurvey(it)
                    step++
                }
            )
            1 -> HealthDetailsPage(
                survey = surveyViewModel.currentSurvey.collectAsState().value,
                onNext = {
                    surveyViewModel.updateSurvey(it)
                    step++
                },
                onBack = { step-- }
            )
            2 -> AddressDetailsPage(
                survey = surveyViewModel.currentSurvey.collectAsState().value,
                onNext = {
                    surveyViewModel.updateSurvey(it)
                    step++
                },
                onBack = { step-- }
            )
            3 -> DocumentUploadPage(
                surveyViewModel = surveyViewModel,
                onBack = { step-- },
                onSubmit = {
                    surveyViewModel.submitSurvey()
                    onSubmitSuccess()
                }
            )
        }
    }
}
