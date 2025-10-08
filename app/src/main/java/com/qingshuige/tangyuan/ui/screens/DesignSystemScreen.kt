package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingshuige.tangyuan.ui.theme.*
import com.qingshuige.tangyuan.utils.withPanguSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 设计系统预览页面
 *
 * 用于集中展示和测试 `TangyuanTheme` 中的颜色、排版和形状，
 * 方便设计师和开发者快速查阅和验证 UI 组件。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemScreen(
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tangyuan Design System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "其他",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                DesignTitleWithGuides()

                // 颜色系统
                DesignSection(title = "颜色系统 (Colors)") {
                    ColorSystemPreview()
                }

                // 排版系统
                DesignSection(title = "排版系统 (Typography)") {
                    TypographySystemPreview()
                }

                // 形状系统
                DesignSection(title = "形状系统 (Shapes)") {
                    ShapeSystemPreview()
                }
            }
        }
    }
}

@Composable
fun DesignTitleWithGuides() {
    // 状态变量，用于存储测量到的标题和副标题的布局坐标
    var titleCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var subtitleCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // 动画状态：lineProgress 控制线条划过，alpha 控制淡出
    val lineProgress = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    // 使用 LaunchedEffect 启动一次性动画
    LaunchedEffect(Unit) {
        // 启动一个协程来执行动画序列
        launch {
            // 1. 线条划入动画
            lineProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
            // 2. 短暂保持可见
            delay(200)
            // 3. 线条淡出动画
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 400))
        }
    }

    // 定义参考线的颜色
    val guideColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    Box(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        // Canvas 用于在文本背后绘制引导线
        Canvas(modifier = Modifier.matchParentSize()) {
            val titleLayout = titleCoords
            val subtitleLayout = subtitleCoords

            // 确保坐标已经被测量到才开始绘制
            if (titleLayout != null && subtitleLayout != null) {
                val animatedAlphaColor = guideColor.copy(alpha = alpha.value)

                // 计算整个标题区域的边界
                val left = 0f
                val top = 0f
                val right = titleLayout.size.width.toFloat()
                val bottom = subtitleLayout.positionInParent().y + subtitleLayout.size.height

                // 动画进度
                val progress = lineProgress.value

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)

                // 绘制四条动态参考线
                // 1. 从左到右的上边线
                drawLine(animatedAlphaColor, start = Offset(left, top), end = Offset(right * progress, top), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                // 2. 从左到右的下边线
                drawLine(animatedAlphaColor, start = Offset(left, bottom), end = Offset(right * progress, bottom), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                // 3. 从上到下的左边线
                drawLine(animatedAlphaColor, start = Offset(left, top), end = Offset(left, bottom * progress), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                // 4. 从上到下的右边线
                drawLine(animatedAlphaColor, start = Offset(right, top), end = Offset(right, bottom * progress), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
            }
        }

        // 实际的标题和副标题文本
        Column {
            Text(
                text = "糖原社区设计系统",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    titleCoords = coordinates
                }
            )
            Text(
                text = "现代简洁 · 文化雅致",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LiteraryFontFamily, // 保留文学字体
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    subtitleCoords = coordinates
                }
            )
        }
    }
}


/**
 * 设计系统的分区组件，包含标题和内容
 */
@Composable
private fun DesignSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/**
 * 颜色系统预览
 */
@Composable
private fun ColorSystemPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("主题色 (Light / Dark)", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ColorRoleItem("Primary", TangyuanColors.PrimaryLight, TangyuanColors.PrimaryDark)
            ColorRoleItem("Secondary", TangyuanColors.SecondaryLight, TangyuanColors.SecondaryDark)
            ColorRoleItem("Tertiary", TangyuanColors.TertiaryLight, TangyuanColors.TertiaryDark)
            ColorRoleItem("Accent", TangyuanColors.AccentLight, TangyuanColors.AccentDark)
        }
        Spacer(Modifier.height(8.dp))

        Text("功能色 (Success / Warning / Error)", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ColorFunctionItem("Success", TangyuanColors.SuccessLight, TangyuanColors.SuccessDark)
            ColorFunctionItem("Warning", TangyuanColors.WarningLight, TangyuanColors.WarningDark)
            ColorFunctionItem("Error", TangyuanColors.ErrorLight, TangyuanColors.ErrorDark)
        }
        Spacer(Modifier.height(8.dp))

        Text("界面基础色 (Background / Surface)", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SurfaceColorItem(
                "Background",
                TangyuanColors.BackgroundLight,
                TangyuanColors.BackgroundDark,
                TangyuanColors.OnBackgroundLight,
                TangyuanColors.OnBackgroundDark
            )
            SurfaceColorItem(
                "Surface",
                TangyuanColors.SurfaceLight,
                TangyuanColors.SurfaceDark,
                TangyuanColors.OnSurfaceLight,
                TangyuanColors.OnSurfaceDark
            )
        }
    }
}

