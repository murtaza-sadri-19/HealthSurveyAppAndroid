package com.example.healthsurveyappandroid.ui.screens.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.ui.screens.admin.AdminDashboardScreen
import com.example.healthsurveyappandroid.ui.screens.admin.CreateUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.ManageUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.SurveyAnalyticsScreen
import com.example.healthsurveyappandroid.ui.screens.auth.AuthScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin/dashboard")
    object CreateUser : Screen("admin/create-user")
    object ManageUsers : Screen("admin/manage-users")
    object SurveyAnalytics : Screen("admin/survey-analytics")
    object SurveyForm : Screen("survey/form")
    object SurveySuccess : Screen("survey/success")
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel,
    adminViewModel: AdminViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Route user based on their role after login
    LaunchedEffect(authState.user?.role) {
        when (authState.user?.role) {
            "admin" -> navController.navigate(Screen.AdminDashboard.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
            "user" -> navController.navigate(Screen.SurveyForm.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Auth Screen (Combined Login/Register)
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToUser = {
                    navController.navigate(Screen.SurveyForm.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onUserManagement = { navController.navigate(Screen.ManageUsers.route) },
                onCreateUser = { navController.navigate(Screen.CreateUser.route) },
                onSurveyAnalytics = { navController.navigate(Screen.SurveyAnalytics.route) },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Create User Screen
        composable(Screen.CreateUser.route) {
            CreateUserScreen(viewModel = adminViewModel)
        }

        // Manage Users Screen
        composable(Screen.ManageUsers.route) {
            ManageUserScreen(viewModel = adminViewModel)
        }

        // Survey Analytics Screen
        composable(Screen.SurveyAnalytics.route) {
            SurveyAnalyticsScreen(viewModel = surveyViewModel)
        }

        // Survey Form Screen
        composable(Screen.SurveyForm.route) {
            SurveyFormScreen(
                surveyViewModel = surveyViewModel,
                authViewModel = authViewModel,
                onSubmitSuccess = {
                    navController.navigate(Screen.SurveySuccess.route) {
                        popUpTo(Screen.SurveyForm.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Survey Success Screen
        composable(Screen.SurveySuccess.route) {
            SurveySuccessScreen(
                onNewSurvey = {
                    navController.navigate(Screen.SurveyForm.route) {
                        popUpTo(Screen.SurveySuccess.route) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun SurveySuccessScreen(
    onNewSurvey: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Animation Icon
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Success Message
            Text(
                text = "Survey Submitted Successfully!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Thank you for completing the health survey. Your information has been securely recorded and will help improve healthcare services.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNewSurvey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start New Survey",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Additional Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your survey response has been saved and synced to the server. You can now proceed to collect another survey or logout from the application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
