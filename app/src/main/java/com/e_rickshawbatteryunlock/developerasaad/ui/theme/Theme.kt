package com.e_rickshawbatteryunlock.developerasaad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── Dark Colour Scheme (Primary) ─────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = ElectricTeal,
    onPrimary = OnPrimaryDark,
    primaryContainer = TealContainer,
    onPrimaryContainer = ElectricTeal,
    secondary = Amber,
    onSecondary = OnPrimaryDark,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Amber,
    error = ErrorRed,
    onError = OnErrorDark,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed,
    background = DeepNavy,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)

// ─── Light Colour Scheme (Secondary) ──────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = ElectricTeal,
    onPrimaryContainer = OnPrimaryDark,
    secondary = Amber,
    onSecondary = OnPrimaryDark,
    error = ErrorRed,
    onError = OnErrorDark,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

// ─── App Theme ────────────────────────────────────────────────────────────────

/**
 * Root composable theme for E-Rickshaw Battery Unlock.
 *
 * Dynamic color is disabled intentionally — the electric teal brand color
 * must remain consistent across all devices to convey reliability.
 *
 * @param darkTheme Defaults to system setting. Can be overridden for previews.
 * @param content The composable content to theme.
 */
@Composable
fun BatteryUnlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
