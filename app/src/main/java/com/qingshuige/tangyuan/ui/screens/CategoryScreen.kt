package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.components.PostCardItem
import com.qingshuige.tangyuan.ui.components.RollingNumber
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.viewmodel.CategoryViewModel

/**
 * 分类页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CategoryScreen(
    categoryId: Int,
    onBackClick: () -> Unit = {},
    onPostClick: (Int) -> Unit = {},
    onAuthorClick: (Int) -> Unit = {},
    onImageClick: (Int, Int) -> Unit = { _, _ -> },
    viewModel: CategoryViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by viewModel.categoryUiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(categoryId) {
        viewModel.loadCategoryDetail(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentCategory?.baseName ?: "分类",
                        fontFamily = TangyuanGeneralFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadCategoryDetail(categoryId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 加载状态
                uiState.isLoading && uiState.currentCategory == null -> {
                    LoadingContent()
                }

                // 错误状态
                uiState.error != null && uiState.currentCategory == null -> {
                    val errorMessage = uiState.error
                    ErrorContent(
                        message = errorMessage!!,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.loadCategoryDetail(categoryId)
                        }
                    )
                }

                // 正常内容
                uiState.currentCategory != null -> {
                    CategoryContent(
                        category = uiState.currentCategory!!,
                        stats = uiState.categoryStats,
                        posts = uiState.posts,
                        listState = listState,
                        onPostClick = onPostClick,
                        onAuthorClick = onAuthorClick,
                        onImageClick = onImageClick,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    )
                }
            }
        }
    }
}

/**
 * 分类内容
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CategoryContent(
    category: com.qingshuige.tangyuan.model.Category,
    stats: com.qingshuige.tangyuan.viewmodel.CategoryStats?,
    posts: List<PostCard>,
    listState: LazyListState,
    onPostClick: (Int) -> Unit,
    onAuthorClick: (Int) -> Unit,
    onImageClick: (Int, Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 分类头部信息
        item {
            CategoryHeader(
                category = category,
                stats = stats
            )
        }

        // 帖子列表
        items(
            items = posts,
            key = { it.postId }
        ) { postCard ->
            PostCardItem(
                postCard = postCard,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick,
                onLikeClick = {},
                onCommentClick = { onPostClick(it) },
                onShareClick = {},
                onBookmarkClick = {},
                onMoreClick = {},
                onImageClick = onImageClick,
                onCategoryClick = {}, // 在分类页面内，点击分类标签不需要跳转
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                sharedElementPrefix = "category_post_${postCard.postId}"
            )
        }

        // 空状态
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无帖子",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = LiteraryFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 分类头部
 */
@Composable
private fun CategoryHeader(
    category: com.qingshuige.tangyuan.model.Category,
    stats: com.qingshuige.tangyuan.viewmodel.CategoryStats?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 分类名称
        Text(
            text = category.baseName ?: "未知分类",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = LiteraryFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 分类描述
        if (!category.baseDescription.isNullOrBlank()) {
            Text(
                text = category.baseDescription,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 统计数据卡片
        if (stats != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 24小时新帖数
                    StatItem(
                        label = "24h 新帖",
                        count = stats.dailyNewCount
                    )

                    // 分隔线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                            )
                    )

                    // 7天新帖数
                    StatItem(
                        label = "7天 新帖",
                        count = stats.sevenDayNewCount
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 帖子列表标题
        Text(
            text = "相关帖子",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = LiteraryFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RollingNumber(
            number = count,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * 加载中内容
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "正在加载...",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重试",
                    fontFamily = LiteraryFontFamily
                )
            }
        }
    }
}
