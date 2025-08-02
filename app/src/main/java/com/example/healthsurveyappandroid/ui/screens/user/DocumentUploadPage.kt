package com.example.healthsurveyappandroid.ui.screens.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@Composable
fun DocumentUploadPage(
    surveyViewModel: SurveyViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var samagraIdPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var surveyTakerId by remember { mutableStateOf("") }

    // Launchers for photo/image picking
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    val samagraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> samagraIdPhotoUri = uri }

    // For handling keyboard hiding
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .imePadding()
    ) {
        Text("Upload Your Photo", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = { photoLauncher.launch("image/*") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Select Photo")
        }
        photoUri?.let {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "User Photo",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Upload Samagra ID Photo", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = { samagraLauncher.launch("image/*") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Select Samagra ID Photo")
        }
        samagraIdPhotoUri?.let {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Samagra ID",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = surveyTakerId,
            onValueChange = { surveyTakerId = it },
            label = { Text("Survey Taker ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    surveyViewModel.setPhotoUris(photoUri, samagraIdPhotoUri)
                    surveyViewModel.setSurveyTakerId(surveyTakerId.trim())
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSubmit()
                },
                // Optionally add validation: disable Submit if surveyTakerId is blank
                enabled = surveyTakerId.isNotBlank()
            ) {
                Text("Submit")
            }
        }
    }
}
