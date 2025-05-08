package com.example.healthsurveyappandroid.ui.screens.user

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFormScreen(viewModel: SurveyViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Form state
    var name by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var educationLevel by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var nutritionStatus by remember { mutableStateOf(false) }
    var immunizationStatus by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    // GPS and image
    var gpsLocation by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBase64 by remember { mutableStateOf<String?>(null) }

    // Location permission request
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    gpsLocation = "${it.latitude},${it.longitude}"
                    Toast.makeText(context, "GPS Captured", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Image Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            val bitmap: Bitmap? = try {
                if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            bitmap?.let { bmp ->
                val outputStream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val imageBytes = outputStream.toByteArray()
                photoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("Guardian Name") })
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
        DropdownSelector("Sex", sex, listOf("Male", "Female", "Other")) { sex = it }
        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") })
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
        OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") })
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
        OutlinedTextField(value = disease, onValueChange = { disease = it }, label = { Text("Disease") })
        DropdownSelector("Blood Group", bloodGroup, listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")) {
            bloodGroup = it
        }
        OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar") })
        DropdownSelector("Education Level", educationLevel, listOf("None", "Primary", "Secondary", "Graduate", "Postgraduate")) {
            educationLevel = it
        }
        OutlinedTextField(value = schoolName, onValueChange = { schoolName = it }, label = { Text("School Name") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = nutritionStatus, onCheckedChange = { nutritionStatus = it })
            Text("Nutrition Status")
        }
        DropdownSelector("Immunization Status", immunizationStatus, listOf("Complete", "Incomplete", "Not Started")) {
            immunizationStatus = it
        }
        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") })
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") })
        OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") })

        Button(onClick = {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        gpsLocation = "${it.latitude},${it.longitude}"
                        Toast.makeText(context, "GPS Captured", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }) {
            Text("Get GPS Location")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { photoPickerLauncher.launch("image/*") }) {
            Text("Upload Photo")
        }

        photoUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Uploaded Photo",
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val survey = Survey(
                registrationId = System.currentTimeMillis().toString(),
                name = name,
                guardianName = guardianName,
                age = age.toIntOrNull() ?: 0,
                sex = sex,
                state = state,
                city = city,
                pincode = pincode,
                address = address,
                disease = disease,
                bloodGroup = bloodGroup,
                aadhaar = aadhaar,
                photoBase64 = photoBase64,
                educationLevel = educationLevel,
                schoolName = schoolName,
                nutritionStatus = nutritionStatus,
                immunizationStatus = immunizationStatus,
                village = village,
                district = district,
                gpsLocation = gpsLocation,
                surveyDateTime = LocalDateTime.now().toString(),
                surveyTakerId = "SURV123",
                remarks = remarks
            )

            coroutineScope.launch {
                try {
                    viewModel.submitSurvey(survey)
                    Toast.makeText(context, "Survey Submitted!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Submission Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Submit Survey")
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}