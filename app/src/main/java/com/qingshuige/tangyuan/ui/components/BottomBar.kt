package com.qingshuige.tangyuan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingshuige.tangyuan.navigation.Screen

// 定义一个数据类来存储底部导航项所需的所有信息
private data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
)

@Composable
fun TangyuanBottomAppBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    // 定义底部导航栏的项目列表
    val items = listOf(
        BottomNavItem(Screen.Talk, Icons.Filled.ChatBubble),
        BottomNavItem(Screen.Topic, Icons.Filled.ListAlt),
        BottomNavItem(Screen.Message, Icons.Filled.Notifications),
        BottomNavItem(Screen.User, Icons.Filled.Person)
    )

    val topBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            // 绘制顶部的 1dp 分隔线
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        // 循环渲染所有的导航项
        items.forEach { item ->
            val isSelected = currentScreen == item.screen

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.screen) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.screen.title
                        )
                        Text(
                            item.screen.title,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                label = null
            )
        }
    }
}