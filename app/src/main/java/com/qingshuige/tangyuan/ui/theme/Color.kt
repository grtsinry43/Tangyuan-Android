package com.qingshuige.tangyuan.ui.theme

import androidx.compose.ui.graphics.Color

// 糖原社区颜色系统 - 现代简洁·文化雅致
object TangyuanColors {
    // Light Theme Colors - 浅色主题（晴空蓝调）
    val PrimaryLight = Color(0xFF2E7CF6)  // 明亮天蓝 - 主品牌色
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val PrimaryContainerLight = Color(0xFFE3F2FF)  // 晴空浅蓝容器
    val OnPrimaryContainerLight = Color(0xFF001D35)

    val SecondaryLight = Color(0xFF5B9FFF)  // 次要亮蓝
    val OnSecondaryLight = Color(0xFFFFFFFF)
    val SecondaryContainerLight = Color(0xFFEBF4FF)
    val OnSecondaryContainerLight = Color(0xFF001B3D)

    val TertiaryLight = Color(0xFF7B68EE)  // 文化紫蓝（艺术感）
    val OnTertiaryLight = Color(0xFFFFFFFF)
    val TertiaryContainerLight = Color(0xFFEDE7FF)
    val OnTertiaryContainerLight = Color(0xFF23036A)

    val AccentLight = Color(0xFF00BCD4)  // 青蓝强调色
    val OnAccentLight = Color(0xFFFFFFFF)

    val ErrorLight = Color(0xFFFF5252)  // 温和红色
    val OnErrorLight = Color(0xFFFFFFFF)
    val ErrorContainerLight = Color(0xFFFFEBEE)
    val OnErrorContainerLight = Color(0xFF410002)

    val SuccessLight = Color(0xFF4CAF50)  // 成功绿色
    val WarningLight = Color(0xFFFF9800)  // 警告橙色

    val BackgroundLight = Color(0xFFFAFBFC)  // 极简浅灰背景
    val OnBackgroundLight = Color(0xFF1A1C1E)
    val SurfaceLight = Color(0xFFFFFFFF)  // 纯白卡片
    val OnSurfaceLight = Color(0xFF1A1C1E)

    val SurfaceVariantLight = Color(0xFFF5F7FA)  // 分割线浅灰
    val OnSurfaceVariantLight = Color(0xFF6B7280)

    val OutlineLight = Color(0xFFE5E7EB)  // 边框色
    val ShadowLight = Color(0x0D000000)  // 轻微阴影

    // Dark Theme Colors - 深色主题（夜空蓝调）
    val PrimaryDark = Color(0xFF5B9FFF)  // 深色模式明亮蓝
    val OnPrimaryDark = Color(0xFF001D35)
    val PrimaryContainerDark = Color(0xFF0B4BA3)
    val OnPrimaryContainerDark = Color(0xFFE3F2FF)

    val SecondaryDark = Color(0xFF7CB5FF)
    val OnSecondaryDark = Color(0xFF001B3D)
    val SecondaryContainerDark = Color(0xFF0D3D6E)
    val OnSecondaryContainerDark = Color(0xFFEBF4FF)

    val TertiaryDark = Color(0xFF9B8AFF)  // 深色模式文化紫
    val OnTertiaryDark = Color(0xFF23036A)
    val TertiaryContainerDark = Color(0xFF3E2D7A)
    val OnTertiaryContainerDark = Color(0xFFEDE7FF)

    val AccentDark = Color(0xFF26C6DA)  // 深色强调色
    val OnAccentDark = Color(0xFF00363D)

    val ErrorDark = Color(0xFFFF6B6B)
    val OnErrorDark = Color(0xFF410002)
    val ErrorContainerDark = Color(0xFF8C1D18)
    val OnErrorContainerDark = Color(0xFFFFEBEE)

    val SuccessDark = Color(0xFF66BB6A)
    val WarningDark = Color(0xFFFFB74D)

    val BackgroundDark = Color(0xFF0F1419)  // 深邃夜空背景
    val OnBackgroundDark = Color(0xFFE4E6E9)
    val SurfaceDark = Color(0xFF1A1F29)  // 深色卡片
    val OnSurfaceDark = Color(0xFFE4E6E9)

    val SurfaceVariantDark = Color(0xFF141820)
    val OnSurfaceVariantDark = Color(0xFF9CA3AF)

    val OutlineDark = Color(0xFF2D3748)  // 深色边框
    val ShadowDark = Color(0x1A000000)  // 更深阴影
}

// 语义化颜色扩展
object TangyuanSemanticColors {
    // 互动状态色（浅色）
    val InteractiveLightHover = Color(0xFFF0F7FF)
    val InteractiveLightPressed = Color(0xFFD6EBFF)
    val InteractiveLightDisabled = Color(0xFFE5E7EB)

    // 互动状态色（深色）
    val InteractiveDarkHover = Color(0xFF1E3A5F)
    val InteractiveDarkPressed = Color(0xFF0D2847)
    val InteractiveDarkDisabled = Color(0xFF374151)

    // 信息层级色（浅色）
    val TextPrimaryLight = Color(0xFF1A1C1E)
    val TextSecondaryLight = Color(0xFF4B5563)
    val TextTertiaryLight = Color(0xFF9CA3AF)
    val TextDisabledLight = Color(0xFFD1D5DB)

    // 信息层级色（深色）
    val TextPrimaryDark = Color(0xFFE4E6E9)
    val TextSecondaryDark = Color(0xFFB4B8BE)
    val TextTertiaryDark = Color(0xFF6B7280)
    val TextDisabledDark = Color(0xFF4B5563)

    // 功能色
    val LinkBlue = Color(0xFF2E7CF6)
    val HighlightYellow = Color(0xFFFFF9E6)
    val DividerLight = Color(0xFFF0F0F0)
    val DividerDark = Color(0xFF2D3748)
}

// 渐变色方案（用于特殊场景）
object TangyuanGradients {
    // 主题渐变（浅色）
    val PrimaryGradientLight = listOf(
        Color(0xFF2E7CF6),
        Color(0xFF5B9FFF)
    )

    // 主题渐变（深色）
    val PrimaryGradientDark = listOf(
        Color(0xFF0B4BA3),
        Color(0xFF2E7CF6)
    )

    // 文化艺术渐变
    val CulturalGradient = listOf(
        Color(0xFF7B68EE),
        Color(0xFF5B9FFF)
    )

    // 高级灰渐变背景
    val SurfaceGradientLight = listOf(
        Color(0xFFFAFBFC),
        Color(0xFFF5F7FA)
    )

    val SurfaceGradientDark = listOf(
        Color(0xFF0F1419),
        Color(0xFF1A1F29)
    )
}

// 使用示例
/*
使用建议：
1. 主要操作按钮使用 PrimaryLight/Dark
2. 卡片背景使用 SurfaceLight/Dark
3. 页面背景使用 BackgroundLight/Dark
4. 文化内容区域可使用 TertiaryLight/Dark 或 CulturalGradient
5. 强调元素使用 AccentLight/Dark
6. 分割线使用 OutlineLight/Dark 或 DividerLight/Dark
7. 悬停效果使用 InteractiveLightHover/DarkHover
*/