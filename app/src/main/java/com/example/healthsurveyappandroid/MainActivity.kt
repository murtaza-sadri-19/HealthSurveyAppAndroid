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
import com.example.healthsurveyappandroid.data.SurveyDatabase
import com.example.healthsurveyappandroid.location.LocationClientImpl
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.ui.screens.navigation.AppNavigation
import com.example.healthsurveyappandroid.ui.theme.HealthSurveyAppAndroidTheme
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
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

        // Initialize Room database and DAO
        val database = SurveyDatabase.getDatabase(applicationContext)
        val surveyDao = database.surveyDao()

        // Initialize location client
        val locationClient = LocationClientImpl(applicationContext)

        val sheetsService = SheetsService(this)
        val repository = SurveyRepository(
            sheetsService,
            surveyDao = surveyDao  // Now properly initialized
        )

        setContent {
            HealthSurveyAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()

                    // Proper SurveyViewModel initialization
                    val surveyViewModel = remember {
                        SurveyViewModel(
                            repository = repository,
                            locationClient = locationClient  // Now provided
                        )
                    }

                    AppNavigation(authViewModel, surveyViewModel, adminViewModel)
                }
            }
        }
    }
}

private fun Unit.surveyDao() {
    TODO("Not yet implemented")
}
