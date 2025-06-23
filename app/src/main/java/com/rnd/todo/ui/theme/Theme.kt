package com.rnd.todo.ui.theme

import android.app.Activity
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

val AppPrimaryBlue = Color(0xFF004AAD)
val AppPrimaryBlueDark = Color(0xFF003380) // <<< MAKE SURE THIS LINE EXISTS AND IS CORRECT
val AppOnPrimary = Color.White

val AppSecondaryBlue = Color(0xFF007AB8)
val AppOnSecondary = Color.White

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryBlueDark,
    onPrimary = AppOnPrimary,
    secondary = AppSecondaryBlue,
    onSecondary = AppOnSecondary,
    tertiary = Pink80,
    background = AppPrimaryBlueDark, // Set dark theme background to a dark version of your primary
    surface = AppPrimaryBlueDark,    // Set dark theme surface to a dark version of your primary
    onBackground = AppOnPrimary,     // Text on dark primary background
    onSurface = AppOnPrimary

)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimaryBlue,
    onPrimary = AppOnPrimary,
    secondary = AppSecondaryBlue,
    onSecondary = AppOnSecondary,
    tertiary = Pink40,
    background = AppPrimaryBlue, // Set light theme background to your primary color
    surface = AppPrimaryBlue,    // Set light theme surface to your primary color
    onBackground = AppOnPrimary, // Ensure text on this background is readable (e.g., White)
    onSurface = AppOnPrimary     // Ensure text on this surface is readable
    /* Other default colors to override if needed */

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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