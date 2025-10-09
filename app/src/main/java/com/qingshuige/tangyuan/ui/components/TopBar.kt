package com.qingshuige.tangyuan.ui.components

/**
 * // 一级页面使用示例
 *   TangyuanTopBar(
 *       currentScreen = Screen.Talk,
 *       pageLevel = PageLevel.PRIMARY,
 *       onAvatarClick = {/* 头像点击事件 */},
 *       onAnnouncementClick = {/* 公告点击事件 */},
 *       onPostClick = {/* 发表点击事件 */}
 *   )
 *
 *   // 二级页面使用示例
 *   TangyuanTopBar(
 *       currentScreen = Screen.Talk,
 *       pageLevel = PageLevel.SECONDARY,
 *       onBackClick = {/* 返回点击事件 */},
 *       onAnnouncementClick = {/* 公告点击事件 */},
 *       onPostClick = {/* 发表点击事件 */},
 *       onActionClick = {/* 操作按钮点击事件 */}
 *   )
 *
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.navigation.Screen
import com.qingshuige.tangyuan.network.TokenManager

// 定义页面层级类型
enum class PageLevel {
    PRIMARY,    // 一级页面
    SECONDARY   // 二级页面
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TangyuanTopBar(
    currentScreen: Screen,
    pageLevel: PageLevel,
    avatarUrl: String? = null,
    onBackClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onAnnouncementClick: (() -> Unit)? = null,
    onPostClick: ((sectionId: Int) -> Unit)? = null,
    onActionClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：头像或返回按钮
                when {
                    // 二级页面显示返回按钮
                    pageLevel == PageLevel.SECONDARY -> {
                        IconButton(onClick = { onBackClick?.invoke() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                    // 一级页面且不是我的页面显示头像或应用图标
                    pageLevel == PageLevel.PRIMARY -> {
                        val tokenManager = TokenManager()
                        val isLoggedIn = tokenManager.token != null
                        
                        IconButton(
                            onClick = { onAvatarClick?.invoke() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (isLoggedIn && avatarUrl != null) {
                                // 已登录且有头像URL，显示用户头像
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "头像",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onError = {error ->
                                        // 处理图片加载错误
                                        error.result.throwable.printStackTrace()
                                    },
                                    fallback = painterResource(R.drawable.ic_launcher_foreground),
                                    error = painterResource(R.drawable.ic_launcher_foreground)
                                )
                            } else {
                                // 未登录或没有头像URL，显示应用图标
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = if (isLoggedIn) "头像" else "应用图标",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    // 我的页面不显示左侧内容
                    else -> {
                        Box(modifier = Modifier.size(40.dp))
                    }
                }

                // 中间：页面标题
                Text(
                    text = currentScreen.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).
                        padding(start = 24.dp),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 右侧：公告、发表、操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 公告按钮（除我的页面一级页面都显示）
                    if (!(pageLevel == PageLevel.PRIMARY && currentScreen == Screen.User)) {
                        IconButton(onClick = { onAnnouncementClick?.invoke() }) {
                            Icon(
                                Icons.Filled.Campaign,
                                contentDescription = "公告",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 发表按钮（除我的一级页面都显示）
                    if (!(pageLevel == PageLevel.PRIMARY && currentScreen == Screen.User)) {
                        IconButton(onClick = { onPostClick?.invoke(if (currentScreen == Screen.Topic) 2 else 1) }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "发表",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 操作按钮（仅二级页面显示）
                    if (pageLevel == PageLevel.SECONDARY) {
                        IconButton(onClick = { onActionClick?.invoke() }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "更多操作",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 如果没有任何右侧按钮，添加占位空间保持布局平衡
                    if (pageLevel == PageLevel.PRIMARY && currentScreen == Screen.User) {
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun TangyuanTopBarPreview() {
    TangyuanTopBar(
        currentScreen = Screen.Talk,
        pageLevel = PageLevel.PRIMARY,
        avatarUrl = "https://dogeoss.grtsinry43.com/img/author.jpeg",
        onAvatarClick = {},
        onAnnouncementClick = {},
        onPostClick = {}
    )
}