package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    surveyViewModel: SurveyViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Take Survey") }

    val drawerItems = listOf("View Profile", "Take Survey", "View History", "Logout")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = selectedItem == item,
                        onClick = {
                            selectedItem = item
                            coroutineScope.launch { drawerState.close() }
                            if (item == "Logout") {
                                authViewModel.signOut()
                                onLogout()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    "View Profile" -> Icons.Default.Person
                                    "Take Survey" -> Icons.Default.Assignment
                                    "View History" -> Icons.Default.History
                                    "Logout" -> Icons.Default.ExitToApp
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Health Survey Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open drawer")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedItem) {
                    "Take Survey" -> SurveyFormScreen(
                        surveyViewModel = surveyViewModel,
                        authViewModel = authViewModel,
                        onSubmitSuccess = onSubmitSuccess,
                        onLogout = onLogout,
                        onViewProfile = { selectedItem = "View Profile" },
                        onViewHistory = { selectedItem = "View History" }
                    )

                    "View Profile" -> ProfileScreen(authViewModel = authViewModel)

                    "View History" -> HistoryScreen(
                        surveyViewModel = surveyViewModel,
                        onBack = { selectedItem = "Take Survey" }
                    )
                }
            }
        }
    }
}
