package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@Composable
fun AddressDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit,
    onBack: () -> Unit,
    viewModel: SurveyViewModel
) {
    var state by remember { mutableStateOf(survey.state) }
    var city by remember { mutableStateOf(survey.city) }
    var pinCode by remember { mutableStateOf(survey.pinCode) }
    var permanentAddress by remember { mutableStateOf(survey.permanentAddress) }
    var temporaryAddress by remember { mutableStateOf(survey.temporaryAddress) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it },
            label = { Text("Pin Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = permanentAddress,
            onValueChange = { permanentAddress = it },
            label = { Text("Permanent Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = temporaryAddress,
            onValueChange = { temporaryAddress = it },
            label = { Text("Temporary Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    onNext(
                        survey.copy(
                            state = state,
                            city = city,
                            pinCode = pinCode,
                            permanentAddress = permanentAddress,
                            temporaryAddress = temporaryAddress
                        )
                    )
                }
            ) {
                Text("Next")
            }
        }
    }
}