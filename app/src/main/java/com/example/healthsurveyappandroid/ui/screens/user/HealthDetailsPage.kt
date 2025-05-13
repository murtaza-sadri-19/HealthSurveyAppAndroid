package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.healthsurveyappandroid.data.Survey
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HealthDetailsPage(survey: Survey, onNext: (Survey) -> Unit, onBack: () -> Unit) {
    var bloodGroup by remember { mutableStateOf(survey.bloodGroup) }
    var immunizationStatus by remember { mutableStateOf(survey.immunizationStatus) }
    var disease by remember { mutableStateOf(survey.disease) }
    var remarks by remember { mutableStateOf(survey.remarks) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") })
        OutlinedTextField(value = immunizationStatus, onValueChange = { immunizationStatus = it }, label = { Text("Immunization Status") })
        OutlinedTextField(value = disease, onValueChange = { disease = it }, label = { Text("Disease") })
        OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") })

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = {
                onNext(survey.copy(
                    bloodGroup = bloodGroup,
                    immunizationStatus = immunizationStatus,
                    disease = disease,
                    remarks = remarks
                ))
            }) {
                Text("Next")
            }
        }
    }
}