package com.example.healthsurveyappandroid.repository

import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.network.SheetsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SurveyRepository(private val sheetsService: SheetsService) {

    suspend fun getSurveys(): List<Survey> = withContext(Dispatchers.IO) {
        // Implement fetching surveys from Google Sheets
        return@withContext emptyList() // Replace with actual implementation
    }

    suspend fun submitSurvey(survey: Survey) = withContext(Dispatchers.IO) {
        // Use the injected sheetsService
        sheetsService.createSurvey(survey)
    }
}