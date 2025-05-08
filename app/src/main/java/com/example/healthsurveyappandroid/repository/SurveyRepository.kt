package com.example.healthsurveyappandroid.repository

import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.utils.Constants

class SurveyRepository(private val sheetsService: SheetsService) {

    // Submits a survey to Google Sheets
    suspend fun submitSurvey(survey: Survey) {
        val spreadsheetId = Constants.SHEET_ID
        sheetsService.submitSurvey(survey, spreadsheetId)
    }

    // Fetches all surveys from Google Sheets for admin dashboard
    suspend fun getSurveys(): List<Survey> {
        val spreadsheetId = Constants.SHEET_ID
        return try {
            sheetsService.fetchSurveys(spreadsheetId)
        } catch (e: Exception) {
            // Handle/log error as needed (could log or rethrow)
            emptyList()
        }
    }
}
