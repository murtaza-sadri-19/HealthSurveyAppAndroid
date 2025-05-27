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
    // Track pending sync status
    private val _pendingSync = MutableStateFlow(false)
    val pendingSync: StateFlow<Boolean> = _pendingSync

    // Track sync operation state
    sealed class SyncState {
        object Idle : SyncState()
        object InProgress : SyncState()
        data class Success(val syncedCount: Int, val timestamp: Long = System.currentTimeMillis()) : SyncState()
        data class Error(val message: String, val failedCount: Int) : SyncState()
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    // Track last successful sync time
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime

    /**
     * Submit survey with offline-first approach
     * @return Pair<Boolean, Boolean> where first is online success, second is local save success
     */
    suspend fun submitSurvey(survey: Survey): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        try {
            // First attempt online submission
            val onlineSuccess = sheetsService.submitSurvey(survey, Constants.SHEET_ID)
            if (onlineSuccess) {
                return@withContext Pair(true, false)
            }
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Online submission failed", e)
        }

        // Fallback to local storage
        val localSuccess = try {
            saveSurveyLocally(survey)
            true
        } catch (e: Exception) {
            false
        }

        return@withContext Pair(false, localSuccess)
    }

    /**
     * Get surveys with network-first approach
     * @return List<Survey> from network if available, otherwise from local storage
     */
    suspend fun getSurveys(): List<Survey> = withContext(Dispatchers.IO) {
        try {
            val onlineSurveys = sheetsService.fetchSurveys(Constants.SHEET_ID)
            if (onlineSurveys.isNotEmpty()) {
                // Cache the online surveys locally
                cacheSurveysLocally(onlineSurveys)
                return@withContext onlineSurveys
            }
        } catch (e: Exception) {
            Log.w("SurveyRepository", "Network fetch failed, using local data", e)
        }

        return@withContext getLocalSurveys()
    }

    /**
     * Sync all pending surveys with the online sheet
     * @return SyncState with detailed results
     */
    suspend fun syncPendingSurveys(): SyncState = withContext(Dispatchers.IO) {
        _syncState.value = SyncState.InProgress

        try {
            val pendingSurveys = getLocalSurveys()
            if (pendingSurveys.isEmpty()) {
                _pendingSync.update { false }
                return@withContext SyncState.Success(0)
            }

            val results = pendingSurveys.map { survey ->
                try {
                    val success = sheetsService.submitSurvey(survey, Constants.SHEET_ID)
                    if (success) {
                        surveyDao.deleteSurvey(survey.toEntity())
                    }
                    success
                } catch (e: Exception) {
                    false
                }
            }

            val successCount = results.count { it }
            val failedCount = results.size - successCount

            _pendingSync.update { failedCount > 0 }
            _lastSyncTime.update { System.currentTimeMillis() }

            return@withContext if (failedCount == 0) {
                SyncState.Success(successCount)
            } else {
                SyncState.Error(
                    message = "$failedCount surveys failed to sync",
                    failedCount = failedCount
                )
            }
        } catch (e: Exception) {
            val errorState = SyncState.Error(
                message = "Sync failed: ${e.message ?: "Unknown error"}",
                failedCount = getPendingSurveyCount()
            )
            _syncState.value = errorState
            return@withContext errorState
        } finally {
            // Ensure state is updated even if error occurs
            _syncState.value = syncState.value
        }
    }

    /* Helper Methods */
    private suspend fun saveSurveyLocally(survey: Survey) {
        surveyDao.insertSurvey(survey.toEntity())
        _pendingSync.update { true }
        Log.d("SurveyRepository", "Survey saved locally: ${survey.registrationId}")
    }

    private suspend fun cacheSurveysLocally(surveys: List<Survey>) {
        try {
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
        } catch (e: Exception) {
            Log.e("SurveyRepository", "Error clearing pending surveys", e)
            throw e
        }
    }
}