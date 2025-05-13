package com.example.healthsurveyappandroid.ui.screens.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    val samagraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> samagraIdPhotoUri = uri }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Upload Your Photo", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { photoLauncher.launch("image/*") }) {
            Text("Select Photo")
        }
        photoUri?.let {
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
        Button(onClick = { samagraLauncher.launch("image/*") }) {
            Text("Select Samagra ID Photo")
        }
        samagraIdPhotoUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Samagra ID",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = {
                surveyViewModel.setPhotoUris(photoUri, samagraIdPhotoUri)
                onSubmit()
            }) {
                Text("Submit")
            }
        }
    }
}