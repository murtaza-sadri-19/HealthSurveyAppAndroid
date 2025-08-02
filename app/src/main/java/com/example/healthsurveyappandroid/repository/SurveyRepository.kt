package com.example.healthsurveyappandroid.repository

import android.util.Log
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.data.SurveyDao
import com.example.healthsurveyappandroid.data.toEntity
import com.example.healthsurveyappandroid.data.toSurvey
import com.example.healthsurveyappandroid.network.SheetsService
import com.example.healthsurveyappandroid.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class SurveyRepository(
    private val sheetsService: SheetsService,
    private val surveyDao: SurveyDao
) {
    // Track pending sync status (whether unsynced surveys exist)
    private val _pendingSync = MutableStateFlow(false)
    val pendingSync: StateFlow<Boolean> = _pendingSync

    // Track sync operation state with sealed class for detailed status
    sealed class SyncState {
        object Idle : SyncState()
        object InProgress : SyncState()
        data class Success(val syncedCount: Int, val timestamp: Long = System.currentTimeMillis()) : SyncState()
        data class Error(val message: String, val failedCount: Int) : SyncState()
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    // Track last successful sync time (nullable)
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime

    /**
     * Submit survey with offline-first approach:
     * - try submitting online to Google Sheets
     * - if online fails, save locally for later sync
     * @return Pair(first = onlineSuccess, second = localSaveSuccess)
     */
    suspend fun submitSurvey(survey: Survey): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        try {
            // Attempt online submission first
            val onlineSuccess = sheetsService.submitSurvey(survey, Constants.SHEET_ID)
            if (onlineSuccess) {
                // Successfully sent online, no need to store locally
                return@withContext Pair(true, false)
            }
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Online submission failed", e)
        }

        // Online submission failed; fallback to local storage
        val localSuccess = try {
            saveSurveyLocally(survey)
            true
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Local save failed", e)
            false
        }

        return@withContext Pair(false, localSuccess)
    }

    /**
     * Get surveys with network-first approach:
     * - Try fetching from online Google Sheets
     * - If network fails or empty, fallback to local Room DB cache
     */
    suspend fun getSurveys(): List<Survey> = withContext(Dispatchers.IO) {
        try {
            val onlineSurveys = sheetsService.fetchSurveys(Constants.SHEET_ID)
            if (onlineSurveys.isNotEmpty()) {
                // Cache newly fetched online surveys locally (replace all)
                cacheSurveysLocally(onlineSurveys)
                return@withContext onlineSurveys
            }
        } catch (e: Exception) {
            Log.w("SurveyRepository", "Network fetch failed, using local data", e)
        }

        // Return local cache when network unavailable or no data
        return@withContext getLocalSurveys()
    }

    /**
     * Synchronize all pending locally saved surveys to online Google Sheets
     * Deletes successfully synced surveys from local DB
     * Returns SyncState reporting success / failure counts.
     */
    suspend fun syncPendingSurveys(): SyncState = withContext(Dispatchers.IO) {
        _syncState.value = SyncState.InProgress

        try {
            val pendingSurveys = getLocalSurveys()
            if (pendingSurveys.isEmpty()) {
                _pendingSync.update { false }
                val successState = SyncState.Success(0)
                _syncState.value = successState
                return@withContext successState
            }

            val results = pendingSurveys.map { survey ->
                try {
                    val success = sheetsService.submitSurvey(survey, Constants.SHEET_ID)
                    if (success) {
                        // Remove synced survey from local DB
                        surveyDao.deleteSurvey(survey.toEntity())
                    }
                    success
                } catch (e: Exception) {
                    Log.e("SurveyRepository", "Failed to sync survey: ${survey.registrationId}", e)
                    false
                }
            }

            val successCount = results.count { it }
            val failedCount = results.size - successCount

            // Update pending sync status accordingly
            _pendingSync.update { failedCount > 0 }
            _lastSyncTime.update { System.currentTimeMillis() }

            val syncResult = if (failedCount == 0) {
                SyncState.Success(successCount)
            } else {
                SyncState.Error(
                    message = "$failedCount surveys failed to sync",
                    failedCount = failedCount
                )
            }
            _syncState.value = syncResult
            return@withContext syncResult

        } catch (e: Exception) {
            Log.e("SurveyRepository", "Sync failed", e)
            val pendingCount = getPendingSurveyCount()
            val errorState = SyncState.Error(
                message = "Sync failed: ${e.message ?: "Unknown error"}",
                failedCount = pendingCount
            )
            _syncState.value = errorState
            return@withContext errorState
        }
    }

    /* --- Helper Methods --- */

    private suspend fun saveSurveyLocally(survey: Survey) {
        surveyDao.insertSurvey(survey.toEntity())
        _pendingSync.update { true }
        Log.d("SurveyRepository", "Survey saved locally: ${survey.registrationId}")
    }

    private suspend fun cacheSurveysLocally(surveys: List<Survey>) {
        try {
            // Insert or update all surveys in local DB
            surveyDao.insertAll(surveys.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Failed to cache surveys locally", e)
        }
    }

    private suspend fun getLocalSurveys(): List<Survey> {
        return surveyDao.getAllSurveys().map { it.toSurvey() }
    }

    suspend fun getPendingSurveyCount(): Int {
        return surveyDao.getCount()
    }

    suspend fun clearPendingSurveys() {
        try {
            surveyDao.deleteAll()
            _pendingSync.update { false }
            _syncState.value = SyncState.Success(0)
            _lastSyncTime.update { System.currentTimeMillis() }
            Log.d("SurveyRepository", "Cleared all pending surveys")
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Error clearing pending surveys", e)
            throw e
        }
    }
}
