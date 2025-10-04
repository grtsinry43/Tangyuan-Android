package com.qingshuige.tangyuan.ui.theme

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
    primary = TangyuanColors.PrimaryDark,
    secondary = TangyuanColors.SecondaryDark,
    tertiary = TangyuanColors.TertiaryDark,
    onPrimary = TangyuanColors.OnPrimaryDark,
    onSecondary = TangyuanColors.OnSecondaryDark,
    onTertiary = TangyuanColors.OnTertiaryDark,
    primaryContainer = TangyuanColors.PrimaryContainerDark,
    secondaryContainer = TangyuanColors.SecondaryContainerDark,
    tertiaryContainer = TangyuanColors.TertiaryContainerDark,
    onPrimaryContainer = TangyuanColors.OnPrimaryContainerDark,
    onSecondaryContainer = TangyuanColors.OnSecondaryContainerDark,
    onTertiaryContainer = TangyuanColors.OnTertiaryContainerDark,
    error = TangyuanColors.ErrorDark,
    onError = TangyuanColors.OnErrorDark,
    errorContainer = TangyuanColors.ErrorContainerDark,
    onErrorContainer = TangyuanColors.OnErrorContainerDark,
    background = TangyuanColors.BackgroundDark,
    onBackground = TangyuanColors.OnBackgroundDark,
    surface = TangyuanColors.SurfaceDark,
    onSurface = TangyuanColors.OnSurfaceDark,
    surfaceVariant = TangyuanColors.SurfaceVariantDark,
    onSurfaceVariant = TangyuanColors.OnSurfaceVariantDark,
    outline = TangyuanColors.OutlineDark,
)

private val LightColorScheme = lightColorScheme(
    primary = TangyuanColors.PrimaryLight,
    secondary = TangyuanColors.SecondaryLight,
    tertiary = TangyuanColors.TertiaryLight,
    onPrimary = TangyuanColors.OnPrimaryLight,
    onSecondary = TangyuanColors.OnSecondaryLight,
    onTertiary = TangyuanColors.OnTertiaryLight,
    primaryContainer = TangyuanColors.PrimaryContainerLight,
    secondaryContainer = TangyuanColors.SecondaryContainerLight,
    tertiaryContainer = TangyuanColors.TertiaryContainerLight,
    onPrimaryContainer = TangyuanColors.OnPrimaryContainerLight,
    onSecondaryContainer = TangyuanColors.OnSecondaryContainerLight,
    onTertiaryContainer = TangyuanColors.OnTertiaryContainerLight,
    error = TangyuanColors.ErrorLight,
    onError = TangyuanColors.OnErrorLight,
    errorContainer = TangyuanColors.ErrorContainerLight,
    onErrorContainer = TangyuanColors.OnErrorContainerLight,
    background = TangyuanColors.BackgroundLight,
    onBackground = TangyuanColors.OnBackgroundLight,
    surface = TangyuanColors.SurfaceLight,
    onSurface = TangyuanColors.OnSurfaceLight,
    surfaceVariant = TangyuanColors.SurfaceVariantLight,
    onSurfaceVariant = TangyuanColors.OnSurfaceVariantLight,
    outline = TangyuanColors.OutlineLight,
)

@Composable
fun TangyuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
        shapes = Shapes,
        content = content
    )
}