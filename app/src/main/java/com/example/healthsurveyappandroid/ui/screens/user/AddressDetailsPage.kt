package com.example.healthsurveyappandroid.ui.screens.user

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun AddressDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit,
    onBack: () -> Unit,
    viewModel: SurveyViewModel
) {
    var state by remember { mutableStateOf(survey.state ?: "") }
    var city by remember { mutableStateOf(survey.city ?: "") }
    var district by remember { mutableStateOf(survey.district ?: "") }
    var pinCode by remember { mutableStateOf(survey.pinCode ?: "") }
    var permanentAddress by remember { mutableStateOf(survey.permanentAddress ?: "") }
    var temporaryAddress by remember { mutableStateOf(survey.temporaryAddress ?: "") }
    var gpsCoordinates by remember { mutableStateOf(survey.gpsCoordinates ?: "") }

    val context = LocalContext.current
    val locationStatus by viewModel.locationStatus.collectAsState()

    // Location permission launcher - Added from old version
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
            .padding(16.dp)
    ) {
        Text(
            text = "Address Information", // Added section header
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Address fields
        OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Added district field from old version
        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("District") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it },
            label = { Text("Pin Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
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

        // GPS Coordinates Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = gpsCoordinates,
                onValueChange = {}, // Read-only
                label = { Text("GPS Coordinates") },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {
                if (locationStatus is SurveyViewModel.LocationStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = "Get Location")
                }
            }
        }

        if (gpsCoordinates.isNotEmpty()) {
            Text(
                text = "Location captured: $gpsCoordinates",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }

            Button(
                onClick = {
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
                },
                enabled = gpsCoordinates.isNotEmpty()
            ) {
                Text("Next")
            }
        }
    }
}