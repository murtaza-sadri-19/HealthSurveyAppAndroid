package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit,
    onBack: () -> Unit
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val immunizationOptions = listOf("Yes", "No")

    var bloodGroup by remember { mutableStateOf(survey.bloodGroup.takeIf { it in bloodGroups } ?: "") }
    var immunizationStatus by remember { mutableStateOf(survey.immunizationStatus.takeIf { it in immunizationOptions } ?: "") }
    var disease by remember { mutableStateOf(survey.disease) }
    var remarks by remember { mutableStateOf(survey.remarks) }

    var bloodGroupError by remember { mutableStateOf<String?>(null) }
    var immunizationError by remember { mutableStateOf<String?>(null) }

    fun validateBloodGroup(selection: String) {
        bloodGroupError = if (selection.isBlank() || selection !in bloodGroups) "Select blood group" else null
    }

    fun validateImmunization(selection: String) {
        immunizationError = if (selection.isBlank() || selection !in immunizationOptions) "Select immunization" else null
    }

    // Initial validation
    LaunchedEffect(Unit) {
        validateBloodGroup(bloodGroup)
        validateImmunization(immunizationStatus)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Blood Group dropdown with prompt
        var bgExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = bgExpanded,
            onExpandedChange = { bgExpanded = !bgExpanded }
        ) {
            OutlinedTextField(
                value = if (bloodGroup.isNotBlank()) bloodGroup else "Select Blood Group",
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = bloodGroupError != null,
                supportingText = {
                    if (bloodGroupError != null) Text(bloodGroupError!!, color = MaterialTheme.colorScheme.error)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded)
                }
            )
            ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                bloodGroups.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            bloodGroup = option
                            bgExpanded = false
                            validateBloodGroup(option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Immunization dropdown with prompt
        var immunExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = immunExpanded,
            onExpandedChange = { immunExpanded = !immunExpanded }
        ) {
            OutlinedTextField(
                value = if (immunizationStatus.isNotBlank()) immunizationStatus else "Select Immunization",
                onValueChange = {},
                readOnly = true,
                label = { Text("Immunization Status") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = immunizationError != null,
                supportingText = {
                    if (immunizationError != null) Text(immunizationError!!, color = MaterialTheme.colorScheme.error)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = immunExpanded)
                }
            )
            ExposedDropdownMenu(expanded = immunExpanded, onDismissRequest = { immunExpanded = false }) {
                immunizationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            immunizationStatus = option
                            immunExpanded = false
                            validateImmunization(option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = disease,
            onValueChange = { disease = it },
            label = { Text("Disease") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    validateBloodGroup(bloodGroup)
                    validateImmunization(immunizationStatus)

                    if (bloodGroupError == null && immunizationError == null) {
                        onNext(
                            survey.copy(
                                bloodGroup = bloodGroup,
                                immunizationStatus = immunizationStatus,
                                disease = disease.trim(),
                                remarks = remarks.trim()
                            )
                        )
                    }
                },
                enabled = bloodGroupError == null && immunizationError == null && bloodGroup.isNotBlank() && immunizationStatus.isNotBlank()
            ) {
                Text("Next")
            }
        }
    }
}
