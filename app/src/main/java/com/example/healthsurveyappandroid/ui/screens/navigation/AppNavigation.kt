package com.example.healthsurveyandroid.ui.screens.navigation

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyandroid.ui.screens.user.UserDashboardScaffold
//import com.example.healthsurveyandroid.ui.screens.admin.*
//import com.example.healthsurveyandroid.ui.screens.auth.*
//import com.example.healthsurveyandroid.ui.screens.user.*
//import com.example.healthsurveyandroid.viewmodel.*
import com.example.healthsurveyappandroid.ui.screens.admin.AdminDashboardScreen
import com.example.healthsurveyappandroid.ui.screens.admin.CreateUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.ManageUsersScreen
import com.example.healthsurveyappandroid.ui.screens.admin.SurveyAnalyticsScreen
import com.example.healthsurveyappandroid.ui.screens.auth.ForgotPasswordScreen
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.auth.RegisterScreen
import com.example.healthsurveyappandroid.ui.screens.user.HistoryScreen
import com.example.healthsurveyappandroid.ui.screens.user.ProfileScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
//import com.example.healthsurveyappandroid.ui.screens.user.UserDashboardScaffold
// Removed: import com.example.healthsurveyappandroid.utils.GoogleSignInManager
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
    object UserDashboard : Screen("user/dashboard")
    object SurveyForm : Screen("survey/form")
    object ViewProfile : Screen("user/profile")
    object ViewHistory : Screen("user/history")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel,
    adminViewModel: AdminViewModel
    // Removed: googleSignIn : GoogleSignInManager
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Redirect on login success based on user role
    LaunchedEffect(authState.user?.role) {
        when (authState.user?.role) {
            "admin" -> {
                navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            "user" -> {
                navController.navigate(Screen.UserDashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToUser = { navController.navigate(Screen.UserDashboard.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                // Removed: onGoogleSignIn
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.UserDashboard.route) },
                onNavigateToUser = { navController.navigate(Screen.SurveyForm.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onPasswordResetSent = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                authViewModel = authViewModel,
                onUserManagement = { navController.navigate(Screen.ManageUsers.route) },
                onCreateUser = { navController.navigate(Screen.CreateUser.route) },
                onSurveyAnalytics = { navController.navigate(Screen.SurveyAnalytics.route) },
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
        composable(Screen.UserDashboard.route) {
            // Pass navController down for drawer navigation
            UserDashboardScaffold(
                navController = navController,
                authViewModel = authViewModel,
                surveyViewModel = surveyViewModel
            )
        }
        composable(Screen.SurveyForm.route) {
            SurveyFormScreen(
                authViewModel = authViewModel,
                surveyViewModel = surveyViewModel,
                onSubmitSuccess = {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.SurveyForm.route) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onViewHistory = {
                    navController.navigate(Screen.ViewHistory.route)
                },
                onViewProfile = {
                    navController.navigate(Screen.ViewProfile.route)
                }
            )
        }
        composable(Screen.ViewProfile.route) {
            ProfileScreen(authViewModel = authViewModel)
        }
        composable(Screen.ViewHistory.route) {
            HistoryScreen(
                surveyViewModel = surveyViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
