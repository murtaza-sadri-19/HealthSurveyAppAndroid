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
    
    var bloodGroup by remember { mutableStateOf(survey.bloodGroup) }
    var immunizationStatus by remember { mutableStateOf(survey.immunizationStatus) }
    var disease by remember { mutableStateOf(survey.disease) }
    var remarks by remember { mutableStateOf(survey.remarks) }
    
    var bloodGroupError by remember { mutableStateOf<String?>(null) }
    var immunizationError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        SectionHeader(
            icon = Icons.Default.LocalHospital,
            title = "Health Information",
            subtitle = "Your medical details"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Blood Group Dropdown
        var bgExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = bgExpanded,
            onExpandedChange = { bgExpanded = it }
        ) {
            ModernOutlinedTextField(
                value = bloodGroup,
                onValueChange = {},
                label = "Blood Group",
                placeholder = "Select your blood group",
                leadingIcon = Icons.Default.Bloodtype,
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true,
                isError = bloodGroupError != null,
                supportingText = bloodGroupError,
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = bgExpanded,
                onDismissRequest = { bgExpanded = false }
            ) {
                bloodGroups.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            bloodGroup = option
                            bgExpanded = false
                            bloodGroupError = null
                        },
                        leadingIcon = {
                            if (bloodGroup == option) {
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
        
        // Immunization Status Dropdown
        var immunExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = immunExpanded,
            onExpandedChange = { immunExpanded = it }
        ) {
            ModernOutlinedTextField(
                value = immunizationStatus,
                onValueChange = {},
                label = "Immunization Status",
                placeholder = "Have you been immunized?",
                leadingIcon = Icons.Default.Vaccines,
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true,
                isError = immunizationError != null,
                supportingText = immunizationError,
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = immunExpanded,
                onDismissRequest = { immunExpanded = false }
            ) {
                immunizationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            immunizationStatus = option
                            immunExpanded = false
                            immunizationError = null
                        },
                        leadingIcon = {
                            if (immunizationStatus == option) {
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
        
        // Disease Field
        ModernOutlinedTextField(
            value = disease,
            onValueChange = { disease = it },
            label = "Disease (if any)",
            placeholder = "Enter any disease or medical condition",
            leadingIcon = Icons.Default.MedicalServices,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Remarks Field
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks (Optional)") },
            placeholder = { 
                Text(
                    "Any additional health information",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = {
                    // Validation
                    bloodGroupError = if (bloodGroup !in bloodGroups) {
                        "Please select your blood group"
                    } else null
                    
                    immunizationError = if (immunizationStatus !in immunizationOptions) {
                        "Please select immunization status"
                    } else null
                    
                    if (bloodGroupError == null && immunizationError == null) {
                        onNext(
                            survey.copy(
                                bloodGroup = bloodGroup,
                                immunizationStatus = immunizationStatus,
                                disease = disease,
                                remarks = remarks
                            )
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
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
}
