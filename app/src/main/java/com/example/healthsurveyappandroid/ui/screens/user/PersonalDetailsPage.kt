package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.healthsurveyappandroid.data.Survey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit
) {
    val educationOptions = listOf("Primary", "Secondary", "Higher Secondary", "Graduate", "Post Graduate")
    val genderOptions = listOf("Male", "Female", "Prefer not to say")

    var gender by remember { mutableStateOf(if (survey.gender in genderOptions) survey.gender else "") }
    var highestEducation by remember { mutableStateOf(if (survey.highestEducation in educationOptions) survey.highestEducation else "") }

    var name by remember { mutableStateOf(survey.name) }
    var fathersName by remember { mutableStateOf(survey.fathersName) }
    var age by remember { mutableStateOf(survey.age) }
    var samagraId by remember { mutableStateOf(survey.samagraId) }

    var ageError by remember { mutableStateOf<String?>(null) }
    var educationError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }

    fun validateAge(input: String) {
        ageError = when {
            input.isBlank() -> "Age is required"
            input.length > 3 -> "Age too long"
            input.toIntOrNull() == null -> "Age must be numeric"
            input.toInt() !in 0..120 -> "Enter valid age (0-120)"
            else -> null
        }
    }

    fun validateGender(selection: String) {
        genderError = if (selection.isBlank() || selection !in genderOptions) "Select gender" else null
    }

    fun validateEducation(selection: String) {
        educationError = if (selection.isBlank() || selection !in educationOptions) "Select education" else null
    }

    // Scrollability fix
    val scrollState = rememberScrollState()

    // Initial validations
    LaunchedEffect(Unit) {
        validateAge(age)
        validateGender(gender)
        validateEducation(highestEducation)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { if (name.isBlank()) Text("Name is recommended", style = MaterialTheme.typography.bodySmall) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fathersName,
            onValueChange = { fathersName = it },
            label = { Text("Father's Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { if (fathersName.isBlank()) Text("Father's name is recommended", style = MaterialTheme.typography.bodySmall) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(3)
                age = filtered
                validateAge(filtered)
            },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            isError = ageError != null,
            singleLine = true,
            supportingText = { ageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = if (gender.isNotBlank()) gender else "Select Gender",
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = genderError != null,
                supportingText = { genderError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                            validateGender(option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = samagraId,
            onValueChange = { samagraId = it },
            label = { Text("Samagra ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { if (samagraId.isBlank()) Text("Enter Samagra ID if available", style = MaterialTheme.typography.bodySmall) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        var eduExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = eduExpanded,
            onExpandedChange = { eduExpanded = !eduExpanded }
        ) {
            OutlinedTextField(
                value = if (highestEducation.isNotBlank()) highestEducation else "Select Education",
                onValueChange = {},
                readOnly = true,
                label = { Text("Highest Education") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = educationError != null,
                supportingText = { educationError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eduExpanded) }
            )
            ExposedDropdownMenu(expanded = eduExpanded, onDismissRequest = { eduExpanded = false }) {
                educationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            highestEducation = option
                            eduExpanded = false
                            validateEducation(option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                validateAge(age)
                validateGender(gender)
                validateEducation(highestEducation)

                if (ageError == null && genderError == null && educationError == null &&
                    age.isNotBlank() && gender.isNotBlank() && highestEducation.isNotBlank()
                ) {
                    onNext(
                        survey.copy(
                            name = name.trim(),
                            fathersName = fathersName.trim(),
                            age = age,
                            gender = gender,
                            samagraId = samagraId.trim(),
                            highestEducation = highestEducation
                        )
                    )
                }
            },
            enabled = ageError == null && genderError == null && educationError == null &&
                    age.isNotBlank() && gender.isNotBlank() && highestEducation.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next") }
    }
}
