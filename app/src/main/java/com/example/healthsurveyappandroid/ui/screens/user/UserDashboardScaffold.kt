package com.example.healthsurveyandroid.ui.screens.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.healthsurveyandroid.ui.screens.navigation.Screen
import com.example.healthsurveyappandroid.ui.screens.user.HistoryScreen
import com.example.healthsurveyappandroid.ui.screens.user.ProfileScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
//import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScaffold(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel
) {
    val drawerItems = listOf(
        "Take Survey" to Screen.SurveyForm.route,
        "View Profile" to Screen.ViewProfile.route,
        "View History" to Screen.ViewHistory.route
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(drawerItems[0].first) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Navigation",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEach { (title, route) ->
                    NavigationDrawerItem(
                        label = { Text(title) },
                        selected = (selectedItem == title),
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                selectedItem = title
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (title) {
                                    "Take Survey" -> Icons.Default.Edit
                                    "View Profile" -> Icons.Default.Person
                                    "View History" -> Icons.Default.History
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Surveyor Panel") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Open navigation drawer")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedItem) {
                    "Take Survey" -> SurveyFormScreen(
                        authViewModel = authViewModel,
                        surveyViewModel = surveyViewModel,
                        onSubmitSuccess = { selectedItem = "Take Survey" },
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onViewHistory = { selectedItem = "View History" },
                        onViewProfile = { selectedItem = "View Profile" }
                    )
                    "View Profile" -> ProfileScreen(authViewModel = authViewModel)
                    "View History" -> HistoryScreen(
                        surveyViewModel = surveyViewModel,
                        onBack = { selectedItem = "Take Survey" }
                    )
                    else -> SurveyFormScreen(
                        authViewModel = authViewModel,
                        surveyViewModel = surveyViewModel,
                        onSubmitSuccess = {},
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onViewHistory = { selectedItem = "View History" },
                        onViewProfile = { selectedItem = "View Profile" }
                    )
                }
            }
        }
    }
}
