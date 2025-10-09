package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.NotificationViewModel
import com.qingshuige.tangyuan.viewmodel.NotificationWithUserAndComment
import com.qingshuige.tangyuan.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNotificationClick: (Int, String) -> Unit = { _, _ -> },
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val uiState by notificationViewModel.notificationUiState.collectAsState()
    val loginState by userViewModel.loginState.collectAsState()
    val userId = loginState.user?.userId

    // 初始加载通知
    LaunchedEffect(userId) {
        userId?.let {
            notificationViewModel.getAllNotifications(it)
        }
    }

    // 处理错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            kotlinx.coroutines.delay(3000)
            notificationViewModel.clearError()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = {
                userId?.let { notificationViewModel.refreshNotifications(it) }
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.notificationsWithData.isEmpty() -> {
                    LoadingContent()
                }

                !uiState.isLoading && uiState.notificationsWithData.isEmpty() -> {
                    EmptyNotificationsContent()
                }

                else -> {
                    NotificationList(
                        notificationsWithData = uiState.notificationsWithData,
                        onNotificationClick = { item ->
                            // 标记为已读
                            if (!item.notification.isRead) {
                                notificationViewModel.markAsRead(item.notification.notificationId)
                            }
                            // 根据通知类型跳转到相应页面
                            when (item.notification.sourceType) {
                                "comment", "reply" -> {
                                    // 跳转到帖子详情
                                    item.postId?.let { postId ->
                                        onNotificationClick(postId, "post")
                                    }
                                }
                                "like" -> {
                                    // 跳转到帖子详情
                                    item.postId?.let { postId ->
                                        onNotificationClick(postId, "post")
                                    }
                                }
                                "follow" -> {
                                    // 跳转到用户详情
                                    item.user?.userId?.let { userId ->
                                        onNotificationClick(userId, "user")
                                    }
                                }
                                else -> { /* 忽略未知类型 */ }
                            }
                        }
                    )
                }
            }
        }

        // 错误提示
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyNotificationsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "暂无消息",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "暂无消息".withPanguSpacing(),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationList(
    notificationsWithData: List<NotificationWithUserAndComment>,
    onNotificationClick: (NotificationWithUserAndComment) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = notificationsWithData,
            key = { it.notification.notificationId }
        ) { item ->
            NotificationItem(
                item = item,
                onClick = { onNotificationClick(item) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun NotificationItem(
    item: NotificationWithUserAndComment,
    onClick: () -> Unit
) {
    val notification = item.notification
    val user = item.user

    val (icon, iconTint) = when (notification.type) {
        "comment" -> Icons.Default.Comment to MaterialTheme.colorScheme.primary
        "like" -> Icons.Default.Favorite to MaterialTheme.colorScheme.error
        "follow" -> Icons.Default.Person to MaterialTheme.colorScheme.tertiary
        else -> Icons.Default.Notifications to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val notificationText = when (notification.type) {
        "comment" -> "评论了您的帖子"
        "like" -> "点赞了您的帖子"
        "follow" -> "关注了您"
        "reply" -> "回复了您的评论"
        else -> "给您发送了一条通知"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.isRead)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 用户头像
        Box {
            AsyncImage(
                model = user?.let { "${TangyuanApplication.instance.bizDomain}images/${it.avatarGuid}.jpg" },
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher_foreground),
                fallback = painterResource(R.drawable.ic_launcher_foreground)
            )

            // 通知类型图标（小角标）
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(iconTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = notification.type,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (user?.nickName ?: "未知用户").withPanguSpacing(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = notificationText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDate(notification.createDate),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 未读标记
        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "未读",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(8.dp)
            )
        }
    }
}

private fun formatDate(date: java.util.Date?): String {
    if (date == null) return ""

    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}小时前"
        diff < 604800_000 -> "${diff / 86400_000}天前"
        else -> {
            val sdf = SimpleDateFormat("MM月dd日", Locale.CHINA)
            sdf.format(date)
        }
    }
}
