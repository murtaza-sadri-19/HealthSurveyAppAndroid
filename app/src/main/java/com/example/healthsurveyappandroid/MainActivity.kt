package com.example.healthsurveyappandroid

import android.content.Context
import android.content.IntentFilter
import com.example.healthsurveyappandroid.location.LocationClientImpl

import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthsurveyappandroid.data.SurveyDatabase
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.ui.screens.navigation.AppNavigation
import com.example.healthsurveyappandroid.ui.theme.HealthSurveyAppAndroidTheme
import com.example.healthsurveyappandroid.utils.NetworkReceiver
import com.example.healthsurveyappandroid.viewmodel.AuthViewModel
//import com.example.healthsurveyappandroid.viewmodel.LocationClient
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var networkReceiver: NetworkReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseAuth.getInstance()
        FirebaseFirestore.getInstance()

        // Initialize Room database and repository
        val database = SurveyDatabase.getDatabase(applicationContext)
        val dao = database.surveyDao()
        val sheetsService = SheetsService(applicationContext)
        val repository = SurveyRepository(sheetsService, dao)

        // Register network listener with callback
        networkReceiver = NetworkReceiver(repository) { success ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (success) "Surveys synced successfully"
                    else "Some surveys failed to sync",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        setContent {
            HealthApp(repository)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister receiver when activity is destroyed
        unregisterReceiver(networkReceiver)
    }
}

@Composable
fun HealthApp(repository: SurveyRepository) {
    HealthSurveyAppAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val authViewModel: AuthViewModel = viewModel()
            val locationClient = LocationClientImpl(LocalContext.current)
            val surveyViewModel = SurveyViewModel(repository, locationClient)

            AppNavigation(authViewModel, surveyViewModel)
        }
    }
}
