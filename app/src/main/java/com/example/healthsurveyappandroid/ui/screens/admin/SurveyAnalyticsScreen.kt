package com.example.healthsurveyappandroid.ui.screens.admin

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyAnalyticsScreen(viewModel: SurveyViewModel) {
    val context = LocalContext.current
    val surveys by viewModel.surveys.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

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

    // Disease analysis
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Survey Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${surveys.size} total responses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                exportSurveysToCSV(context, surveys)
                                isExporting = false
                            }
                        },
                        enabled = surveys.isNotEmpty() && !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export CSV",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (surveys.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertChart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Survey Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete surveys to view analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overview Cards
                    item {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernDashboardCard(
                                title = "Total Surveys",
                                value = "${surveys.size}",
                                icon = Icons.Default.Assignment,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            ModernDashboardCard(
                                title = "Avg Age",
                                value = if (avgAge > 0) "$avgAge" else "N/A",
                                icon = Icons.Default.Person,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernDashboardCard(
                                title = "States",
                                value = "${stateGroups.keys.filter { it != "Not Specified" }.size}",
                                icon = Icons.Default.Map,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            ModernDashboardCard(
                                title = "Diseases",
                                value = "${diseaseGroups.size}",
                                icon = Icons.Default.MedicalServices,
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Gender Ratio Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Gender Distribution",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    GenderStatItem("Male", maleCount, MaterialTheme.colorScheme.primary)
                                    GenderStatItem("Female", femaleCount, MaterialTheme.colorScheme.secondary)
                                    if (otherGenderCount > 0) {
                                        GenderStatItem("Other", otherGenderCount, MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        }
                    }

                    // Charts Section
                    item {
                        Text(
                            text = "Detailed Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        ModernBarChart("Age Distribution", ageGroups)
                    }

                    item {
                        ModernPieChart("Gender Distribution", genderGroups)
                    }

                    item {
                        ModernBarChart("Education Level", educationGroups)
                    }

                    item {
                        ModernPieChart("Blood Group Distribution", bloodGroupGroups)
                    }

                    item {
                        ModernBarChart("Immunization Status", immunizationGroups)
                    }

                    item {
                        ModernBarChart(
                            "Top 10 States",
                            stateGroups.toList().sortedByDescending { it.second }.take(10).toMap()
                        )
                    }

                    item {
                        ModernBarChart(
                            "Top 10 Cities",
                            cityGroups.toList().sortedByDescending { it.second }.take(10).toMap()
                        )
                    }

                    if (diseaseGroups.isNotEmpty()) {
                        item {
                            ModernBarChart(
                                "Common Diseases (Top 15)",
                                diseaseGroups.toList().sortedByDescending { it.second }.take(15).toMap()
                            )
                        }
                    }

                    // Export Button
                    item {
                        Button(
                            onClick = {
                                isExporting = true
                                scope.launch {
                                    exportSurveysToCSV(context, surveys)
                                    isExporting = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isExporting,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Export to CSV",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDashboardCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
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

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GenderStatItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernBarChart(title: String, data: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isNotEmpty()) {
                EnhancedBarChart(
                    data = data,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPieChart(title: String, data: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pie Chart
                    EnhancedPieChart(
                        data = data,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Legend
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val colors = getChartColors()
                        data.entries.forEachIndexed { index, entry ->
                            LegendItem(
                                color = colors[index % colors.size],
                                label = entry.key,
                                value = entry.value
                            )
                            if (index < data.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    value: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label.take(20),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Count: $value",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EnhancedBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.values.maxOrNull() ?: 1
    val colors = getChartColors()

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val barWidth = size.width / (data.size * 1.8f)
        val barSpacing = barWidth * 0.3f
        val chartHeight = size.height - 60.dp.toPx()

        data.entries.forEachIndexed { index, entry ->
            val barHeight = (entry.value.toFloat() / maxValue) * chartHeight
            val x = index * (barWidth + barSpacing) + barSpacing

            // Draw bar with gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors[index % colors.size],
                        colors[index % colors.size].copy(alpha = 0.7f)
                    )
                ),
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw value on top
            drawText(
                textMeasurer = textMeasurer,
                text = entry.value.toString(),
                topLeft = Offset(
                    x + barWidth / 2 - 15.dp.toPx(),
                    maxOf(0f, chartHeight - barHeight - 20.dp.toPx())
                )
            )
        }
    }
}

@Composable
fun EnhancedPieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()
    if (total == 0f) return

    val colors = getChartColors()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2.5f
        var currentAngle = -90f

        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f

            // Draw arc segment
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

        // Draw white circle in center for donut effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = center
        )
    }
}

fun getChartColors(): List<Color> {
    return listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF84CC16), // Lime
        Color(0xFF6B7280), // Gray
        Color(0xFFEC4899), // Pink
        Color(0xFF14B8A6)  // Teal
    )
}

// Backend Export Function - Maintained
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

            writer.appendLine(
                listOf(
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
                ).joinToString(",") { "\"${it?.replace("\"", "\"\"")}\"" }
            )
        }
    }
}
