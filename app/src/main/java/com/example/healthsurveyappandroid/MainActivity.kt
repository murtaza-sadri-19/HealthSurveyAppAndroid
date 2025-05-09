package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.ui.navigation.AppNavigation
import com.example.healthsurveyappandroid.ui.theme.HealthSurveyAppAndroidTheme
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        FirebaseAuth.getInstance()

        val sheetsService = SheetsService(this)
        val repository = SurveyRepository(sheetsService)

        setContent {
            HealthSurveyAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create ViewModels with proper initialization
                    val authViewModel: AuthViewModel = viewModel()
                    val surveyViewModel = remember { SurveyViewModel(repository) }

                    // Check if user is already logged in
                    authViewModel.checkCurrentUser()

                    // Use the extracted navigation component
                    AppNavigation(authViewModel, surveyViewModel)
                }
            }
        }
    }
}
