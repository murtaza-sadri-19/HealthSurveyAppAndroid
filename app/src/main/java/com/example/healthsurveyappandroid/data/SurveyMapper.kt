package com.example.healthsurveyappandroid.data

import com.example.healthsurveyappandroid.data.Survey

fun Survey.toEntity(): SurveyEntity {
    return SurveyEntity(
        registrationId = registrationId,
        surveyDateTime = surveyDateTime,
        surveyTakerId = surveyTakerId,
        name = name,
        fathersName = fathersName,
        age = age,
        gender = gender,
        samagraId = samagraId,
        highestEducation = highestEducation,
        bloodGroup = bloodGroup,
        immunizationStatus = immunizationStatus,
        disease = disease,
        remarks = remarks,
        state = state,
        city = city,
        district=district,
        pinCode = pinCode,
        permanentAddress = permanentAddress,
        temporaryAddress = temporaryAddress,
        gpsCoordinates = gpsCoordinates.toString(),
        photoUrl = photoUrl,
        samagraIdPhotoUrl = samagraIdPhotoUrl
    )
}

fun SurveyEntity.toSurvey(): Survey {
    return Survey(
        registrationId = registrationId,
        surveyDateTime = surveyDateTime,
        surveyTakerId = surveyTakerId,
        name = name,
        fathersName = fathersName,
        age = age,
        gender = gender,
        samagraId = samagraId,
        highestEducation = highestEducation,
        bloodGroup = bloodGroup,
        immunizationStatus = immunizationStatus,
        disease = disease,
        remarks = remarks,
        state = state,
        city = city,
        district=district,
        pinCode = pinCode,
        permanentAddress = permanentAddress,
        temporaryAddress = temporaryAddress,
        gpsCoordinates= gpsCoordinates,
        photoUrl = photoUrl,
        samagraIdPhotoUrl = samagraIdPhotoUrl
    )
}