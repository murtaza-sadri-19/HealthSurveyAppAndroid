package com.example.healthsurveyappandroid.data

data class Survey(
    val registrationId: String,
    val name: String,
    val guardianName: String?,
    val age: Int,
    val sex: String,
    val state: String,
    val city: String,
    val pincode: String,
    val address: String,
    val disease: String,
    val bloodGroup: String,
    val aadhaar: String,
    val photoBase64: String?,
    val educationLevel: String,
    val schoolName: String?,
    val nutritionStatus: Boolean,
    val immunizationStatus: String,
    val village: String,
    val district: String,
    val gpsLocation: String?,
    val surveyDateTime: String,
    val surveyTakerId: String,
    val remarks: String?
)