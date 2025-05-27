package com.example.healthsurveyappandroid.viewmodel

import android.location.Location
import android.net.Uri
//import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.repository.SurveyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.healthsurveyappandroid.location.LocationClient

class SurveyViewModel(
    private val repository: SurveyRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _surveys = MutableStateFlow<List<Survey>>(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentSurvey = MutableStateFlow(Survey())
    val currentSurvey: StateFlow<Survey> = _currentSurvey.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _samagraIdUri = MutableStateFlow<Uri?>(null)
    val samagraIdUri: StateFlow<Uri?> = _samagraIdUri.asStateFlow()

    private val _survey = mutableStateOf(Survey())
    val survey: State<Survey> = _survey

    private val _submissionResult = MutableStateFlow<String?>(null)
    val submissionResult: StateFlow<String?> = _submissionResult.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Idle)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    private val _surveyTakerId = mutableStateOf<String?>(null)
    val surveyTakerId: State<String?> = _surveyTakerId

    // Location status sealed class
    sealed class LocationStatus {
        object Idle : LocationStatus()
        object Loading : LocationStatus()
        data class Success(val location: Location) : LocationStatus()
        data class Error(val message: String) : LocationStatus()
    }

    // Sync status sealed class
    sealed class SyncStatus {
        object Idle : SyncStatus()
        object Syncing : SyncStatus()
        data class Success(val count: Int) : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }

    init {
        initializeSurveyWithDefaults()
    }

    private fun initializeSurveyWithDefaults() {
        _currentSurvey.value = Survey().copy(
            surveyDateTime = getCurrentTimestamp()
        )
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun updateSurvey(updated: Survey) {
        _currentSurvey.value = updated
    }

    fun setSurveyTakerId(id: String) {
        _survey.value = _survey.value.copy(surveyTakerId = id)
        _currentSurvey.value = _currentSurvey.value.copy(surveyTakerId = id)
    }



    fun setPhotoUris(photo: Uri?, samagra: Uri?) {
        _photoUri.value = photo
        _samagraIdUri.value = samagra
    }

    fun initializeSurvey() {
        _survey.value = Survey()
    }

    fun loadSurveys() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _surveys.value = repository.getSurveys()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load surveys"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

                // Reset current survey with surveyTakerId preserved
                _currentSurvey.value = Survey(surveyTakerId = updatedSurvey.surveyTakerId)

            } catch (e: Exception) {
                _error.value = "Submission failed: ${e.message}"
            }
        }
    }
    private fun generateUniqueRegistrationId(): String {
        val timestampPart = System.currentTimeMillis().toString().takeLast(10) // 10 digits
        val randomPart = (100..999).random().toString()                         // 3 digits
        return timestampPart + randomPart                                       // 13-digit unique ID
    }


    /** ✅ FIXED: Updates locationStatus instead of using a callback **/
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

    /** Optional: Reset location status after use in UI */
    fun resetLocationStatus() {
        _locationStatus.value = LocationStatus.Idle
    }

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

    fun clearResult() {
        _submissionResult.value = null
    }

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