package com.example.healthsurveyappandroid.ui.screens.navigation

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.ui.screens.admin.AdminHomeScreen
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.auth.RegisterScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
import com.example.healthsurveyappandroid.ui.screens.admin.AdminDashboardScreen
import com.example.healthsurveyappandroid.ui.screens.admin.CreateUserScreen
import com.example.healthsurveyappandroid.ui.screens.admin.ManageUsersScreen
import com.example.healthsurveyappandroid.ui.screens.admin.SurveyAnalyticsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminHome : Screen("admin/home")
    object AdminDashboard : Screen("admin/dashboard")
    object CreateUser : Screen("admin/create-user")
    object ManageUsers : Screen("admin/manage-users")
    object SurveyForm : Screen("survey/form")
    object Dashboard : Screen("dashboard")
    object SurveyAnalytics : Screen("admin/survey-analytics")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.user?.role) {
        when (authState.user?.role) {
            "admin" -> navController.navigate(Screen.AdminHome.route) {
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
                onNavigateToAdmin = { navController.navigate(Screen.AdminHome.route) },
                onNavigateToUser = { navController.navigate(Screen.SurveyForm.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.SurveyForm.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                viewModel = surveyViewModel,
                authViewModel = authViewModel,
                onNavigateToCreateSurvey = { /* Not yet implemented */ },
                onNavigateToLogin = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminHome.route) { inclusive = true }
                    }
                },
                onNavigateToSurveyDetail = { /* Not yet implemented */ }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onUserManagement = { navController.navigate(Screen.ManageUsers.route) },
                onCreateUser = { navController.navigate(Screen.CreateUser.route) }
            )
        }
        composable(Screen.CreateUser.route) {
            CreateUserScreen(viewModel = authViewModel as AdminViewModel)
        }
        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(viewModel = authViewModel as AdminViewModel)
        }
        composable(Screen.SurveyForm.route) {
            SurveyFormScreen(
                surveyViewModel = surveyViewModel,
                onSubmitSuccess = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }
        composable(Screen.SurveyAnalytics.route) {
            SurveyAnalyticsScreen(viewModel = surveyViewModel)
        }
        composable(Screen.Dashboard.route) {
            Text("Survey Submitted Successfully!")
        }
    }
}