@Composable
private fun ColorRoleItem(name: String, lightColor: Color, darkColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(lightColor)
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(darkColor)
            )
        }
        Text(name, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RowScope.ColorFunctionItem(name: String, lightColor: Color, darkColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(lightColor, darkColor)),
                    shape = MaterialTheme.shapes.small
                )
                .clip(MaterialTheme.shapes.small)
        )
        Text(
            name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun RowScope.SurfaceColorItem(
    name: String,
    lightBg: Color,
    darkBg: Color,
    lightContent: Color,
    darkContent: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(lightBg),
                contentAlignment = Alignment.Center
            ) {
                Text("Text", color = lightContent, style = MaterialTheme.typography.labelSmall)
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(darkBg),
                contentAlignment = Alignment.Center
            ) {
                Text("Text", color = darkContent, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(name, style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * 排版系统预览
 */
@Composable
private fun TypographySystemPreview() {
    val exampleText = "糖原社区 Tangyuan 2025"
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // M3 Type Scale
        TypographyItem("Headline Medium", exampleText, MaterialTheme.typography.headlineMedium)
        TypographyItem("Title Large", exampleText, MaterialTheme.typography.titleLarge)
        TypographyItem("Body Large", exampleText, MaterialTheme.typography.bodyLarge)
        TypographyItem("Label Large", exampleText, MaterialTheme.typography.labelLarge)

        // Font Weight Section
        Spacer(Modifier.height(16.dp))
        Text("字重 (Font Weights)", style = MaterialTheme.typography.titleMedium)
        FontWeightShowcase()

        // Chinese & Mixed Typography Section
        Spacer(Modifier.height(16.dp))
        Text("中英混排处理", style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("自动盘古之白 (Pangu Spacing)", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "在Tangyuan中使用Jetpack Compose构建UI。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "在Tangyuan中使用Jetpack Compose构建UI。".withPanguSpacing(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Special Purpose Fonts
        Spacer(Modifier.height(16.dp))
        Text("特殊用途字体", style = MaterialTheme.typography.titleMedium)

        // Literary Font
        TypographyItem(
            name = "文学字体 (Literary)",
            exampleText = "人生若只如初见",
            style = TextStyle(
                fontFamily = LiteraryFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.tertiary
        )

        // Other extended styles
        TypographyItem(
            "数字字体 (Number Large)",
            "1,234,567",
            TangyuanTypography.numberLarge,
            MaterialTheme.colorScheme.primary
        )
        TypographyItem(
            "代码字体 (Code)",
            "val name = \"Tangyuan\"",
            TangyuanTypography.code,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TypographyItem(
    name: String,
    exampleText: String,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = exampleText,
            style = style.copy(color = color),
            maxLines = 1
        )
        Text(
            text = "Font: ${getFontFamilyName(style.fontFamily)} | Size: ${style.fontSize.value.toInt()}sp | Weight: ${style.fontWeight?.weight}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FontWeightShowcase() {
    val weights = listOf(
        FontWeight.Normal to "Normal (400)",
        FontWeight.Medium to "Medium (500)",
        FontWeight.SemiBold to "SemiBold (600)",
        FontWeight.Bold to "Bold (700)"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weights.forEach { (weight, name) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = "线粒体 XianlitiCN",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = weight)
                )
            }
        }
    }
}

private fun getFontFamilyName(fontFamily: FontFamily?): String {
    return when (fontFamily) {
        TangyuanGeneralFontFamily -> "General (混排)"
        EnglishFontFamily -> "Quicksand"
        ChineseFontFamily -> "Noto Sans SC"
        LiteraryFontFamily -> "Noto Serif SC (文学)"
        FontFamily.Monospace -> "Monospace"
        else -> "Default"
    }
}

/**
 * 形状系统预览
 */
@Composable
private fun ShapeSystemPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Material Shapes", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeItem("Extra Small (4dp)", MaterialTheme.shapes.extraSmall)
            ShapeItem("Small (8dp)", MaterialTheme.shapes.small)
            ShapeItem("Medium (12dp)", MaterialTheme.shapes.medium)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeItem("Large (16dp)", MaterialTheme.shapes.large, Modifier.size(80.dp, 60.dp))
            ShapeItem(
                "Extra Large (28dp)",
                MaterialTheme.shapes.extraLarge,
                Modifier.size(80.dp, 60.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("扩展形状 (TangyuanShapes)", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeItem("Circle", TangyuanShapes.Circle)
            ShapeItem("Top Rounded", TangyuanShapes.TopRounded)
            ShapeItem("Cultural Card", TangyuanShapes.CulturalCard)
        }
    }
}

@Composable
private fun ShapeItem(name: String, shape: Shape, modifier: Modifier = Modifier.size(72.dp)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.primaryContainer, shape)
                .border(1.dp, MaterialTheme.colorScheme.primary, shape)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ====================================
// 预览
// ====================================
@Preview(showBackground = true, name = "Design System - Light Theme")
@Composable
fun DesignSystemScreenLightPreview() {
    TangyuanTheme(darkTheme = false) {
        Surface {
            DesignSystemScreen()
        }
    }
}

@Preview(showBackground = true, name = "Design System - Dark Theme")
@Composable
fun DesignSystemScreenDarkPreview() {
    TangyuanTheme(darkTheme = true) {
        Surface {
            DesignSystemScreen()
        }
    }
}