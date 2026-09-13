package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF003733),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealContainerLight,
    secondary = SaffronLight,
    onSecondary = Color(0xFF451E00),
    secondaryContainer = SaffronContainerDark,
    onSecondaryContainer = SaffronContainerLight,
    tertiary = Color(0xFF818CF8),
    onTertiary = Color(0xFF1E1B4B),
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = NeutralDarkBorder,
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = TealDark,
    secondary = SaffronSecondary,
    onSecondary = Color.White,
    secondaryContainer = SaffronContainerLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = IndigoUpi,
    onTertiary = Color.White,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = NeutralLightBorder,
    error = RedExpense
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent fintech brand identity
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
