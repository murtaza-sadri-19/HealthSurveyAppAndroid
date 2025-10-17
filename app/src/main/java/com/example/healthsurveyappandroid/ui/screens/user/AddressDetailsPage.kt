package com.example.healthsurveyappandroid.ui.screens.user

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit,
    onBack: () -> Unit,
    viewModel: SurveyViewModel
) {
    val context = LocalContext.current
    val locationStatus by viewModel.locationStatus.collectAsState()
    val focusManager = LocalFocusManager.current
    
    val indianStates = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
        "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
        "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )

    var state by remember { mutableStateOf(survey.state ?: "") }
    var city by remember { mutableStateOf(survey.city ?: "") }
    var district by remember { mutableStateOf(survey.district ?: "") }
    var pinCode by remember { mutableStateOf(survey.pinCode ?: "") }
    var permanentAddress by remember { mutableStateOf(survey.permanentAddress ?: "") }
    var temporaryAddress by remember { mutableStateOf(survey.temporaryAddress ?: "") }
    var gpsCoordinates by remember { mutableStateOf(survey.gpsCoordinates ?: "") }
    
    var stateError by remember { mutableStateOf<String?>(null) }
    var pinCodeError by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineGranted || coarseGranted) {
            viewModel.getCurrentLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(locationStatus) {
        when (locationStatus) {
            is SurveyViewModel.LocationStatus.Success -> {
                val location = (locationStatus as SurveyViewModel.LocationStatus.Success).location
                gpsCoordinates = "${location.latitude},${location.longitude}"
            }
            is SurveyViewModel.LocationStatus.Error -> {
                val error = (locationStatus as SurveyViewModel.LocationStatus.Error).message
                Toast.makeText(context, "Location error: $error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        SectionHeader(
            icon = Icons.Default.LocationOn,
            title = "Address Details",
            subtitle = "Where do you live?"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // State Dropdown
        var stateExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = stateExpanded,
            onExpandedChange = { stateExpanded = it }
        ) {
            ModernOutlinedTextField(
                value = state,
                onValueChange = {},
                label = "State",
                placeholder = "Select your state",
                leadingIcon = Icons.Default.Map,
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true,
                isError = stateError != null,
                supportingText = stateError,
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = stateExpanded,
                onDismissRequest = { stateExpanded = false }
            ) {
                indianStates.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            state = option
                            stateExpanded = false
                            stateError = null
                        },
                        leadingIcon = {
                            if (state == option) {
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
        
        // City Field
        ModernOutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = "City",
            placeholder = "Enter your city",
            leadingIcon = Icons.Default.LocationCity,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // District Field
        ModernOutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = "District",
            placeholder = "Enter your district",
            leadingIcon = Icons.Default.Landscape,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pin Code Field
        ModernOutlinedTextField(
            value = pinCode,
            onValueChange = { 
                pinCode = it.filter { c -> c.isDigit() }.take(6)
                pinCodeError = when {
                    pinCode.isEmpty() -> null
                    pinCode.length != 6 -> "Pin code must be 6 digits"
                    else -> null
                }
            },
            label = "Pin Code",
            placeholder = "Enter 6-digit pin code",
            leadingIcon = Icons.Default.Pin,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = pinCodeError != null,
            supportingText = pinCodeError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Permanent Address Field
        OutlinedTextField(
            value = permanentAddress,
            onValueChange = { permanentAddress = it },
            label = { Text("Permanent Address") },
            placeholder = { 
                Text(
                    "Enter your permanent address",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Temporary Address Field
        OutlinedTextField(
            value = temporaryAddress,
            onValueChange = { temporaryAddress = it },
            label = { Text("Temporary Address (Optional)") },
            placeholder = { 
                Text(
                    "Enter temporary address if different",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.BusinessCenter,
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
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // GPS Location Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "GPS Coordinates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (gpsCoordinates.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = gpsCoordinates,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Location not captured yet. Please capture your current location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = locationStatus !is SurveyViewModel.LocationStatus.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (locationStatus is SurveyViewModel.LocationStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capturing Location...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (gpsCoordinates.isEmpty()) "Capture Location" else "Update Location")
                    }
                }
            }
        }
        
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
                shape = RoundedCornerShape(12.dp)
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
                    stateError = if (state !in indianStates) {
                        "Please select a state"
                    } else null
                    
                    pinCodeError = if (pinCode.length != 6) {
                        "Pin code must be 6 digits"
                    } else null
                    
                    if (stateError == null && pinCodeError == null && gpsCoordinates.isNotEmpty()) {
                        onNext(
                            survey.copy(
                                state = state,
                                city = city,
                                district = district,
                                pinCode = pinCode,
                                permanentAddress = permanentAddress,
                                temporaryAddress = temporaryAddress,
                                gpsCoordinates = gpsCoordinates
                            )
                        )
                    } else if (gpsCoordinates.isEmpty()) {
                        Toast.makeText(context, "Please capture GPS location", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = gpsCoordinates.isNotEmpty()
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
