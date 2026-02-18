package com.qingshuige.tangyuan.ui.theme

enum class ThemeBackgroundLevel {
    PRIMARY,
    SECONDARY
}

object ThemeDecorAssets {
    fun backgroundAsset(mode: AppThemeMode, level: ThemeBackgroundLevel): String? {
        return when (mode) {
            AppThemeMode.SPRING_FESTIVAL -> when (level) {
                ThemeBackgroundLevel.PRIMARY -> "themes/spring_festival/backgrounds/chenyang1912-PZWa0yCbJHQ-unsplash.jpg"
                ThemeBackgroundLevel.SECONDARY -> "themes/spring_festival/backgrounds/sahil-pandita-bsDlcGg9Nh4-unsplash.jpg"
            }

            else -> null
        }
    }

    fun postDetailStickerAsset(mode: AppThemeMode): String? {
        return when (mode) {
            AppThemeMode.SPRING_FESTIVAL -> "themes/spring_festival/stickers/openclipart-vectors-lampion-152693_1280.png"
            else -> null
        }
    }
}
