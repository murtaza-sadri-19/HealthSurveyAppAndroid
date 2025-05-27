package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.healthsurveyappandroid.location.LocationClient
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
        val locationClient: LocationClient = LocationClientImpl(applicationContext)

        // Use application context for SheetsService to prevent leaks
        val sheetsService = SheetsService(applicationContext)
        val repository = SurveyRepository(
            sheetsService,
            surveyDao = surveyDao
        )

        setContent {
            HealthSurveyAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()

                    // Proper ViewModel initialization with factory
                    val surveyViewModel: SurveyViewModel = viewModel(
                        factory = SurveyViewModelFactory(repository, locationClient)
                    )

                    AppNavigation(authViewModel, surveyViewModel, adminViewModel)
                }
            }
        }
    }
}

// Add ViewModel Factory for proper dependency injection
class SurveyViewModelFactory(
    private val repository: SurveyRepository,
    private val locationClient: LocationClient
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurveyViewModel(repository,
                locationClient as com.example.healthsurveyappandroid.location.LocationClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
