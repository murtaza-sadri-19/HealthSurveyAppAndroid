package com.example.healthsurveyappandroid.data

import com.android.identity.util.UUID

//data class Survey(
//    val registrationId: String,
//    val name: String,
//    val guardianName: String?,
//    val age: Int,
//    val sex: String,
//    val state: String,
//    val city: String,
//    val pincode: String,
//    val address: String,
//    val disease: String,
//    val bloodGroup: String,
//    val aadhaar: String,
//    val photoBase64: String?,
//    val educationLevel: String,
//    val schoolName: String?,
//    val nutritionStatus: Boolean,
//    val immunizationStatus: String,
//    val village: String,
//    val district: String,
//    val gpsLocation: String?,
//    val surveyDateTime: String,
//    val surveyTakerId: String,
//    val remarks: String?
//)
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
