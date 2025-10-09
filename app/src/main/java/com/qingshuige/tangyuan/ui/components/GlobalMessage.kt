package com.qingshuige.tangyuan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 消息类型枚举
 */
enum class MessageType {
    SUCCESS,    // 成功消息
    WARNING,    // 警告消息
    ERROR       // 错误消息
}

/**
 * 消息数据类
 */
data class Message(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val type: MessageType,
    val duration: Long = 3000L // 默认显示3秒
)

/**
 * 获取消息类型对应的图标
 */
private fun MessageType.getIcon(): ImageVector {
    return when (this) {
        MessageType.SUCCESS -> Icons.Filled.CheckCircle
        MessageType.WARNING -> Icons.Filled.Warning
        MessageType.ERROR -> Icons.Filled.Error
    }
}

/**
 * 获取消息类型对应的颜色
 */
@Composable
private fun MessageType.getContainerColor(): Color {
    return when (this) {
        MessageType.SUCCESS -> Color(0xFF4CAF50) // 绿色
        MessageType.WARNING -> Color(0xFFFF9800) // 橙色
        MessageType.ERROR -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun MessageType.getContentColor(): Color {
    return when (this) {
        MessageType.SUCCESS -> Color.White
        MessageType.WARNING -> Color.White
        MessageType.ERROR -> MaterialTheme.colorScheme.onError
    }
}

/**
 * 全局消息提示组件 - 简洁Toast风格
 * 底部居中显示，淡入淡出动画
 * 支持键盘适配，键盘展开时显示在键盘上方
 */
@Composable
fun GlobalMessageHost(
    message: Message?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    // 自动消失计时器
    LaunchedEffect(message) {
        if (message != null) {
            delay(message.duration)
            onDismiss()
        }
    }

    // 获取键盘高度
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val imeHeight = with(density) { imeInsets.getBottom(density).toDp() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { it / 3 }, // 从下方1/3位置滑入
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, // 中等回弹
                    stiffness = Spring.StiffnessMedium // 中等弹性
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            ),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp + imeHeight // 根据键盘高度动态调整底部间距
            )
        ) {
            message?.let {
                MessageItem(message = it)
            }
        }
    }
}

/**
 * 单个消息项组件 - 简洁样式
 */
@Composable
private fun MessageItem(message: Message) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = message.type.getContainerColor(),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = message.type.getIcon(),
                contentDescription = null,
                tint = message.type.getContentColor(),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = message.type.getContentColor()
            )
        }
    }
}
