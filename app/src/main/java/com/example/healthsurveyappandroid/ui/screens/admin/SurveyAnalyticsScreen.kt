package com.example.healthsurveyappandroid.ui.screens.admin

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.math.*

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SurveyAnalyticsScreen(viewModel: SurveyViewModel) {
    val context = LocalContext.current
    val surveys by viewModel.surveys.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    // Analytics Data with safe parsing
    val validAges = surveys.mapNotNull { survey ->
        try {
            survey.age.toDoubleOrNull()?.takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }
    }

    val ageGroups = surveys.groupingBy {
        try {
            val age = it.age.toDoubleOrNull() ?: 0.0
            when {
                age < 18 -> "Under 18"
                age < 30 -> "18-29"
                age < 45 -> "30-44"
                age < 60 -> "45-59"
                else -> "60+"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }.eachCount()

    val genderGroups = surveys.groupingBy { it.gender.ifEmpty { "Not Specified" } }.eachCount()
    val educationGroups = surveys.groupingBy { it.highestEducation.ifEmpty { "Not Specified" } }.eachCount()
    val bloodGroupGroups = surveys.groupingBy { it.bloodGroup.ifEmpty { "Not Specified" } }.eachCount()
    val immunizationGroups = surveys.groupingBy { it.immunizationStatus.ifEmpty { "Not Specified" } }.eachCount()
    val stateGroups = surveys.groupingBy { it.state.ifEmpty { "Not Specified" } }.eachCount()
    val cityGroups = surveys.groupingBy { it.city.ifEmpty { "Not Specified" } }.eachCount()
    val districtGroups = surveys.groupingBy { it.district.ifEmpty { "Not Specified" } }.eachCount()

    // Disease analysis - handling multiple diseases per survey
    val diseaseGroups = surveys.flatMap { survey ->
        survey.disease.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }.groupingBy { it }.eachCount()

    val avgAge = if (validAges.isNotEmpty()) validAges.average().toInt() else 0
    val maleCount = genderGroups["Male"] ?: 0
    val femaleCount = genderGroups["Female"] ?: 0
    val otherGenderCount = genderGroups.values.sum() - maleCount - femaleCount

    LaunchedEffect(Unit) {
        viewModel.loadSurveys()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Health Survey Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Total Surveys",
                    value = "${surveys.size}",
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Avg Age",
                    value = if (avgAge > 0) "$avgAge" else "N/A",
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Gender Ratio",
                    value = "$maleCount M / $femaleCount F" + if (otherGenderCount > 0) " / $otherGenderCount O" else "",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "States Covered",
                    value = "${stateGroups.keys.filter { it != "Not Specified" }.size}",
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Cities Covered",
                    value = "${cityGroups.keys.filter { it != "Not Specified" }.size}",
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Diseases Reported",
                    value = "${diseaseGroups.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SurveyBarChart("Age Distribution", ageGroups)
        }

        item {
            SurveyPieChart("Gender Distribution", genderGroups)
        }

        item {
            SurveyBarChart("Education Level Distribution", educationGroups)
        }

        item {
            SurveyPieChart("Blood Group Distribution", bloodGroupGroups)
        }

        item {
            SurveyBarChart("Immunization Status", immunizationGroups)
        }

        item {
            SurveyBarChart("State Distribution", stateGroups.toList().sortedByDescending { it.second }.take(10).toMap())
        }

        item {
            SurveyBarChart("Top 10 Cities", cityGroups.toList().sortedByDescending { it.second }.take(10).toMap())
        }

        item {
            SurveyBarChart("District Distribution", districtGroups.toList().sortedByDescending { it.second }.take(10).toMap())
        }

        if (diseaseGroups.isNotEmpty()) {
            item {
                SurveyBarChart("Common Diseases", diseaseGroups.toList().sortedByDescending { it.second }.take(15).toMap())
            }
        }

        // Combined Age-Gender Analysis
        item {
            val ageGenderData = surveys.groupBy { survey ->
                try {
                    val age = survey.age.toDoubleOrNull() ?: 0.0
                    val ageGroup = when {
                        age < 18 -> "Under 18"
                        age < 30 -> "18-29"
                        age < 45 -> "30-44"
                        age < 60 -> "45-59"
                        else -> "60+"
                    }
                    "${survey.gender.ifEmpty { "Unknown" }} ($ageGroup)"
                } catch (e: Exception) {
                    "Unknown"
                }
            }.mapValues { it.value.size }

            SurveyBarChart("Age-Gender Cross Analysis", ageGenderData)
        }

        // Education-Gender Analysis
        item {
            val educationGenderData = surveys.groupBy { survey ->
                "${survey.gender.ifEmpty { "Unknown" }} - ${survey.highestEducation.ifEmpty { "Not Specified" }}"
            }.mapValues { it.value.size }

            SurveyBarChart("Education-Gender Analysis", educationGenderData.toList().sortedByDescending { it.second }.take(12).toMap())
        }

        item {
            Button(
                onClick = {
                    scope.launch { exportSurveysToCSV(context, surveys) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export CSV")
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SurveyBarChart(title: String, data: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isNotEmpty()) {
                BarChart(
                    data = data,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available")
                }
            }
        }
    }
}

@Composable
fun SurveyPieChart(title: String, data: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isNotEmpty()) {
                Row {
                    PieChart(
                        data = data,
                        modifier = Modifier
                            .size(200.dp)
                            .weight(1f)
                    )

                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6),
                            Color(0xFF06B6D4),
                            Color(0xFF10B981),
                            Color(0xFFF59E0B),
                            Color(0xFFEF4444),
                            Color(0xFF84CC16),
                            Color(0xFF6B7280)
                        )

                        data.entries.forEachIndexed { index, entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors[index % colors.size])
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${entry.key.take(15)}: ${entry.value}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available")
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.values.maxOrNull() ?: 1
    val colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFF84CC16),
        Color(0xFF6B7280)
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val barWidth = size.width / (data.size * 1.5f)
        val barSpacing = barWidth * 0.5f
        val chartHeight = size.height - 80.dp.toPx() // Leave more space for labels

        data.entries.forEachIndexed { index, entry ->
            val barHeight = (entry.value.toFloat() / maxValue) * chartHeight
            val x = index * (barWidth + barSpacing) + barSpacing

            // Draw bar
            drawRect(
                color = colors[index % colors.size],
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight)
            )

            // Draw value on top of bar
            drawText(
                textMeasurer = textMeasurer,
                text = entry.value.toString(),
                topLeft = Offset(
                    x + barWidth / 2 - 10.dp.toPx(),
                    maxOf(0f, chartHeight - barHeight - 25.dp.toPx())
                )
            )

            // Draw label below bar (truncated and rotated for better fit)
            val labelText = if (entry.key.length > 10) "${entry.key.take(8)}..." else entry.key
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(
                    x + barWidth / 2 - 20.dp.toPx(),
                    chartHeight + 10.dp.toPx()
                )
            )
        }
    }
}

@Composable
fun PieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()
    if (total == 0f) return

    val colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFF84CC16),
        Color(0xFF6B7280)
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2.5f
        var currentAngle = -90f // Start from top

        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f

            drawArc(
                color = colors[index % colors.size],
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            currentAngle += sweepAngle
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
        val uri = resolver.insert(collection, contentValues)
            ?: throw Exception("Failed to create file URI")

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