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
            // 1) Authenticate
            val credentials = context.assets.open("service_account.json").use {
                GoogleCredentials.fromStream(it)
                    .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))
            }
            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue

            // 2) Build rowValues as pure strings
            val rowValues = listOf(
                survey.registrationId,
                survey.name.orEmpty(),
                survey.guardianName.orEmpty(),
                survey.age.toString(),
                survey.sex.orEmpty(),
                survey.state.orEmpty(),
                survey.city.orEmpty(),
                survey.pincode.orEmpty(),
                survey.address.orEmpty(),
                survey.disease.orEmpty(),
                survey.bloodGroup.orEmpty(),
                survey.aadhaar.orEmpty(),
                survey.photoBase64.orEmpty(),
                survey.educationLevel.orEmpty(),
                survey.schoolName.orEmpty(),
                if (survey.nutritionStatus) "Yes" else "No",
                survey.immunizationStatus.orEmpty(),
                survey.village.orEmpty(),
                survey.district.orEmpty(),
                survey.gpsLocation.orEmpty(),
                survey.surveyDateTime.orEmpty(),
                survey.surveyTakerId.orEmpty(),
                survey.remarks.orEmpty()
            )

            // 3) Wrap into JSON arrays
            val rowArray = JSONArray().apply { rowValues.forEach { put(it) } }
            val valuesArray = JSONArray().put(rowArray)
            val jsonBody = JSONObject().put("values", valuesArray)
            val body = jsonBody
                .toString()
                .toRequestBody("application/json".toMediaType())

            // 4) Build and execute request
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
            // 1) Authenticate using service account credentials
            val credentials = context.assets.open("service_account.json").use {
                GoogleCredentials.fromStream(it)
                    .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets.readonly"))
            }
            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue

            // 2) Build the GET request URL for reading values from Sheet1
            val url = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Sheet1!A2:Z"

            // 3) Build the request with authorization header
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            // 4) Execute the request
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
                        guardianName = row.optString(2),
                        age = row.optString(3).toIntOrNull() ?: 0,
                        sex = row.optString(4),
                        state = row.optString(5),
                        city = row.optString(6),
                        pincode = row.optString(7),
                        address = row.optString(8),
                        disease = row.optString(9),
                        bloodGroup = row.optString(10),
                        aadhaar = row.optString(11),
                        photoBase64 = row.optString(12),
                        educationLevel = row.optString(13),
                        schoolName = row.optString(14),
                        nutritionStatus = row.optString(15) == "Yes",
                        immunizationStatus = row.optString(16),
                        village = row.optString(17),
                        district = row.optString(18),
                        gpsLocation = row.optString(19),
                        surveyDateTime = row.optString(20),
                        surveyTakerId = row.optString(21),
                        remarks = row.optString(22)
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