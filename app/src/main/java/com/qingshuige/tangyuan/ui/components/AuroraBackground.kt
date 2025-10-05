package com.qingshuige.tangyuan.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    showRadialGradient: Boolean = true,
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mainProgress"
    )

    val secondaryProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondaryProgress"
    )

    // 颜色配置
    val backgroundColor = if (darkMode) Color(0xFF18181B) else Color(0xFFFAFAFA)
    val auroraColors = if (darkMode) {
        listOf(
            Color(0xFF3B82F6),
            Color(0xFFA5B4FC),
            Color(0xFF93C5FD),
            Color(0xFFDDD6FE),
            Color(0xFF60A5FA),
        )
    } else {
        listOf(
            Color(0xFF3B82F6),
            Color(0xFFA5B4FC),
            Color(0xFF93C5FD),
            Color(0xFFDDD6FE),
            Color(0xFF60A5FA),
        )
    }

    val maskColor = if (darkMode) Color(0xFF000000) else Color(0xFFFFFFFF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 第一层主 Aurora（斜向条纹）
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
        ) {
            val width = size.width
            val height = size.height

            // 计算倾斜角度（100度约等于弧度）
            val angleRad = Math.toRadians(100.0)
            val stripeWidth = width * 0.1f
            val totalWidth = width * 3f
            val offset = -totalWidth * animationProgress

            // 计算倾斜后的绘制范围
            val extraHeight = width * sin(angleRad).toFloat()

            for (i in 0..40) {
                val baseX = offset + i * stripeWidth

                // 绘制倾斜的矩形条纹
                val path = Path().apply {
                    moveTo(baseX, -extraHeight)
                    lineTo(baseX + stripeWidth * 5, -extraHeight)
                    lineTo(
                        baseX + stripeWidth * 5 + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    lineTo(
                        baseX + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = auroraColors.map { it.copy(alpha = 0.3f) },
                        start = Offset(baseX, 0f),
                        end = Offset(baseX + stripeWidth * 5, 0f)
                    )
                )
            }
        }

        // 第二层 Aurora（反向动画）
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
        ) {
            val width = size.width
            val height = size.height
            val angleRad = Math.toRadians(100.0)
            val stripeWidth = width * 0.12f
            val offset = width * 2.5f * secondaryProgress
            val extraHeight = width * sin(angleRad).toFloat()

            for (i in 0..35) {
                val baseX = offset + i * stripeWidth

                val path = Path().apply {
                    moveTo(baseX, -extraHeight)
                    lineTo(baseX + stripeWidth * 4, -extraHeight)
                    lineTo(
                        baseX + stripeWidth * 4 + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    lineTo(
                        baseX + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = auroraColors.reversed().map { it.copy(alpha = 0.25f) },
                        start = Offset(baseX, 0f),
                        end = Offset(baseX + stripeWidth * 4, 0f)
                    )
                )
            }
        }

        // 白色/黑色条纹遮罩层
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
        ) {
            val width = size.width
            val height = size.height
            val angleRad = Math.toRadians(100.0)
            val offset = -width * 2 * animationProgress * 0.6f
            val extraHeight = width * sin(angleRad).toFloat()

            for (i in 0..50) {
                val baseX = offset + i * (width * 0.07f)
                val phase = (i + animationProgress * 10) % 3
                val alpha = when {
                    phase < 1f -> 0.15f
                    phase < 2f -> 0.08f
                    else -> 0.12f
                }

                val path = Path().apply {
                    moveTo(baseX, -extraHeight)
                    lineTo(baseX + width * 0.05f, -extraHeight)
                    lineTo(
                        baseX + width * 0.05f + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    lineTo(
                        baseX + height * cos(angleRad).toFloat().coerceAtMost(0f),
                        height + extraHeight
                    )
                    close()
                }

                drawPath(
                    path = path,
                    color = maskColor,
                    alpha = alpha
                )
            }
        }

        // 柔和叠加层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.2f),
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.15f),
                        )
                    )
                )
        )

        // 径向遮罩层
        if (showRadialGradient) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.3f),
                            backgroundColor.copy(alpha = 0.7f),
                        ),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.9f
                    )
                )
            }
        }

        // 内容层
        content()
    }
}

@Preview
@Composable
fun AuroraBackgroundExample() {
    val darkMode = isSystemInDarkTheme()

    AuroraBackground(
        showRadialGradient = true,
        darkMode = darkMode
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Background\nlights are\ncool\nyou know.",
                style = MaterialTheme.typography.displayMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (darkMode) Color.White else Color(0xFF18181B),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Text(
                text = "And this, is chemical burn.",
                style = MaterialTheme.typography.titleLarge,
                color = if (darkMode) Color(0xFFA1A1AA) else Color(0xFF71717A)
            )

            Spacer(modifier = Modifier.height(32.dp))

            androidx.compose.material3.Button(
                onClick = { },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                androidx.compose.material3.Text(
                    "Debug now",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}