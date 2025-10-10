package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.components.PostCardItem
import com.qingshuige.tangyuan.ui.components.ShimmerAsyncImage
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanTypography
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.UserDetailViewModel

/**
 * 用户详情页
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit = {},
    onImageClick: (postId: Int, imageIndex: Int) -> Unit = { _, _ -> },
    onFollowClick: () -> Unit = {},
    onCategoryClick: (Int) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    sharedElementPrefix: String? = null, // 从导航传递的前缀
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPostsLoading by viewModel.isPostsLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    // 延迟初始加载以避免阻塞共享元素动画
    LaunchedEffect(userId) {
        kotlinx.coroutines.delay(200) // 等待共享元素动画完成
        viewModel.loadUserDetails(userId)
    }

    // 错误提示
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示错误提示
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            UserDetailTopBar(
                onBackClick = onBackClick,
                userName = user?.nickName ?: "",
                isLoading = isLoading
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refreshUserData(userId)
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 用户信息区域
                item {
                    if (isLoading) {
                        UserProfileLoadingState()
                    } else {
                        user?.let { userInfo ->
                            UserProfileSection(
                                user = userInfo,
                                onFollowClick = onFollowClick,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedContentScope = animatedContentScope,
                                sharedElementPrefix = sharedElementPrefix
                            )
                        }
                    }
                }

                // 统计信息
//                item {
//                    user?.let { userInfo ->
//                        UserStatsSection(
//                            postsCount = userPosts.size
//                        )
//                    }
//                }

                // 用户帖子信息流
                user?.let { userInfo ->
                    item {
                        PostsSectionHeader(postsCount = userPosts.size)
                    }

                    if (isPostsLoading && userPosts.isEmpty()) {
                        item {
                            PostsLoadingState()
                        }
                    } else if (userPosts.isEmpty() && !isPostsLoading) {
                        item {
                            EmptyPostsState()
                        }
                    } else {
                        // 使用PostCardItem展示完整帖子信息流
                        items(
                            items = userPosts,
                            key = { it.postId }
                        ) { postCard ->
                            PostCardItem(
                                postCard = postCard,
                                onPostClick = onPostClick,
                                onAuthorClick = { /* 已经在用户详情页，不需要跳转 */ },
                                onLikeClick = { /* TODO: 实现点赞 */ },
                                onCommentClick = { /* TODO: 实现评论 */ },
                                onShareClick = { /* TODO: 实现分享 */ },
                                onBookmarkClick = { /* TODO: 实现收藏 */ },
                                onMoreClick = { /* TODO: 实现更多操作 */ },
                                onImageClick = { postId, imageIndex ->
                                    onImageClick(postId, imageIndex)
                                },
                                onCategoryClick = onCategoryClick,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedContentScope = animatedContentScope,
                                sharedElementPrefix = "userdetail_post_${postCard.postId}"
                            )
                        }
                        
                        // 底部提示文字
                        if (userPosts.isNotEmpty()) {
                            item {
                                BottomIndicator()
                            }
                        }
                    }
                }
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
    userName: String,
    isLoading: Boolean = false
) {
    TopAppBar(
        title = {
            if (isLoading) {
                Text(
                    text = "用户详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = if (userName.isNotBlank()) "用户详情 · $userName" else "用户详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
 * 简洁的加载状态
 */
@Composable
private fun UserProfileLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载用户信息中...",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 无边界用户信息区域
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UserProfileSection(
    user: User,
    onFollowClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?,
    sharedElementPrefix: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // 头像和昵称区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧头像 - 支持共享元素动画
            ShimmerAsyncImage(
                imageUrl = "${TangyuanApplication.instance.bizDomain}images/${user.avatarGuid}.jpg",
                contentDescription = "${user.nickName}的头像",
                modifier = Modifier
                    .size(80.dp)
                    .let { mod ->
                        if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                mod.sharedElement(
                                    rememberSharedContentState(
                                        key = sharedElementPrefix?.let { "${it}_user_avatar_${user.userId}" }
                                            ?: "user_avatar_${user.userId}"
                                    ),
                                    animatedVisibilityScope = animatedContentScope,
                                    boundsTransform = { _, _ ->
                                        tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing
                                        )
                                    }
                                )
                            }
                        } else mod
                    }
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(20.dp))

            // 右侧昵称和信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 昵称 - 支持共享元素动画
                Text(
                    text = user.nickName.withPanguSpacing(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(
                                    key = sharedElementPrefix?.let { "${it}_user_name_${user.userId}" }
                                        ?: "user_name_${user.userId}"
                                ),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                }
                            )
                        }
                    } else Modifier
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 地区和邮箱信息 - 右侧划入动画
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(
                        animationSpec = tween(400, delayMillis = 100)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 地区信息胶囊
                        if (user.isoRegionName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = "地区",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
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
                        }

                        // 邮箱信息胶囊
                        if (!user.email.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = "邮箱",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = TangyuanGeneralFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 用户签名
        if (!user.bio.isNullOrBlank()) {
            Text(
                text = user.bio.withPanguSpacing(),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

//        // 关注按钮
//        Button(
//            onClick = onFollowClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(48.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primary
//            )
//        ) {
//            Icon(
//                imageVector = Icons.Outlined.PersonAdd,
//                contentDescription = null,
//                modifier = Modifier.size(18.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = "关注",
//                style = MaterialTheme.typography.labelLarge,
//                fontFamily = TangyuanGeneralFontFamily,
//                fontWeight = FontWeight.Medium
//            )
//        }
    }
}

/**
 * 统计信息区域
 */
@Composable
private fun UserStatsSection(
    postsCount: Int
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
                value = "0" // TODO: 从 API 获取关注数
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            StatItem(
                label = "粉丝",
                value = "0" // TODO: 从 API 获取粉丝数
            )
        }
    }
}

/**
 * 帖子区域标题
 */
@Composable
private fun PostsSectionHeader(postsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "帖子",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = TangyuanGeneralFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($postsCount)",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 帖子加载状态
 */
@Composable
private fun PostsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "加载帖子中...",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 空帖子状态
 */
@Composable
private fun EmptyPostsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.PostAdd,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有发布过帖子",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "期待 TA 的第一个分享",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * 底部到底提示
 */
@Composable
private fun BottomIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.4f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "已经到底了",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "去发现更多精彩吧",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
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




@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun UserProfilePreview() {
    val sampleUser = User(
        userId = 1,
        nickName = "示例用户",
        avatarGuid = "sample_avatar",
        bio = "这是一个示例用户的签名，用于展示用户详情卡片的样式。",
        email = "123@example.com",
        isoRegionName = "示例地区",
        phoneNumber = "+1234567890",
        password = "password",
    )
    UserProfileSection(
        user = sampleUser,
        onFollowClick = {},
        sharedTransitionScope = null,
        animatedContentScope = null
    )
}