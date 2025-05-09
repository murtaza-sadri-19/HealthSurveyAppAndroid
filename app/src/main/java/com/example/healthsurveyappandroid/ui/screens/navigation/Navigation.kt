package com.example.healthsurveyappandroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.ui.screens.admin.AdminHomeScreen
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.auth.RegisterScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

// Create a sealed class for navigation routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Admin : Screen("admin")
    object Survey : Screen("survey")
}

// Extract navigation to a separate composable
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel
) {
    val navController = rememberNavController()
    val loginState by authViewModel.loginState.collectAsState()

    // Determine start destination based on login state
    val startDestination = if (loginState.isSuccess) {
        if (loginState.user?.role == "admin") Screen.Admin.route else Screen.Survey.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
                    val destination = if (authViewModel.loginState.value.user?.role == "admin") {
                        Screen.Admin.route
                    } else {
                        Screen.Survey.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToHome = {
                    val destination = if (authViewModel.loginState.value.user?.role == "admin") {
                        Screen.Admin.route
                    } else {
                        Screen.Survey.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminHomeScreen(
                viewModel = surveyViewModel,
                authViewModel = authViewModel,
                onNavigateToCreateSurvey = { /* No implementation needed for now */ },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Admin.route) { inclusive = true }
                    }
                },
                onNavigateToSurveyDetail = { /* No implementation needed for now */ }
            )
        }

        composable(Screen.Survey.route) {
            SurveyFormScreen(
                viewModel = surveyViewModel,
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Survey.route) { inclusive = true }
                    }
                }
            )
        }
    }
}