package com.example.healthsurveyappandroid.data

import com.android.identity.util.UUID

data class Survey(
    val registrationId: String = UUID.randomUUID().toString().take(8),
    val surveyDateTime: String = "",
    val surveyTakerId: String = "",

    // Personal Details
    val name: String = "",
    val fathersName: String = "",
    val age: String = "",
    val gender: String = "",
    val samagraId: String = "",
    val highestEducation: String = "",

    // Health Details
    val bloodGroup: String = "",
    val immunizationStatus: String = "",
    val disease: String = "",
    val remarks: String = "",

    // Address Details
    val state: String = "",
    val city: String = "",
    val pinCode: String = "",
    val permanentAddress: String = "",
    val temporaryAddress: String = "",

    // Document Upload
    val photoUrl: String = "",
    val samagraIdPhotoUrl: String = ""
)
