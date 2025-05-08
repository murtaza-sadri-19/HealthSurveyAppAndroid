package com.example.healthsurveyappandroid.repository

import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.utils.Constants

class SurveyRepository(private val sheetsService: SheetsService) {
    suspend fun submitSurvey(survey: Survey) {
        val spreadsheetId = Constants.SHEET_ID
        val apiKey = Constants.API_KEY
        sheetsService.submitSurvey(survey, spreadsheetId, apiKey)
    }
}
