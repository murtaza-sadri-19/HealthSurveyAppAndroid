package com.example.healthsurveyappandroid.ui.screens.admin

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.OutputStream

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SurveyAnalyticsScreen(viewModel: SurveyViewModel) {
    val surveys by viewModel.surveys.collectAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showAnalytics by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Filter surveys by search query
    val filteredSurveys = surveys.filter { it.name.contains(searchQuery, ignoreCase = true) }

    // Compute analytics
    val ageGroups = surveys.groupingBy { it.age }.eachCount()
    val genderGroups = surveys.groupingBy { it.gender }.eachCount()
    val educationGroups = surveys.groupingBy { it.highestEducation }.eachCount()

    LaunchedEffect(Unit) {
        viewModel.loadSurveys()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Survey Analytics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { showAnalytics = !showAnalytics }) {
                    Text(if (showAnalytics) "Hide Analytics" else "Show Analytics")
                }
                Button(onClick = {
                    exportSurveysToCSV(context, surveys)
                }) {
                    Text("Export CSV")
                }
            }
        }

        if (showAnalytics) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Surveys", style = MaterialTheme.typography.titleMedium)
                        Text("${surveys.size}", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            item {
                Text("Distribution by Age", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            items(ageGroups.entries.toList()) { (age, count) ->
                Text("$age: $count", style = MaterialTheme.typography.bodyLarge)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Distribution by Gender", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            items(genderGroups.entries.toList()) { (gender, count) ->
                Text("$gender: $count", style = MaterialTheme.typography.bodyLarge)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Distribution by Education", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            items(educationGroups.entries.toList()) { (edu, count) ->
                Text("$edu: $count", style = MaterialTheme.typography.bodyLarge)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("All Submitted Surveys", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        items(filteredSurveys) { survey ->
            ExpandableSurveyCard(survey = survey)
        }
    }
}

@Composable
fun ExpandableSurveyCard(survey: Survey) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${survey.name}", style = MaterialTheme.typography.titleMedium)
            Text("Age: ${survey.age}, Gender: ${survey.gender}")
            Text("City: ${survey.city}, State: ${survey.state}")

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                Text("Registration ID: ${survey.registrationId}")
                Text("Father's Name: ${survey.fathersName}")
                Text("District: ${survey.district}")
                Text("Pin Code: ${survey.pinCode}")
                Text("Permanent Address: ${survey.permanentAddress}")
                Text("Temporary Address: ${survey.temporaryAddress}")
                Text("GPS: ${survey.gpsCoordinates}")
                Text("Disease: ${survey.disease}")
                Text("Blood Group: ${survey.bloodGroup}")
                Text("Samagra ID: ${survey.samagraId}")
                Text("Education: ${survey.highestEducation}")
                Text("Immunization: ${survey.immunizationStatus}")
                Text("Survey Date: ${survey.surveyDateTime}")
                Text("Taker ID: ${survey.surveyTakerId}")
                Text("Remarks: ${survey.remarks}")
                if (survey.photoUrl.isNotEmpty()) {
                    Text("Photo URL: ${survey.photoUrl}")
                }
                if (survey.samagraIdPhotoUrl.isNotEmpty()) {
                    Text("Samagra ID Photo: ${survey.samagraIdPhotoUrl}")
                }
            }

            Text(
                text = if (expanded) "Show Less" else "Show More",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun exportSurveysToCSV(context: Context, surveys: List<Survey>) {
    val fileName = "health_survey_export_${System.currentTimeMillis()}.csv"
    val mimeType = "text/csv"
    val relativeLocation = Environment.DIRECTORY_DOWNLOADS

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, relativeLocation)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, contentValues) ?: throw Exception("Failed to create file URI")

        resolver.openOutputStream(uri)?.use { outputStream ->
            writeSurveysToCSV(outputStream, surveys)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        Toast.makeText(context, "CSV saved to Downloads as $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export CSV: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun writeSurveysToCSV(outputStream: OutputStream, surveys: List<Survey>) {
    outputStream.writer().use { writer ->
        writer.appendLine("Registration ID,Survey Date,Survey Taker ID,Name,Father's Name,Age,Gender,Samagra ID,Highest Education,Blood Group,Immunization Status,Disease,Remarks,State,City,District,Pin Code,Permanent Address,Temporary Address,GPS,Photo URL,Samagra ID Photo URL")
        for (survey in surveys) {
            val regId = "=\"${survey.registrationId}\""
            val samagraId = "=\"${survey.samagraId}\""
            val dateTime = "=\"${survey.surveyDateTime}\""

            writer.appendLine(listOf(
                regId,
                dateTime,
                survey.surveyTakerId,
                survey.name,
                survey.fathersName,
                survey.age.toString(),
                survey.gender,
                samagraId,
                survey.highestEducation,
                survey.bloodGroup,
                survey.immunizationStatus,
                survey.disease,
                survey.remarks,
                survey.state,
                survey.city,
                survey.district,
                survey.pinCode,
                survey.permanentAddress,
                survey.temporaryAddress,
                survey.gpsCoordinates,
                survey.photoUrl,
                survey.samagraIdPhotoUrl
            ).joinToString(",") { "\"${it?.replace("\"", "\"\"")}\"" })
        }
    }
}