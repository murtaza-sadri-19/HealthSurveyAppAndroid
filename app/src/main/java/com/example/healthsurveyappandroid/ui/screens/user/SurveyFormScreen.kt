package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ExitToApp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    // State observers
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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Survey") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                surveyViewModel.syncPendingSurveys()
                            }
                        },
                        enabled = syncStatus !is SurveyViewModel.SyncStatus.Syncing
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sync Local Surveys")
                        if (syncStatus is SurveyViewModel.SyncStatus.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    // Logout Button
                    IconButton(
                        onClick = {
                            authViewModel.signOut() // Call signOut from AuthViewModel
                            onLogout()
                        }
                    ) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Survey step content
            LinearProgressIndicator(
                progress = { (step + 1) / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(bottom = 16.dp),
            )
            Text(
                text = "Step ${step + 1} of 4",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            when (step) {
                0 -> PersonalDetailsPage(
                    survey = currentSurvey,
                    onNext = {
                        surveyViewModel.updateSurvey(it)
                        step++
                    }
                )
                1 -> HealthDetailsPage(
                    survey = currentSurvey,
                    onNext = {
                        surveyViewModel.updateSurvey(it)
                        step++
                    },
                    onBack = { step-- }
                )
                2 -> AddressDetailsPage(

                    survey = currentSurvey,
                    viewModel = surveyViewModel,
                    onNext = {
                        surveyViewModel.updateSurvey(it)
                        step++
                    },
                    onBack = { step-- }
                )
                3 -> DocumentUploadPage(
                    surveyViewModel = surveyViewModel,
                    onBack = { step-- },
                    onSubmit = {
                        scope.launch {
                            surveyViewModel.submitSurvey()
                            onSubmitSuccess()
                        }
                    }
                )
            }
        }
    }
}

