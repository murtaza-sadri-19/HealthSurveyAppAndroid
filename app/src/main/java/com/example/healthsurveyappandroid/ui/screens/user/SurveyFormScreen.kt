package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFormScreen(
    surveyViewModel: SurveyViewModel,
    authViewModel: AuthViewModel,
    onSubmitSuccess: () -> Unit,
    onLogout: () -> Unit,
    onViewHistory: () -> Unit,
    onViewProfile: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val currentSurvey by surveyViewModel.currentSurvey.collectAsState(Survey())
    val syncStatus by surveyViewModel.syncStatus.collectAsState(SurveyViewModel.SyncStatus.Idle)
    val submissionResult by surveyViewModel.submissionResult.collectAsState(null)

    var step by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(syncStatus) {
        when (val status = syncStatus) {
            is SurveyViewModel.SyncStatus.Syncing -> snackbarHostState.showSnackbar("Syncing pending surveys...")
            is SurveyViewModel.SyncStatus.Success -> {
                if (status.count > 0) snackbarHostState.showSnackbar("Synced ${status.count} surveys successfully")
            }
            is SurveyViewModel.SyncStatus.Error -> snackbarHostState.showSnackbar("Sync error: ${status.message}")
            else -> {}
        }
    }

    LaunchedEffect(submissionResult) {
        submissionResult?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            isSubmitting = false
            if (msg.contains("Submitted") || msg.contains("saved locally")) {
                onSubmitSuccess()
                surveyViewModel.clearResult()
                step = 0
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                DrawerItem(
                    label = "View Profile",
                    icon = Icons.Default.Person,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onViewProfile()
                    }
                )
                DrawerItem(
                    label = "Take Survey",
                    icon = Icons.Default.Assignment,
                    onClick = {
                        scope.launch { drawerState.close() }
                        step = 0
                    }
                )
                DrawerItem(
                    label = "View History",
                    icon = Icons.Default.History,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onViewHistory()
                    }
                )
                DrawerItem(
                    label = "Logout",
                    icon = Icons.Default.ExitToApp,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.signOut()
                            onLogout()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Health Survey") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch { surveyViewModel.syncPendingSurveys() }
                            },
                            enabled = syncStatus !is SurveyViewModel.SyncStatus.Syncing
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync Local Surveys")
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
                LinearProgressIndicator(
                    progress = (step + 1) / 4f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 16.dp)
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
                            if (!isSubmitting) {
                                isSubmitting = true
                                scope.launch {
                                    surveyViewModel.submitSurvey()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        icon = { Icon(icon, contentDescription = label) },
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}