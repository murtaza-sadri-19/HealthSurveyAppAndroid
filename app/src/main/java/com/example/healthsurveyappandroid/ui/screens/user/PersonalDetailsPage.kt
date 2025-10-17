package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit
) {
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
    
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        SectionHeader(
            icon = Icons.Default.Person,
            title = "Personal Information",
            subtitle = "Tell us about yourself"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Field
        ModernOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            placeholder = "Enter your full name",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Father's Name Field
        ModernOutlinedTextField(
            value = fathersName,
            onValueChange = { fathersName = it },
            label = "Father's Name",
            placeholder = "Enter father's name",
            leadingIcon = Icons.Default.FamilyRestroom,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age Field
        ModernOutlinedTextField(
            value = age,
            onValueChange = { 
                age = it
                ageError = when {
                    it.isBlank() -> null
                    it.toIntOrNull() == null -> "Please enter a valid number"
                    it.toInt() < 0 || it.toInt() > 120 -> "Age must be between 0 and 120"
                    else -> null
                }
            },
            label = "Age",
            placeholder = "Enter your age",
            leadingIcon = Icons.Default.Cake,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = ageError != null,
            supportingText = ageError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gender Dropdown
        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it }
        ) {
            ModernOutlinedTextField(
                value = gender,
                onValueChange = {},
                label = "Gender",
                placeholder = "Select your gender",
                leadingIcon = Icons.Default.Wc,
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true,
                isError = genderError != null,
                supportingText = genderError,
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                            genderError = null
                        },
                        leadingIcon = {
                            if (gender == option) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Samagra ID Field
        ModernOutlinedTextField(
            value = samagraId,
            onValueChange = { samagraId = it },
            label = "Samagra ID (Optional)",
            placeholder = "Enter Samagra ID if available",
            leadingIcon = Icons.Default.Badge,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Education Dropdown
        var educationExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = educationExpanded,
            onExpandedChange = { educationExpanded = it }
        ) {
            ModernOutlinedTextField(
                value = highestEducation,
                onValueChange = {},
                label = "Highest Education",
                placeholder = "Select education level",
                leadingIcon = Icons.Default.School,
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true,
                isError = educationError != null,
                supportingText = educationError,
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = educationExpanded,
                onDismissRequest = { educationExpanded = false }
            ) {
                educationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            highestEducation = option
                            educationExpanded = false
                            educationError = null
                        },
                        leadingIcon = {
                            if (highestEducation == option) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Next Button
        Button(
            onClick = {
                val errors = mutableListOf<String>()
                
                if (name.isBlank()) errors.add("Name is required")
                if (fathersName.isBlank()) errors.add("Father's name is required")
                if (age.isBlank()) {
                    ageError = "Age is required"
                    errors.add("Age")
                } else {
                    val ageInt = age.toIntOrNull()
                    if (ageInt == null || ageInt < 0 || ageInt > 120) {
                        ageError = "Please enter a valid age (0-120)"
                        errors.add("Age")
                    }
                }
                if (gender.isBlank()) {
                    genderError = "Please select a gender"
                    errors.add("Gender")
                }
                if (highestEducation.isBlank()) {
                    educationError = "Please select education level"
                    errors.add("Education")
                }
                
                if (errors.isEmpty()) {
                    onNext(
                        survey.copy(
                            name = name,
                            fathersName = fathersName,
                            age = age,
                            gender = gender,
                            samagraId = samagraId,
                            highestEducation = highestEducation
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { 
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        readOnly = readOnly,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    )
}
