package com.example.healthsurveyappandroid.viewmodel

import android.location.Location
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.repository.SurveyRepository
import com.example.healthsurveyappandroid.location.LocationClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SurveyViewModel(
    private val repository: SurveyRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    // List of surveys loaded from local or network
    private val _surveys = MutableStateFlow<List<Survey>>(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    // Loading state for surveys list or other UI loading scenarios
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // General error messages exposed to UI
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Current survey in progress
    private val _currentSurvey = MutableStateFlow(Survey())
    val currentSurvey: StateFlow<Survey> = _currentSurvey.asStateFlow()

    // Photo URIs for documents
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _samagraIdUri = MutableStateFlow<Uri?>(null)
    val samagraIdUri: StateFlow<Uri?> = _samagraIdUri.asStateFlow()

    // Additional current survey state; advised to unify usage with _currentSurvey if possible
    private val _survey = mutableStateOf(Survey())
    val survey: State<Survey> = _survey

    // Submission result message (success or failure) exposed as state
    private val _submissionResult = MutableStateFlow<String?>(null)
    val submissionResult: StateFlow<String?> = _submissionResult.asStateFlow()

    // Sync status with sealed class to represent progress and result
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Location fetching status
    private val _locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Idle)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    // Survey taker ID (user id or identifier)
    private val _surveyTakerId = mutableStateOf<String?>(null)
    val surveyTakerId: State<String?> = _surveyTakerId

    // --- Sealed classes for Location and Sync statuses ---
    sealed class LocationStatus {
        object Idle : LocationStatus()
        object Loading : LocationStatus()
        data class Success(val location: Location) : LocationStatus()
        data class Error(val message: String) : LocationStatus()
    }

    sealed class SyncStatus {
        object Idle : SyncStatus()
        object Syncing : SyncStatus()
        data class Success(val count: Int) : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }

    init {
        // Initialize the current survey with default timestamp etc.
        initializeSurveyWithDefaults()
    }

    private fun initializeSurveyWithDefaults() {
        _currentSurvey.value = Survey().copy(
            surveyDateTime = getCurrentTimestamp()
        )
        _survey.value = _currentSurvey.value
    }

    private fun getCurrentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    /**
     * Update the current survey's full data
     */
    fun updateSurvey(updated: Survey) {
        _currentSurvey.value = updated
        _survey.value = updated
    }

    /**
     * Set survey taker ID across all relevant states for consistency
     */
    fun setSurveyTakerId(id: String) {
        _surveyTakerId.value = id
        // Also update survey instances preserving other data
        _survey.value = _survey.value.copy(surveyTakerId = id)
        _currentSurvey.value = _currentSurvey.value.copy(surveyTakerId = id)
    }

    /**
     * Set photo URIs for both photos and update UI state accordingly
     */
    fun setPhotoUris(photo: Uri?, samagra: Uri?) {
        _photoUri.value = photo
        _samagraIdUri.value = samagra
    }

    /**
     * Initialize/reset the current survey to a clean state,
     * preserving the surveyTakerId if exists
     */
    fun initializeSurvey() {
        val takerId = _surveyTakerId.value
        val now = getCurrentTimestamp()
        _currentSurvey.value = Survey(surveyTakerId = takerId ?: "").copy(surveyDateTime = now)
        _survey.value = _currentSurvey.value
        // Clear photo URIs as well
        _photoUri.value = null
        _samagraIdUri.value = null
        // Clear previous errors and submission results
        _error.value = null
        _submissionResult.value = null
    }

    /**
     * Load all surveys from repository filtered by survey taker ID.
     * Updates loading and error states.
     */
    fun loadSurveys() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allSurveys = repository.getSurveys()
                val userId = surveyTakerId.value
                _surveys.value = if (userId != null) {
                    allSurveys.filter { it.surveyTakerId == userId }
                } else {
                    emptyList()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load surveys"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submit the current survey.
     * Generates a unique registration ID and timestamp,
     * then submits via repository with offline fallback.
     */
    fun submitSurvey() {
        viewModelScope.launch {
            try {
                val generatedId = generateUniqueRegistrationId()
                val timestamp = getCurrentTimestamp()
                val updatedSurvey = _currentSurvey.value.copy(
                    registrationId = generatedId,
                    surveyDateTime = timestamp
                )
                val (isOnline, _) = repository.submitSurvey(updatedSurvey)
                _submissionResult.value = if (isOnline) {
                    "Submitted to Google Sheets"
                } else {
                    "No internet. Survey saved locally (${repository.getPendingSurveyCount()} pending)"
                }

                // Reset the survey but preserve surveyTakerId and reset photo URIs
                initializeSurvey()
            } catch (e: Exception) {
                _error.value = "Submission failed: ${e.message}"
            }
        }
    }

    /**
     * Generate a unique registration ID combining a timestamp and random number
     */
    private fun generateUniqueRegistrationId(): String {
        val timestampPart = System.currentTimeMillis().toString().takeLast(10) // 10 digits
        val randomPart = (100..999).random().toString()                         // 3 digits
        return timestampPart + randomPart                                       // 13-digit unique ID
    }

    /**
     * Fetch current location asynchronously and update location status.
     */
    fun getCurrentLocation() {
        viewModelScope.launch {
            _locationStatus.value = LocationStatus.Loading
            try {
                val location = locationClient.getLocation(highAccuracy = true)
                if (location != null) {
                    _locationStatus.value = LocationStatus.Success(location)
                } else {
                    _locationStatus.value = LocationStatus.Error("Location unavailable")
                }
            } catch (e: Exception) {
                _locationStatus.value = LocationStatus.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Reset location status to Idle, useful after location use.
     */
    fun resetLocationStatus() {
        _locationStatus.value = LocationStatus.Idle
    }

    /**
     * Trigger a sync of all pending local surveys with remote store.
     * Updates the submissionResult with human-readable messages.
     */
    fun syncLocalSurveys() {
        viewModelScope.launch {
            try {
                val syncResult = repository.syncPendingSurveys()
                _submissionResult.value = when (syncResult) {
                    is SurveyRepository.SyncState.Success -> "Synced ${syncResult.syncedCount} surveys"
                    is SurveyRepository.SyncState.Error -> "Sync failed: ${syncResult.message}"
                    else -> "Sync completed"
                }
            } catch (e: Exception) {
                _error.value = "Sync failed: ${e.message}"
            }
        }
    }

    /**
     * Clear the submission result message to reset UI state.
     */
    fun clearResult() {
        _submissionResult.value = null
    }

    /**
     * Start syncing pending surveys and update syncStatus accordingly.
     */
    fun syncPendingSurveys() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            try {
                when (val result = repository.syncPendingSurveys()) {
                    is SurveyRepository.SyncState.Success -> {
                        _syncStatus.value = SyncStatus.Success(result.syncedCount)
                    }
                    is SurveyRepository.SyncState.Error -> {
                        _syncStatus.value = SyncStatus.Error(result.message)
                    }
                    else -> {
                        _syncStatus.value = SyncStatus.Error("Unknown sync state")
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            }
        }
    }

    /**
     * Clear the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    // Factory for ViewModel instantiation with parameters
    class Factory(
        private val repository: SurveyRepository,
        private val locationClient: LocationClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
                return SurveyViewModel(repository, locationClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// LocationClient interface stays the same
interface LocationClient {
    suspend fun getLocation(highAccuracy: Boolean): Location?
}
