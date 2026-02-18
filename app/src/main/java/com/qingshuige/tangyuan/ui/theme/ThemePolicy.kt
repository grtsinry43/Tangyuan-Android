package com.qingshuige.tangyuan.ui.theme

import java.util.Calendar

object ThemePolicy {
    // 自动生效区间：2026-02-16 到 2026-03-03（含）
    private const val SPRING_FESTIVAL_START = 20260216
    private const val SPRING_FESTIVAL_END = 20260303

    fun resolveThemeMode(savedMode: AppThemeMode, userOverridden: Boolean): AppThemeMode {
        if (userOverridden) return savedMode
        return if (isInSpringFestivalWindow2026()) {
            AppThemeMode.SPRING_FESTIVAL
        } else {
            AppThemeMode.DEFAULT
        }
    }

    private fun isInSpringFestivalWindow2026(): Boolean {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        if (year != 2026) return false
        val dateInt = year * 10000 + month * 100 + day
        return dateInt in SPRING_FESTIVAL_START..SPRING_FESTIVAL_END
    }
}
