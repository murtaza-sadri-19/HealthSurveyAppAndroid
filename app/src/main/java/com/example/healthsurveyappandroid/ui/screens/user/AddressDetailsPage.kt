package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.healthsurveyappandroid.data.Survey
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AddressDetailsPage(survey: Survey, onNext: (Survey) -> Unit, onBack: () -> Unit) {
    var state by remember { mutableStateOf(survey.state) }
    var city by remember { mutableStateOf(survey.city) }
    var pinCode by remember { mutableStateOf(survey.pinCode) }
    var permanentAddress by remember { mutableStateOf(survey.permanentAddress) }
    var temporaryAddress by remember { mutableStateOf(survey.temporaryAddress) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") })
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
        OutlinedTextField(value = pinCode, onValueChange = { pinCode = it }, label = { Text("Pin Code") })
        OutlinedTextField(value = permanentAddress, onValueChange = { permanentAddress = it }, label = { Text("Permanent Address") })
        OutlinedTextField(value = temporaryAddress, onValueChange = { temporaryAddress = it }, label = { Text("Temporary Address") })

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = {
                onNext(survey.copy(
                    state = state,
                    city = city,
                    pinCode = pinCode,
                    permanentAddress = permanentAddress,
                    temporaryAddress = temporaryAddress
                ))
            }) {
                Text("Next")
            }
        }
    }
}