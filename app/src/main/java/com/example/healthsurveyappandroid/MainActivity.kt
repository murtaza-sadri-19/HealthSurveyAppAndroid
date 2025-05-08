package com.example.healthsurveyappandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel
import com.example.healthsurveyappandroid.ui.screens.user.SurveyFormScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Manual DI for simplicity
        val sheetsService = SheetsService()
        val repository = SurveyRepository(sheetsService)
        val viewModel = SurveyViewModel(repository)

        setContent {
            SurveyFormScreen(viewModel)
        }
    }
}