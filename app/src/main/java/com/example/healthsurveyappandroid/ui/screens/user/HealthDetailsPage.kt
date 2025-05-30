package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailsPage(survey: Survey, onNext: (Survey) -> Unit, onBack: () -> Unit) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val immunizationOptions = listOf("Yes", "No")

    var bloodGroup by remember { mutableStateOf(survey.bloodGroup) }
    var immunizationStatus by remember { mutableStateOf(survey.immunizationStatus) }
    var disease by remember { mutableStateOf(survey.disease) }
    var remarks by remember { mutableStateOf(survey.remarks) }

    var bloodGroupError by remember { mutableStateOf<String?>(null) }
    var immunizationError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Blood Group dropdown
        var bgExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = !bgExpanded }) {
            OutlinedTextField(
                value = bloodGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = bloodGroupError != null,
                supportingText = { if (bloodGroupError != null) Text(bloodGroupError!!, color = MaterialTheme.colorScheme.error) }
            )
            ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                bloodGroups.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        bloodGroup = it
                        bgExpanded = false
                        bloodGroupError = null
                    })
                }
            }
        }

        // Immunization dropdown
        var immunExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = immunExpanded, onExpandedChange = { immunExpanded = !immunExpanded }) {
            OutlinedTextField(
                value = immunizationStatus,
                onValueChange = {},
                readOnly = true,
                label = { Text("Immunization Status") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = immunizationError != null,
                supportingText = { if (immunizationError != null) Text(immunizationError!!, color = MaterialTheme.colorScheme.error) }
            )
            ExposedDropdownMenu(expanded = immunExpanded, onDismissRequest = { immunExpanded = false }) {
                immunizationOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        immunizationStatus = it
                        immunExpanded = false
                        immunizationError = null
                    })
                }
            }
        }

        OutlinedTextField(value = disease, onValueChange = { disease = it }, label = { Text("Disease") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = {
                bloodGroupError = if (bloodGroup !in bloodGroups) "Select blood group" else null
                immunizationError = if (immunizationStatus !in immunizationOptions) "Select immunization" else null
                if (bloodGroupError == null && immunizationError == null) {
                    onNext(survey.copy(
                        bloodGroup = bloodGroup,
                        immunizationStatus = immunizationStatus,
                        disease = disease,
                        remarks = remarks
                    ))
                }
            }) { Text("Next") }
        }
    }
}