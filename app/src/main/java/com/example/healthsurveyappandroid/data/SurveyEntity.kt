package com.example.healthsurveyappandroid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surveys")
data class SurveyEntity(
    @PrimaryKey val registrationId: String,
    val surveyDateTime: String,
    val surveyTakerId: String,

    val name: String,
    val fathersName: String,
    val age: String,
    val gender: String,
    val samagraId: String,
    val highestEducation: String,

    val bloodGroup: String,
    val immunizationStatus: String,
    val disease: String,
    val remarks: String,

    val state: String,
    val city: String,
    val district:String,
    val pinCode: String,
    val permanentAddress: String,
    val temporaryAddress: String,
    val gpsCoordinates: String,

    val photoUrl: String,
    val samagraIdPhotoUrl: String
)
