package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.ui.screens.auth.LoginScreen
import com.example.healthsurveyappandroid.ui.screens.admin.AdminHomeScreen
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sheetsService = SheetsService(this)
        val repository = SurveyRepository(sheetsService)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val surveyViewModel = remember { SurveyViewModel(repository) }

            NavHost(navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        viewModel = authViewModel,
                        onUserLogin = { navController.navigate("survey") },
                        onAdminLogin = { navController.navigate("admin") }
                    )
                }
                composable("survey") {
                    SurveyFormScreen(surveyViewModel)
                }
                composable("admin") {
                    AdminHomeScreen(surveyViewModel)
                }
            }
        }
    }
}
