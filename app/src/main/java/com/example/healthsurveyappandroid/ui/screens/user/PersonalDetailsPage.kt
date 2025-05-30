package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsPage(survey: Survey, onNext: (Survey) -> Unit) {
    val educationOptions = listOf("Primary", "Secondary", "Higher Secondary", "Graduate", "Post Graduate")
    val genderOptions = listOf("Male", "Female", "Prefer not to say")

    var name by remember { mutableStateOf(survey.name) }
    var fathersName by remember { mutableStateOf(survey.fathersName) }
    var age by remember { mutableStateOf(survey.age) }
    var gender by remember { mutableStateOf(survey.gender) }
    var samagraId by remember { mutableStateOf(survey.samagraId) }
    var highestEducation by remember { mutableStateOf(survey.highestEducation) }

    var ageError by remember { mutableStateOf<String?>(null) }
    var educationError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = fathersName, onValueChange = { fathersName = it }, label = { Text("Father's Name") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = age,
            onValueChange = {
                age = it.filter { c -> c.isDigit() }
                ageError = if (age.isNotBlank() && (age.toIntOrNull() == null || age.toInt() !in 0..120)) "Enter valid age (0-120)" else null
            },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            isError = ageError != null,
            supportingText = { if (ageError != null) Text(ageError!!, color = MaterialTheme.colorScheme.error) }
        )

        // Gender dropdown
        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = genderError != null,
                supportingText = { if (genderError != null) Text(genderError!!, color = MaterialTheme.colorScheme.error) }
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        gender = it
                        genderExpanded = false
                        genderError = null
                    })
                }
            }
        }

        OutlinedTextField(value = samagraId, onValueChange = { samagraId = it }, label = { Text("Samagra ID") }, modifier = Modifier.fillMaxWidth())

        // Highest Education dropdown
        var eduExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = eduExpanded, onExpandedChange = { eduExpanded = !eduExpanded }) {
            OutlinedTextField(
                value = highestEducation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Highest Education") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = educationError != null,
                supportingText = { if (educationError != null) Text(educationError!!, color = MaterialTheme.colorScheme.error) }
            )
            ExposedDropdownMenu(expanded = eduExpanded, onDismissRequest = { eduExpanded = false }) {
                educationOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        highestEducation = it
                        eduExpanded = false
                        educationError = null
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Validation before proceeding
            ageError = if (age.isBlank() || age.toIntOrNull() == null || age.toInt() !in 0..120) "Enter valid age (0-120)" else null
            genderError = if (gender !in genderOptions) "Select gender" else null
            educationError = if (highestEducation !in educationOptions) "Select education" else null

            if (ageError == null && genderError == null && educationError == null) {
                onNext(survey.copy(
                    name = name,
                    fathersName = fathersName,
                    age = age,
                    gender = gender,
                    samagraId = samagraId,
                    highestEducation = highestEducation
                ))
            }
        }) {
            Text("Next")
        }
    }
}