package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.viewmodel.TopicViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TopicScreen(
    onPostClick: (Int) -> Unit = {},
    onAuthorClick: (Int) -> Unit = {},
    onImageClick: (Int, Int) -> Unit = { _, _ -> },
    viewModel: TopicViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // 监听滚动，实现上拉加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                // 当滚动到倒数第3个item时开始加载更多
                if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 3 &&
                    !uiState.isLoading && uiState.hasMore && uiState.error == null
                ) {
                    viewModel.loadMorePosts()
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshPosts,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                // 加载状态
                uiState.isLoading && uiState.posts.isEmpty() -> {
                    LoadingContent()
                }

                // 错误状态
                uiState.error != null && uiState.posts.isEmpty() -> {
                    val errorMessage = uiState.error
                    ErrorContent(
                        message = errorMessage!!,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.loadRecommendedPosts()
                        }
                    )
                }

                // 空状态
                uiState.posts.isEmpty() && !uiState.isLoading -> {
                    EmptyContent(onRefresh = viewModel::refreshPosts)
                }

                // 正常内容
                else -> {
                    PostList(
                        posts = uiState.posts,
                        listState = listState,
                        isLoadingMore = uiState.isLoading,
                        hasMore = uiState.hasMore,
                        error = uiState.error,
                        onPostClick = onPostClick,
                        onAuthorClick = onAuthorClick,
                        onLikeClick = viewModel::toggleLike,
                        onCommentClick = { postId -> onPostClick(postId) }, // 点击评论跳转到详情页
                        onShareClick = viewModel::sharePost,
                        onBookmarkClick = viewModel::toggleBookmark,
                        onMoreClick = { postId ->
                            // TODO: 显示更多操作菜单
                        },
                        onImageClick = onImageClick,
                        onErrorDismiss = viewModel::clearError,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    )
                }
            }
        }
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
                text = "正在加载精彩内容...",
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

/**
 * 空内容
 */
@Composable
private fun EmptyContent(onRefresh: () -> Unit) {
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
                text = "暂无内容",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "下拉刷新或稍后再试",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            TextButton(onClick = onRefresh) {
                Text(
                    text = "刷新",
                    fontFamily = LiteraryFontFamily,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * 加载更多指示器
 */
@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 2.dp
            )
            Text(
                text = "加载更多...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 没有更多数据指示器
 */
@Composable
private fun NoMoreDataIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "已经到底了，没有更多内容",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 错误提示
 */
@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text(
                    text = "知道了",
                    fontFamily = LiteraryFontFamily,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}