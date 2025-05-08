// SurveyCard.kt
package com.example.healthsurveyappandroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

@Composable
fun SurveyCard(survey: Survey) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${survey.name}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Age: ${survey.age}")
            Text(text = "Disease: ${survey.disease}")
        }
    }
}