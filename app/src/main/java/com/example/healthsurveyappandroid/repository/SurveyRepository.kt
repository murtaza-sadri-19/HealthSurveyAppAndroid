package com.example.healthsurveyappandroid.repository

import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SurveyRepository(private val sheetsService: SheetsService) {

    suspend fun submitSurvey(survey: Survey) = withContext(Dispatchers.IO) {
        // Use the injected sheetsService with the correct method
        sheetsService.submitSurvey(survey, Constants.SHEET_ID)
    }
    suspend fun getSurveys(): List<Survey> = withContext(Dispatchers.IO) {
        return@withContext sheetsService.fetchSurveys(Constants.SHEET_ID)
    }
}