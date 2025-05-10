package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.ui.screens.navigation.AppNavigation
import com.example.healthsurveyappandroid.ui.theme.HealthSurveyAppAndroidTheme
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase services
        FirebaseAuth.getInstance()
        FirebaseFirestore.getInstance()

        val sheetsService = SheetsService(this)
        val repository = SurveyRepository(sheetsService)

        setContent {
            HealthSurveyAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val surveyViewModel = remember { SurveyViewModel(repository) }

//                   val LaunchedEffect(Unit) {
//                        authViewModel.checkCurrentUser { role ->
//                            when (role) {
//                                "admin" -> authViewModel.setAdminNavigation()
//                                "user" -> authViewModel.setUserNavigation()
//                                else -> authViewModel.setAuthNavigation()
//                            }
//                        }
//                    }

                    AppNavigation(authViewModel, surveyViewModel)
                }
            }
        }
    }
}