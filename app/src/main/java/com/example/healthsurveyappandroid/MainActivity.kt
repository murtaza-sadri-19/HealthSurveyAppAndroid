package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthsurveyandroid.ui.screens.navigation.AppNavigation
import com.example.healthsurveyappandroid.data.SurveyDatabase
import com.example.healthsurveyappandroid.location.LocationClient
import com.example.healthsurveyappandroid.location.LocationClientImpl
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
//import com.example.healthsurveyappandroid.ui.screens.navigation.AppNavigation
import com.example.healthsurveyappandroid.ui.theme.HealthSurveyAppAndroidTheme
import com.example.healthsurveyappandroid.utils.GoogleSignInManager
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = SurveyDatabase.getDatabase(applicationContext)
        val surveyDao = database.surveyDao()
        val locationClient: LocationClient = LocationClientImpl(applicationContext)
        val sheetsService = SheetsService(applicationContext)
        val repository = SurveyRepository(sheetsService, surveyDao)

        setContent {
            HealthSurveyAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()
                    val surveyViewModel: SurveyViewModel = viewModel(
                        factory = SurveyViewModelFactory(repository, locationClient)
                    )
                    val google = GoogleSignInManager

                    // No need to pass `GoogleSignInManager` here — used directly in LoginScreen
                    AppNavigation(
                        authViewModel = authViewModel,
                        surveyViewModel = surveyViewModel,
                        adminViewModel = adminViewModel,
                        googleSignIn = google
                    )
                }
            }
        }
    }
}

class SurveyViewModelFactory(
    private val repository: SurveyRepository,
    private val locationClient: LocationClient
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurveyViewModel(repository, locationClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}