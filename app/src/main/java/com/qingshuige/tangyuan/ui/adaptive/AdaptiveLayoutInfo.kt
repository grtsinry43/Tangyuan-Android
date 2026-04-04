package com.qingshuige.tangyuan.ui.adaptive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

@Immutable
data class TangyuanLayoutInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
) {
    /** 宽屏（平板/折叠屏）：使用 NavigationRail + 双栏布局 */
    val useNavigationRail: Boolean
        get() = widthSizeClass != WindowWidthSizeClass.COMPACT

    /** 宽屏时显示双栏（列表+详情） */
    val showDualPane: Boolean
        get() = useNavigationRail

    /** 纵向空间紧张（手机横屏）：隐藏 TopBar */
    val isVerticallyConstrained: Boolean
        get() = heightSizeClass == WindowHeightSizeClass.COMPACT
}

/** 默认竖屏手机布局 */
val LocalTangyuanLayoutInfo = compositionLocalOf {
    TangyuanLayoutInfo(
        widthSizeClass = WindowWidthSizeClass.COMPACT,
        heightSizeClass = WindowHeightSizeClass.MEDIUM
    )
}

/** 导航栏占用的内边距，替代硬编码的 108.dp / 84.dp */
val LocalChromeInsets = compositionLocalOf {
    PaddingValues(top = 108.dp, bottom = 84.dp)
}

@Composable
fun rememberTangyuanLayoutInfo(): TangyuanLayoutInfo {
    val windowInfo = currentWindowAdaptiveInfo()
    val widthClass = windowInfo.windowSizeClass.windowWidthSizeClass
    val heightClass = windowInfo.windowSizeClass.windowHeightSizeClass
    return remember(widthClass, heightClass) {
        TangyuanLayoutInfo(widthSizeClass = widthClass, heightSizeClass = heightClass)
    }
}

/** 根据布局模式计算导航栏内边距 */
fun computeChromeInsets(layoutInfo: TangyuanLayoutInfo): PaddingValues {
    val topPadding = when {
        layoutInfo.isVerticallyConstrained -> 0.dp  // 横屏隐藏 TopBar
        layoutInfo.showDualPane -> 64.dp             // 双栏模式 TopBar 较小
        else -> 108.dp                               // 竖屏手机默认
    }
    val bottomPadding = when {
        layoutInfo.useNavigationRail -> 0.dp  // Rail 模式无 BottomBar
        else -> 84.dp                         // 手机 BottomBar
    }
    return PaddingValues(top = topPadding, bottom = bottomPadding)
}
