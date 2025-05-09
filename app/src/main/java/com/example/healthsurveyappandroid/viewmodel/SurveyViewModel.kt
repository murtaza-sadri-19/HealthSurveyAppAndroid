package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.repository.SurveyRepository

class SurveyViewModel(private val repository: SurveyRepository) : ViewModel() {

    private val _surveys = MutableStateFlow<List<Survey>>(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchSurveys() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.getSurveys()
                _surveys.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun submitSurvey(survey: Survey) {
        viewModelScope.launch {
            try {
                repository.submitSurvey(survey)
                fetchSurveys() // Optionally refresh after submit
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}