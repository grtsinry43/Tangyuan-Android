package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.ui.components.ShimmerAsyncImage
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.ui.theme.TangyuanTypography
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.UserDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户详情页
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit = {},
    onFollowClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPostsLoading by viewModel.isPostsLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    // 初始加载
    LaunchedEffect(userId) {
        viewModel.loadUserDetails(userId)
    }

    // 错误提示
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示错误提示
            viewModel.clearError()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshUserData(userId)
            isRefreshing = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 顶部导航栏
            item {
                UserDetailTopBar(
                    onBackClick = onBackClick,
                    userName = user?.nickName ?: ""
                )
            }

            // 用户信息卡片
            item {
                if (isLoading) {
                    UserDetailLoadingCard()
                } else {
                    user?.let { userInfo ->
                        UserDetailCard(
                            user = userInfo,
                            onFollowClick = onFollowClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope
                        )
                    }
                }
            }

            // 统计信息
            item {
                user?.let { userInfo ->
                    UserStatsSection(
                        postsCount = userPosts.size,
                        user = userInfo
                    )
                }
            }

            // 用户帖子列表
            item {
                PostsSection(
                    posts = userPosts,
                    isLoading = isPostsLoading,
                    onPostClick = onPostClick
                )
            }
        }
    }
}

/**
 * 顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailTopBar(
    onBackClick: () -> Unit,
    userName: String
) {
    TopAppBar(
        title = {
            Text(
                text = userName.withPanguSpacing(),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: 更多操作 */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

/**
 * 用户详情卡片
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UserDetailCard(
    user: User,
    onFollowClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = TangyuanShapes.CulturalCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户头像 - 支持共享元素动画
            ShimmerAsyncImage(
                imageUrl = "${TangyuanApplication.instance.bizDomain}images/${user.avatarGuid}.jpg",
                contentDescription = "${user.nickName}的头像",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .let { mod ->
                        if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                mod.sharedElement(
                                    rememberSharedContentState(key = "user_avatar_${user.userId}"),
                                    animatedVisibilityScope = animatedContentScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                    }
                                )
                            }
                        } else mod
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 用户名
            Text(
                text = user.nickName.withPanguSpacing(),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = TangyuanGeneralFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 用户简介
            if (user.bio.isNotBlank()) {
                Text(
                    text = user.bio.withPanguSpacing(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = LiteraryFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 地区信息
            if (user.isoRegionName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "地区",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.isoRegionName,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = TangyuanGeneralFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 关注按钮
            Button(
                onClick = onFollowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关注",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 统计信息区域
 */
@Composable
private fun UserStatsSection(
    postsCount: Int,
    user: User
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "帖子",
                value = postsCount.toString()
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            StatItem(
                label = "关注",
                value = "0" // TODO: 从API获取关注数
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            StatItem(
                label = "粉丝",
                value = "0" // TODO: 从API获取粉丝数
            )
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TangyuanTypography.numberMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 帖子列表区域
 */
@Composable
private fun PostsSection(
    posts: List<PostMetadata>,
    isLoading: Boolean,
    onPostClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "发布的帖子",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = TangyuanGeneralFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${posts.size}篇",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            // 加载状态
            repeat(3) {
                PostItemSkeleton()
            }
        } else if (posts.isEmpty()) {
            // 空状态
            EmptyPostsState()
        } else {
            // 帖子列表
            posts.forEach { post ->
                UserPostItem(
                    post = post,
                    onClick = { onPostClick(post.postId) }
                )
            }
        }
    }
}

/**
 * 用户帖子项
 */
@Composable
private fun UserPostItem(
    post: PostMetadata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "帖子 #${post.postId}",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatPostDate(post.postDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "查看详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 加载骨架屏
 */
@Composable
private fun PostItemSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyPostsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Article,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有发布任何帖子",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 用户详情加载卡片
 */
@Composable
private fun UserDetailLoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = TangyuanShapes.CulturalCard
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像占位
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户名占位
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 简介占位
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * 格式化帖子日期
 */
private fun formatPostDate(date: Date?): String {
    return date?.let {
        val formatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
        formatter.format(it)
    } ?: "未知时间"
}