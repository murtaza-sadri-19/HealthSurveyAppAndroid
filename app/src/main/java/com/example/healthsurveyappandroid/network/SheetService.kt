package com.example.healthsurveyappandroid.network

import android.content.Context
import android.util.Log
import com.example.healthsurveyappandroid.data.Survey
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SheetsService(private val context: Context) {

    private val client = OkHttpClient()

    suspend fun submitSurvey(survey: Survey, spreadsheetId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val credentials = context.assets.open("service_account.json").use {
                GoogleCredentials.fromStream(it)
                    .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))
            }
            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue

            val rowValues = listOf(
                survey.registrationId,
                survey.name.orEmpty(),
                survey.fathersName.orEmpty(),
                survey.age.toString(),
                survey.gender.orEmpty(),
                survey.state.orEmpty(),
                survey.city.orEmpty(),
                survey.pinCode.orEmpty(),
                survey.permanentAddress.orEmpty(),
                survey.temporaryAddress.orEmpty(),
                survey.disease.orEmpty(),
                survey.bloodGroup.orEmpty(),
                survey.samagraId.orEmpty(),
                survey.highestEducation.orEmpty(),
                survey.immunizationStatus.orEmpty(),
                survey.surveyDateTime.orEmpty(),
                survey.surveyTakerId.orEmpty(),
                survey.remarks.orEmpty()
            )

            val rowArray = JSONArray().apply { rowValues.forEach { put(it) } }
            val valuesArray = JSONArray().put(rowArray)
            val jsonBody = JSONObject().put("values", valuesArray)
            val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val url = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Sheet1!A1:append?valueInputOption=RAW"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string().orEmpty()
                    Log.e("SheetsService", "Upload failed: ${response.code} → $err")
                    return@withContext false
                }
            }

            Log.d("SheetsService", "Survey uploaded successfully")
            true

        } catch (e: Exception) {
            Log.e("SheetsService", "Error uploading survey: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun fetchSurveys(spreadsheetId: String): List<Survey> = withContext(Dispatchers.IO) {
        try {
            val credentials = context.assets.open("service_account.json").use {
                GoogleCredentials.fromStream(it)
                    .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets.readonly"))
            }
            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue

            val url = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Sheet1!A2:Z"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("SheetsService", "Fetch failed: ${response.code} -> ${response.message}")
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val values = json.optJSONArray("values") ?: return@withContext emptyList()

                val surveys = mutableListOf<Survey>()
                for (i in 0 until values.length()) {
                    val row = values.getJSONArray(i)
                    val survey = Survey(
                        registrationId = row.optString(0),
                        name = row.optString(1),
                        fathersName = row.optString(2),
                        age = row.optString(3),
                        gender = row.optString(4),
                        state = row.optString(5),
                        city = row.optString(6),
                        pinCode = row.optString(7),
                        permanentAddress = row.optString(8),
                        temporaryAddress = row.optString(9),
                        disease = row.optString(10),
                        bloodGroup = row.optString(11),
                        samagraId = row.optString(12),

                        highestEducation = row.optString(13),


                        immunizationStatus = row.optString(14),


                        surveyDateTime = row.optString(15),
                        surveyTakerId = row.optString(16),
                        remarks = row.optString(17)
                    )
                    surveys.add(survey)
                }
                return@withContext surveys
            }
        } catch (e: Exception) {
            Log.e("SheetsService", "Error fetching surveys: ${e.localizedMessage}", e)
            return@withContext emptyList()
        }
    }
}
