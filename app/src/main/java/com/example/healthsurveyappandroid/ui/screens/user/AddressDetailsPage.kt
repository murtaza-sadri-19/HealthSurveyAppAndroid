package com.example.healthsurveyappandroid.ui.screens.user

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

//@OptIn(ExperimentalMaterialApi::class)
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

    // Location permission launcher
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
                val loc = (locationStatus as SurveyViewModel.LocationStatus.Success).location
                gpsCoordinates = "${loc.latitude},${loc.longitude}"
                // Reset status optionally to prevent repeated updates
                viewModel.resetLocationStatus()
            }
            is SurveyViewModel.LocationStatus.Error -> {
                Toast.makeText(
                    context,
                    "Location error: ${(locationStatus as SurveyViewModel.LocationStatus.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                // Reset status
                viewModel.resetLocationStatus()
            }
            else -> {}
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        Text(text = "Address Information", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // State Dropdown...
        // ... your existing code for state, city, district, pin code etc.
        // State Dropdown
        var stateExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = stateExpanded,
            onExpandedChange = { stateExpanded = !stateExpanded }
        ) {
            OutlinedTextField(
                value = state,
                onValueChange = {},
                readOnly = true,
                label = { Text("State") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = stateError != null,
                supportingText = { stateError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            ExposedDropdownMenu(
                expanded = stateExpanded,
                onDismissRequest = { stateExpanded = false }
            ) {
                indianStates.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            state = it
                            stateExpanded = false
                            stateError = null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("District") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pinCode,
            onValueChange = {
                pinCode = it.filter { c -> c.isDigit() }.take(6)
                pinCodeError = if (pinCode.length != 6) "Pincode must be 6 digits" else null
            },
            label = { Text("Pin Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = pinCodeError != null,
            supportingText = { pinCodeError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = permanentAddress,
            onValueChange = { permanentAddress = it },
            label = { Text("Permanent Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = temporaryAddress,
            onValueChange = { temporaryAddress = it },
            label = { Text("Temporary Address") },
            modifier = Modifier.fillMaxWidth()
        )



        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = gpsCoordinates,
                onValueChange = {},
                readOnly = true,
                label = { Text("GPS Coordinates") },
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                // Request permissions before requesting location
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }) {
                if (locationStatus == SurveyViewModel.LocationStatus.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = "Get Current Location")
                }
            }
        }

        if (gpsCoordinates.isNotBlank()) {
            Text(
                text = "Location captured: $gpsCoordinates",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = {
                // Validate state & pincode before advancing
                stateError = if (state !in indianStates) "Select a valid state" else null
                pinCodeError = if (pinCode.length != 6) "Pin code must be 6 digits" else null
                if (stateError == null && pinCodeError == null) {
                    // Form the updated survey
                    val updatedSurvey = survey.copy(
                        state = state,
                        city = city,
                        district = district,
                        pinCode = pinCode,
                        permanentAddress = permanentAddress,
                        temporaryAddress = temporaryAddress,
                        gpsCoordinates = gpsCoordinates
                    )
                    onNext(updatedSurvey)
                }
            }, enabled = gpsCoordinates.isNotBlank()) {
                Text("Next")
            }
        }
    }
}
