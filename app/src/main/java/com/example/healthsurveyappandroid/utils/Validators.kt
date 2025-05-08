package com.example.healthsurveyappandroid.utils

object Validators {
    fun isAadhaarValid(aadhaar: String): Boolean =
        aadhaar.length == 12 && aadhaar.all { it.isDigit() }

    fun isAgeValid(age: Int): Boolean =
        age in 0..120

    fun isPincodeValid(pincode: String): Boolean =
        pincode.length == 6 && pincode.all { it.isDigit() }
}