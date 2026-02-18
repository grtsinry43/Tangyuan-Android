package com.qingshuige.tangyuan.ui.theme

object ThemeAssets {
    fun manifestPath(mode: AppThemeMode): String = "${mode.assetsDir}/manifest.json"

    fun stickerPath(mode: AppThemeMode, fileName: String): String {
        return "${mode.assetsDir}/stickers/$fileName"
    }

    fun backgroundPath(mode: AppThemeMode, fileName: String): String {
        return "${mode.assetsDir}/backgrounds/$fileName"
    }
}
