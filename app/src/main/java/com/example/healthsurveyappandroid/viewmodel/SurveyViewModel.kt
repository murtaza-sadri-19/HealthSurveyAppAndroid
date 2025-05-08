package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.repository.SurveyRepository

class SurveyViewModel(private val repository: SurveyRepository) : ViewModel() {
    fun submitSurvey(survey: Survey) {
        viewModelScope.launch {
            repository.submitSurvey(survey)
        }
    }
}
