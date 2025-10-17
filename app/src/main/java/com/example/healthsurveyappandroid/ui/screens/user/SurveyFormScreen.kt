package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SurveyFormScreen(
    surveyViewModel: SurveyViewModel,
    authViewModel: AuthViewModel,
    onSubmitSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State observers - Using original structure
    val submissionResult by surveyViewModel.submissionResult.collectAsState(initial = null)
    val syncStatus by surveyViewModel.syncStatus.collectAsState()
    val currentSurvey by surveyViewModel.currentSurvey.collectAsState()

    // Handle sync status changes
    LaunchedEffect(syncStatus) {
        when (val status = syncStatus) {
            is SurveyViewModel.SyncStatus.Syncing -> {
                snackbarHostState.showSnackbar("Syncing pending surveys...")
            }
            is SurveyViewModel.SyncStatus.Success -> {
                if (status.count > 0) {
                    snackbarHostState.showSnackbar("Synced ${status.count} surveys successfully")
                }
            }
            is SurveyViewModel.SyncStatus.Error -> {
                snackbarHostState.showSnackbar("Sync error: ${status.message}")
            }
            else -> {}
        }
    }

    // Handle submission results
    LaunchedEffect(submissionResult) {
        submissionResult?.let { message ->
            snackbarHostState.showSnackbar(message)
            surveyViewModel.clearResult()
            onSubmitSuccess()
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            ModernTopAppBar(
                step = step,
                syncStatus = syncStatus,
                onSyncClick = {
                    scope.launch {
                        surveyViewModel.syncPendingSurveys()
                    }
                },
                onLogoutClick = {
                    authViewModel.signOut()
                    onLogout()
                }
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Progress Indicator
                SurveyProgressIndicator(currentStep = step)
                
                // Step Content with Animation
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "Survey Step Animation"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> PersonalDetailsPage(
                            survey = currentSurvey,
                            onNext = { survey ->
                                surveyViewModel.updateSurvey(survey)
                                step = 1
                            }
                        )
                        1 -> HealthDetailsPage(
                            survey = currentSurvey,
                            onNext = { survey ->
                                surveyViewModel.updateSurvey(survey)
                                step = 2
                            },
                            onBack = { step = 0 }
                        )
                        2 -> AddressDetailsPage(
                            survey = currentSurvey,
                            onNext = { survey ->
                                surveyViewModel.updateSurvey(survey)
                                step = 3
                            },
                            onBack = { step = 1 },
                            viewModel = surveyViewModel
                        )
                        3 -> DocumentUploadPage(
                            surveyViewModel = surveyViewModel,
                            onBack = { step = 2 },
                            onSubmit = {
                                // Backend submission maintained
                                scope.launch {
                                    surveyViewModel.submitSurvey()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    step: Int,
    syncStatus: SurveyViewModel.SyncStatus,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Health Survey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (step) {
                        0 -> "Personal Details"
                        1 -> "Health Information"
                        2 -> "Address Details"
                        3 -> "Document Upload"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Sync Button
            IconButton(
                onClick = onSyncClick,
                enabled = syncStatus !is SurveyViewModel.SyncStatus.Syncing
            ) {
                if (syncStatus is SurveyViewModel.SyncStatus.Syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Logout Button
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SurveyProgressIndicator(currentStep: Int) {
    val steps = listOf("Personal", "Health", "Address", "Documents")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, stepName ->
                    StepIndicator(
                        stepNumber = index + 1,
                        stepName = stepName,
                        isActive = index == currentStep,
                        isCompleted = index < currentStep,
                        isLast = index == steps.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.StepIndicator(
    stepNumber: Int,
    stepName: String,
    isActive: Boolean,
    isCompleted: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            // Step Circle
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isActive -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Step Name
            Text(
                text = stepName,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isActive -> MaterialTheme.colorScheme.primary
                    isCompleted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        
        // Connecting Line
        if (!isLast) {
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(2.dp)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}
