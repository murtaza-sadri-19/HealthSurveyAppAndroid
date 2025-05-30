package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.data.Survey
import com.example.healthsurveyappandroid.viewmodel.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressDetailsPage(
    survey: Survey,
    onNext: (Survey) -> Unit,
    onBack: () -> Unit,
    viewModel: SurveyViewModel
) {
    val indianStates = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
        "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
        "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )

    var state by remember { mutableStateOf(survey.state) }
    var city by remember { mutableStateOf(survey.city) }
    var pinCode by remember { mutableStateOf(survey.pinCode) }
    var permanentAddress by remember { mutableStateOf(survey.permanentAddress) }
    var temporaryAddress by remember { mutableStateOf(survey.temporaryAddress) }
    var stateError by remember { mutableStateOf<String?>(null) }
    var pinCodeError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // State Dropdown
        var stateExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = stateExpanded, onExpandedChange = { stateExpanded = !stateExpanded }) {
            OutlinedTextField(
                value = state,
                onValueChange = {},
                readOnly = true,
                label = { Text("State") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = stateError != null,
                supportingText = { if (stateError != null) Text(stateError!!, color = MaterialTheme.colorScheme.error) }
            )
            ExposedDropdownMenu(expanded = stateExpanded, onDismissRequest = { stateExpanded = false }) {
                indianStates.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        state = it
                        stateExpanded = false
                        stateError = null
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pinCode,
            onValueChange = {
                pinCode = it.filter { c -> c.isDigit() }.take(6)
                pinCodeError = if (pinCode.length != 6) "Pincode must be 6 digits" else null
            },
            label = { Text("Pin Code") },
            modifier = Modifier.fillMaxWidth(),
            isError = pinCodeError != null,
            supportingText = { if (pinCodeError != null) Text(pinCodeError!!, color = MaterialTheme.colorScheme.error) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = permanentAddress, onValueChange = { permanentAddress = it }, label = { Text("Permanent Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = temporaryAddress, onValueChange = { temporaryAddress = it }, label = { Text("Temporary Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = {
                stateError = if (state !in indianStates) "Select a state" else null
                pinCodeError = if (pinCode.length != 6) "Pincode must be 6 digits" else null
                if (stateError == null && pinCodeError == null) {
                    onNext(survey.copy(
                        state = state,
                        city = city,
                        pinCode = pinCode,
                        permanentAddress = permanentAddress,
                        temporaryAddress = temporaryAddress
                    ))
                }
            }) { Text("Next") }
        }
    }
}