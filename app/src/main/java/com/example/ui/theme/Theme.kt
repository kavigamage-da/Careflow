package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HealthcareTealDarkPrimary,
    onPrimary = HealthcareTealDarkOnPrimary,
    primaryContainer = HealthcareTealDarkContainer,
    onPrimaryContainer = HealthcareTealDarkOnContainer,
    secondary = HealthcareSlateDarkSecondary,
    onSecondary = HealthcareSlateDarkOnSecondary,
    secondaryContainer = HealthcareSlateDarkContainer,
    onSecondaryContainer = HealthcareSlateDarkOnContainer,
    background = HealthcareNeutralDarkBackground,
    onBackground = HealthcareNeutralDarkOnBackground,
    surface = HealthcareNeutralDarkSurface,
    onSurface = HealthcareNeutralDarkOnSurface,
    error = HealthcareError,
    onError = HealthcareOnError
)

private val LightColorScheme = lightColorScheme(
    primary = HealthcareTealPrimary,
    onPrimary = HealthcareTealOnPrimary,
    primaryContainer = HealthcareTealContainer,
    onPrimaryContainer = HealthcareTealOnContainer,
    secondary = HealthcareSlateSecondary,
    onSecondary = HealthcareSlateOnSecondary,
    secondaryContainer = HealthcareSlateContainer,
    onSecondaryContainer = HealthcareSlateOnContainer,
    tertiary = HealthcareEmeraldTertiary,
    onTertiary = HealthcareEmeraldOnTertiary,
    tertiaryContainer = HealthcareEmeraldContainer,
    onTertiaryContainer = HealthcareEmeraldOnContainer,
    background = HealthcareNeutralBackground,
    onBackground = HealthcareNeutralOnBackground,
    surface = HealthcareNeutralSurface,
    onSurface = HealthcareNeutralOnSurface,
    surfaceVariant = HealthcareNeutralSurfaceVariant,
    onSurfaceVariant = HealthcareNeutralOnSurfaceVariant,
    error = HealthcareError,
    onError = HealthcareOnError,
    errorContainer = HealthcareErrorContainer,
    onErrorContainer = HealthcareOnErrorContainer
)

@Composable
fun CareFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Healthcare branding preferred over OS wallpaper tint
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
