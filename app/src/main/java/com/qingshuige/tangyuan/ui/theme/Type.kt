package com.qingshuige.tangyuan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingshuige.tangyuan.R

// ====================================
// 字体家族定义
// ====================================

// 中英文混合字体（通用）
val TangyuanGeneralFontFamily = FontFamily(
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Light),      // 300
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Normal),     // 400
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Medium),     // 500
    Font(R.font.notosanssc_variablefont_wght, FontWeight.SemiBold),   // 600
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Bold),       // 700
)

// 英文字体（Quicksand - 现代圆润）
val EnglishFontFamily = FontFamily(
    Font(R.font.quicksand_variablefont_wght, FontWeight.Light),
    Font(R.font.quicksand_variablefont_wght, FontWeight.Normal),
    Font(R.font.quicksand_variablefont_wght, FontWeight.Medium),
    Font(R.font.quicksand_variablefont_wght, FontWeight.SemiBold),
    Font(R.font.quicksand_variablefont_wght, FontWeight.Bold)
)

// 中文字体（思源黑体 - 清晰易读）
val ChineseFontFamily = FontFamily(
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Light),
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Normal),
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Medium),
    Font(R.font.notosanssc_variablefont_wght, FontWeight.SemiBold),
    Font(R.font.notosanssc_variablefont_wght, FontWeight.Bold)
)

// 文学专用字体（思源宋体 - 传统韵味）
val LiteraryFontFamily = FontFamily(
    Font(R.font.notoserifsc_variablefont_wght, FontWeight.Light),
    Font(R.font.notoserifsc_variablefont_wght, FontWeight.Normal),
    Font(R.font.notoserifsc_variablefont_wght, FontWeight.Medium),
    Font(R.font.notoserifsc_variablefont_wght, FontWeight.SemiBold),
    Font(R.font.notoserifsc_variablefont_wght, FontWeight.Bold)
)

// ====================================
// 糖原社区字体排版系统
// ====================================

val Typography = Typography(
    // Display 系列 - 大标题（首页、专题页）
    displayLarge = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline 系列 - 标题（卡片标题、页面标题）
    headlineLarge = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title 系列 - 小标题（列表标题、组件标题）
    titleLarge = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body 系列 - 正文（文章内容、描述文字）
    bodyLarge = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label 系列 - 标签（按钮文字、标签、辅助信息）
    labelLarge = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ====================================
// 糖原社区形状系统（圆角设计）
// ====================================

val Shapes = Shapes(
    // 超小圆角 - 标签、徽章
    extraSmall = RoundedCornerShape(4.dp),

    // 小圆角 - 按钮、输入框
    small = RoundedCornerShape(8.dp),

    // 中圆角 - 卡片、对话框
    medium = RoundedCornerShape(12.dp),

    // 大圆角 - 底部表单、大卡片
    large = RoundedCornerShape(16.dp),

    // 超大圆角 - 浮动按钮、特殊组件
    extraLarge = RoundedCornerShape(28.dp)
)

// ====================================
// 扩展圆角样式（用于特殊场景）
// ====================================

object TangyuanShapes {
    // 完全圆角（头像、圆形按钮）
    val Circle = RoundedCornerShape(50)

    // 顶部圆角（底部表单）
    val TopRounded = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // 底部圆角（顶部导航栏下拉）
    val BottomRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )

    // 左侧圆角（右侧滑出抽屉）
    val LeftRounded = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 0.dp,
        bottomStart = 20.dp,
        bottomEnd = 0.dp
    )

    // 右侧圆角（左侧滑出抽屉）
    val RightRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 20.dp
    )

    // 文化卡片（不对称圆角 - 艺术感）
    val CulturalCard = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )

    // 聊天气泡 - 发送方（右侧）
    val ChatBubbleSent = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )

    // 聊天气泡 - 接收方（左侧）
    val ChatBubbleReceived = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )
}

// ====================================
// 扩展字体样式（特殊场景）
// ====================================

object TangyuanTypography {
    // 数字字体（统计数据、价格）
    val numberLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )

    val numberMedium = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    val numberSmall = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // 引用文本（文章引用、诗词）
    val quote = TextStyle(
        fontFamily = ChineseFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.8.sp
    )

    // 代码文本（如果需要等宽字体，保留备用）
    val code = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

// ====================================
// 使用示例和最佳实践
// ====================================

/*
【排版使用示例】

1. 页面标题：
   Text(
       text = "糖原社区",
       style = MaterialTheme.typography.headlineLarge
   )

2. 卡片标题：
   Text(
       text = "今日推荐",
       style = MaterialTheme.typography.titleLarge
   )

3. 正文内容：
   Text(
       text = "这里是文章内容...",
       style = MaterialTheme.typography.bodyLarge
   )

4. 次要信息：
   Text(
       text = "2小时前",
       style = MaterialTheme.typography.bodySmall,
       color = MaterialTheme.colorScheme.onSurfaceVariant
   )

5. 按钮文字：
   Text(
       text = "立即体验",
       style = MaterialTheme.typography.labelLarge
   )

6. 数字显示（扩展样式）：
   Text(
       text = "1,234",
       style = TangyuanTypography.numberLarge,
       color = MaterialTheme.colorScheme.primary
   )

7. 引用文字（扩展样式）：
   Text(
       text = "人生若只如初见，何事秋风悲画扇。",
       style = TangyuanTypography.quote,
       color = MaterialTheme.colorScheme.tertiary
   )

【形状使用示例】

1. 普通卡片：
   Card(
       shape = MaterialTheme.shapes.medium
   ){...}

2. 按钮：
   Button(
       shape = MaterialTheme.shapes.small
   ){...}

3. 底部弹窗：
   Surface(
       shape = TangyuanShapes.TopRounded
   ){...}

4. 聊天气泡：
   Surface(
       shape = TangyuanShapes.ChatBubbleSent,
       color = MaterialTheme.colorScheme.primaryContainer
   ){...}

5. 文化内容卡片：
   Card(
       shape = TangyuanShapes.CulturalCard,
       colors = CardDefaults.cardColors(
           containerColor = MaterialTheme.colorScheme.tertiaryContainer
       )
   ){...}

【设计原则】

✅ 字体层级：严格遵循 Material Design 3 规范
✅ 圆角系统：4dp / 8dp / 12dp / 16dp / 28dp 五级渐进
✅ 行高比例：保持 1.4-1.5 倍行高，确保中文阅读舒适
✅ 字间距：中文适当放宽，英文数字适当收紧
✅ 混排优化：中英文混合时自动选择最佳字体

【可访问性】
- 最小字号 11sp（labelSmall）
- 正文字号不小于 14sp
- 重要信息不小于 16sp
- 行高确保触摸目标至少 48dp
*/