package com.example.healthsurveyappandroid.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.healthsurveyappandroid.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun ThemeToggle(themeViewModel: ThemeViewModel) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                themeViewModel.toggleTheme(!isDarkMode)
            }
        }
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
