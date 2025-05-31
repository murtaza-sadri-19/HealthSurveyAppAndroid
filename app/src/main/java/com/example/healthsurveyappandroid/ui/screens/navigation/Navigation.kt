package com.example.healthsurveyappandroid.ui.screens.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.ui.screens.admin.AdminDashboardScreen
import com.example.healthsurveyappandroid.ui.screens.admin.CreateUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.ManageUsersScreen
import com.example.healthsurveyappandroid.ui.screens.admin.SurveyAnalyticsScreen
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.auth.RegisterScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin/dashboard")
    object CreateUser : Screen("admin/create-user")
    object ManageUsers : Screen("admin/manage-users")
    object SurveyAnalytics : Screen("admin/survey-analytics")
    object SurveyForm : Screen("survey/form")
    object Dashboard : Screen("dashboard")
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel,
    adminViewModel: AdminViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Route user based on their role after login
    LaunchedEffect(authState.user?.role) {
        when (authState.user?.role) {
            "admin" -> navController.navigate(Screen.AdminDashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            "user" -> navController.navigate(Screen.SurveyForm.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToUser = { navController.navigate(Screen.SurveyForm.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.SurveyForm.route) }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                authViewModel = authViewModel,
                onUserManagement = { navController.navigate(Screen.ManageUsers.route) },
                onCreateUser = { navController.navigate(Screen.CreateUser.route) },
                onSurveyAnalytics = { navController.navigate(Screen.SurveyAnalytics.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // clear backstack
                    }
                }
            )
        }

        composable(Screen.CreateUser.route) {
            CreateUserScreen(
                viewModel = adminViewModel
            )
        }
        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(
                viewModel = adminViewModel
            )
        }
        composable(Screen.SurveyAnalytics.route) {
            SurveyAnalyticsScreen(
                viewModel = surveyViewModel
            )
        }
        composable(Screen.SurveyForm.route) {
            SurveyFormScreen(
                surveyViewModel = surveyViewModel,
                authViewModel = authViewModel,
                onSubmitSuccess = { navController.navigate(Screen.Dashboard.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // clear backstack
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        // Example: a vertical gradient background
                        Brush.verticalGradient(
                            colors =
                                // Dark theme: Green, Blue, Black gradient
                                listOf(
                                    Color(0xFF003366), // deep blue
                                    Color.Black       // black
                                )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Survey Submitted Successfully!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(32.dp),

                )

            }
        }
    }
}
