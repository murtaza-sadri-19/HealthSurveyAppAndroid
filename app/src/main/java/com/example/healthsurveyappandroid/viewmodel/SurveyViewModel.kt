package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.repository.SurveyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class SurveyViewModel(private val repository: SurveyRepository) : ViewModel() {

    private val _surveys = MutableStateFlow<List<Survey>>(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentSurvey = MutableStateFlow(Survey())
    val currentSurvey: StateFlow<Survey> = _currentSurvey.asStateFlow()

    fun updateSurvey(updated: Survey) {
        _currentSurvey.value = updated
    }
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _samagraIdUri = MutableStateFlow<Uri?>(null)
    val samagraIdUri: StateFlow<Uri?> = _samagraIdUri.asStateFlow()

    fun setPhotoUris(photo: Uri?, samagra: Uri?) {
        _photoUri.value = photo
        _samagraIdUri.value = samagra
    }
    private val _survey = mutableStateOf(Survey())
    val survey: State<Survey> = _survey

    fun initializeSurvey() {
        _survey.value = Survey() // this generates new UUID each time
    }

    fun loadSurveys() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val surveyList = repository.getSurveys()
                _surveys.value = surveyList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitSurvey() {
        viewModelScope.launch {
            try {
                repository.submitSurvey(_currentSurvey.value)
                loadSurveys()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    class Factory(private val repository: SurveyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
                return SurveyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
