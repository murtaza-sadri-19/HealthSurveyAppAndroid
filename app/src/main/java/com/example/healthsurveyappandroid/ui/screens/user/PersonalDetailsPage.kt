package com.example.healthsurveyappandroid.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.healthsurveyappandroid.data.Survey
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PersonalDetailsPage(survey: Survey, onNext: (Survey) -> Unit) {
    var name by remember { mutableStateOf(survey.name) }
    var fathersName by remember { mutableStateOf(survey.fathersName) }
    var age by remember { mutableStateOf(survey.age) }
    var gender by remember { mutableStateOf(survey.gender) }
    var samagraId by remember { mutableStateOf(survey.samagraId) }
    var highestEducation by remember { mutableStateOf(survey.highestEducation) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = fathersName, onValueChange = { fathersName = it }, label = { Text("Father's Name") })
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") })
        OutlinedTextField(value = samagraId, onValueChange = { samagraId = it }, label = { Text("Samagra ID") })
        OutlinedTextField(value = highestEducation, onValueChange = { highestEducation = it }, label = { Text("Highest Education") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            onNext(survey.copy(
                name = name,
                fathersName = fathersName,
                age = age,
                gender = gender,
                samagraId = samagraId,
                highestEducation = highestEducation
            ))
        }) {
            Text("Next")
        }
    }
}
