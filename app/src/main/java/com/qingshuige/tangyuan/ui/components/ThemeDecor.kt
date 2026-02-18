package com.qingshuige.tangyuan.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.ui.theme.AppThemeMode
import com.qingshuige.tangyuan.ui.theme.ThemeBackgroundLevel
import com.qingshuige.tangyuan.ui.theme.ThemeDecorAssets

@Composable
fun ThemeBackgroundOverlay(
    themeMode: AppThemeMode,
    level: ThemeBackgroundLevel,
    alpha: Float = 0.08f
) {
    val assetPath = ThemeDecorAssets.backgroundAsset(themeMode, level) ?: return
    AsyncImage(
        model = "file:///android_asset/$assetPath",
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ThemeStickerOverlay(
    themeMode: AppThemeMode,
    modifier: Modifier,
    alpha: Float = 0.12f
) {
    val assetPath = ThemeDecorAssets.postDetailStickerAsset(themeMode) ?: return
    Box(modifier = modifier) {
        AsyncImage(
            model = "file:///android_asset/$assetPath",
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}
