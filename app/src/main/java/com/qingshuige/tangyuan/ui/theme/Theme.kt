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

private val SpringFestivalLightColorScheme = lightColorScheme(
    primary = TangyuanFestivalColors.PrimaryLight,
    secondary = TangyuanFestivalColors.SecondaryLight,
    tertiary = TangyuanFestivalColors.TertiaryLight,
    onPrimary = TangyuanFestivalColors.OnPrimaryLight,
    onSecondary = TangyuanFestivalColors.OnSecondaryLight,
    onTertiary = TangyuanFestivalColors.OnTertiaryLight,
    primaryContainer = TangyuanFestivalColors.PrimaryContainerLight,
    secondaryContainer = TangyuanFestivalColors.SecondaryContainerLight,
    tertiaryContainer = TangyuanFestivalColors.TertiaryContainerLight,
    onPrimaryContainer = TangyuanFestivalColors.OnPrimaryContainerLight,
    onSecondaryContainer = TangyuanFestivalColors.OnSecondaryContainerLight,
    onTertiaryContainer = TangyuanFestivalColors.OnTertiaryContainerLight,
    error = TangyuanFestivalColors.ErrorLight,
    onError = TangyuanFestivalColors.OnErrorLight,
    errorContainer = TangyuanFestivalColors.ErrorContainerLight,
    onErrorContainer = TangyuanFestivalColors.OnErrorContainerLight,
    background = TangyuanFestivalColors.BackgroundLight,
    onBackground = TangyuanFestivalColors.OnBackgroundLight,
    surface = TangyuanFestivalColors.SurfaceLight,
    onSurface = TangyuanFestivalColors.OnSurfaceLight,
    surfaceVariant = TangyuanFestivalColors.SurfaceVariantLight,
    onSurfaceVariant = TangyuanFestivalColors.OnSurfaceVariantLight,
    outline = TangyuanFestivalColors.OutlineLight,
)

private val SpringFestivalDarkColorScheme = darkColorScheme(
    primary = TangyuanFestivalColors.PrimaryDark,
    secondary = TangyuanFestivalColors.SecondaryDark,
    tertiary = TangyuanFestivalColors.TertiaryDark,
    onPrimary = TangyuanFestivalColors.OnPrimaryDark,
    onSecondary = TangyuanFestivalColors.OnSecondaryDark,
    onTertiary = TangyuanFestivalColors.OnTertiaryDark,
    primaryContainer = TangyuanFestivalColors.PrimaryContainerDark,
    secondaryContainer = TangyuanFestivalColors.SecondaryContainerDark,
    tertiaryContainer = TangyuanFestivalColors.TertiaryContainerDark,
    onPrimaryContainer = TangyuanFestivalColors.OnPrimaryContainerDark,
    onSecondaryContainer = TangyuanFestivalColors.OnSecondaryContainerDark,
    onTertiaryContainer = TangyuanFestivalColors.OnTertiaryContainerDark,
    error = TangyuanFestivalColors.ErrorDark,
    onError = TangyuanFestivalColors.OnErrorDark,
    errorContainer = TangyuanFestivalColors.ErrorContainerDark,
    onErrorContainer = TangyuanFestivalColors.OnErrorContainerDark,
    background = TangyuanFestivalColors.BackgroundDark,
    onBackground = TangyuanFestivalColors.OnBackgroundDark,
    surface = TangyuanFestivalColors.SurfaceDark,
    onSurface = TangyuanFestivalColors.OnSurfaceDark,
    surfaceVariant = TangyuanFestivalColors.SurfaceVariantDark,
    onSurfaceVariant = TangyuanFestivalColors.OnSurfaceVariantDark,
    outline = TangyuanFestivalColors.OutlineDark,
)

@Composable
fun TangyuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: AppThemeMode = AppThemeMode.DEFAULT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        themeMode == AppThemeMode.SPRING_FESTIVAL && darkTheme -> SpringFestivalDarkColorScheme
        themeMode == AppThemeMode.SPRING_FESTIVAL -> SpringFestivalLightColorScheme
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
