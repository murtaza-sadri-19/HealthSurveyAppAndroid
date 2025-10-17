package com.example.healthsurveyappandroid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = HealthPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F5E4),
    onPrimaryContainer = Color(0xFF00210F),
    
    secondary = HealthSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6ECFF),
    onSecondaryContainer = Color(0xFF001D36),
    
    tertiary = HealthPrimary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD0F5E4),
    onTertiaryContainer = Color(0xFF00210F),
    
    error = HealthError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = HealthBackground,
    onBackground = TextPrimaryLight,
    
    surface = HealthSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = TextSecondaryLight,
    
    outline = CardBorderLight,
    outlineVariant = DividerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = HealthPrimaryDark,
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF005235),
    onPrimaryContainer = Color(0xFFB8F4D7),
    
    secondary = HealthSecondaryDark,
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF004A7C),
    onSecondaryContainer = Color(0xFFD6ECFF),
    
    tertiary = HealthPrimaryDark,
    onTertiary = Color(0xFF003823),
    tertiaryContainer = Color(0xFF005235),
    onTertiaryContainer = Color(0xFFB8F4D7),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = HealthBackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = HealthSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = HealthSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    outline = CardBorderDark,
    outlineVariant = DividerDark
)

@Composable
fun HealthSurveyAppAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent health theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}