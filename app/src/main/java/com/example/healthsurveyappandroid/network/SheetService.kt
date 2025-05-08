package com.example.healthsurveyappandroid.network

import android.util.Log
import com.example.healthsurveyappandroid.data.Survey
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SheetsApi {
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendSurvey(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "RAW",
        @Query("insertDataOption") insertDataOption: String = "INSERT_ROWS",
        @Query("includeValuesInResponse") includeValuesInResponse: Boolean = false,
        @Query("responseDateTimeRenderOption") responseDateTimeRenderOption: String = "FORMATTED_STRING",
        @Query("key") apiKey: String,
        @Body requestBody: SheetsRequest
    ): SheetsResponse
}

data class SheetsRequest(val values: List<List<String>>)
data class SheetsResponse(
    val spreadsheetId: String,
    val updates: Updates?
)

data class Updates(
    val spreadsheetId: String,
    val updatedRange: String,
    val updatedData: UpdatedData?
)

data class UpdatedData(
    val range: String,
    val majorDimension: String
)

class SheetsService {
    private val sheetsApi: SheetsApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        sheetsApi = retrofit.create(SheetsApi::class.java)
    }

    suspend fun submitSurvey(survey: Survey, spreadsheetId: String, apiKey: String) {
        try {
            Log.d("SheetsService", "Submitting survey: ${survey.registrationId}")

            val row = listOf(
                survey.registrationId,
                survey.name,
                survey.guardianName ?: "",
                survey.age.toString(),
                survey.sex,
                survey.state,
                survey.city,
                survey.pincode,
                survey.address,
                survey.disease,
                survey.bloodGroup,
                survey.aadhaar,
                survey.photoBase64 ?: "",
                survey.educationLevel,
                survey.schoolName ?: "",
                if (survey.nutritionStatus) "Yes" else "No",
                survey.immunizationStatus,
                survey.village,
                survey.district,
                survey.gpsLocation ?: "",
                survey.surveyDateTime,
                survey.surveyTakerId,
                survey.remarks ?: ""
            )

            Log.d("SheetsService", "Survey row prepared for upload: $row")

            val request = SheetsRequest(values = listOf(row))
            val response = sheetsApi.appendSurvey(
                spreadsheetId = spreadsheetId,
                range = "Sheet1!A1:Z",
                apiKey = apiKey,
                requestBody = request
            )

            Log.d("SheetsService", "Survey pushed successfully: ${response.updates?.updatedRange}")
        } catch (e: Exception) {
            Log.e("SheetsService", "Failed to submit survey: ${e.localizedMessage}", e)
        }
    }
}