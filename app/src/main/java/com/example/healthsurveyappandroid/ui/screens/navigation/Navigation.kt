package com.example.healthsurveyappandroid.ui.screens.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.ui.screens.admin.AdminDashboardScreen
import com.example.healthsurveyappandroid.ui.screens.admin.CreateUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.ManageUsersScreen
import com.example.healthsurveyappandroid.ui.screens.admin.SurveyAnalyticsScreen
import com.example.healthsurveyappandroid.ui.screens.auth.ForgotPasswordScreen
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.auth.RegisterScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object AdminDashboard : Screen("admin/dashboard")
    object CreateUser : Screen("admin/create-user")
    object ManageUsers : Screen("admin/manage-users")
    object SurveyAnalytics : Screen("admin/survey-analytics")
    object SurveyForm : Screen("survey/form")
    object Dashboard : Screen("dashboard")
}

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
                onNavigateToUser = { navController.navigate(Screen.SurveyForm.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onGoogleSignIn = { navController.navigate(Screen.ForgotPassword.route)}
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onPasswordResetSent = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
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
                onUserManagement = { navController.navigate(Screen.ManageUsers.route) },
                onCreateUser = { navController.navigate(Screen.CreateUser.route) },
                onSurveyAnalytics = { navController.navigate(Screen.SurveyAnalytics.route) },
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateUser.route) {
            CreateUserScreen(viewModel = adminViewModel)
        }

        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(viewModel = adminViewModel)
        }

        composable(Screen.SurveyAnalytics.route) {
            SurveyAnalyticsScreen(viewModel = surveyViewModel)
        }

        composable(Screen.SurveyForm.route) {
            SurveyFormScreen(
                surveyViewModel = surveyViewModel,
                onSubmitSuccess = { navController.navigate(Screen.Dashboard.route) },
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            Text("Survey Submitted Successfully!")
        }
    }
}
