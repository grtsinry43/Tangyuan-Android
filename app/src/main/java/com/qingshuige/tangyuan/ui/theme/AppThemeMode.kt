package com.qingshuige.tangyuan.ui.theme

enum class AppThemeMode(
    val value: String,
    val assetsDir: String
) {
    DEFAULT("default", "themes/default"),
    SPRING_FESTIVAL("spring_festival", "themes/spring_festival");

    companion object {
        fun fromValue(value: String?): AppThemeMode {
            return entries.firstOrNull { it.value == value } ?: DEFAULT
        }
    }
}
