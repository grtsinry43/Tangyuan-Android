package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingshuige.tangyuan.model.PostWithContent
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.PostManagementViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostManagementScreen(
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit = {},
    viewModel: PostManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 处理删除成功
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            kotlinx.coroutines.delay(1000)
            viewModel.clearDeleteSuccess()
        }
    }

    // 处理错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "帖子管理".withPanguSpacing(),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = TangyuanGeneralFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 全选按钮
                    if (uiState.posts.isNotEmpty()) {
                        if (uiState.selectedPosts.size == uiState.posts.size) {
                            TextButton(onClick = { viewModel.clearSelection() }) {
                                Text("取消全选")
                            }
                        } else {
                            TextButton(onClick = { viewModel.selectAll() }) {
                                Text("全选")
                            }
                        }
                    }

                    // 删除按钮
                    if (uiState.selectedPosts.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.loadUserPosts() },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.posts.isEmpty() -> {
                        LoadingContent()
                    }

                    !uiState.isLoading && uiState.posts.isEmpty() -> {
                        EmptyPostsContent()
                    }

                    else -> {
                        PostList(
                            posts = uiState.posts,
                            selectedPosts = uiState.selectedPosts,
                            onPostClick = onPostClick,
                            onPostSelect = { viewModel.togglePostSelection(it) }
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

            // 删除成功提示
            AnimatedVisibility(
                visible = uiState.deleteSuccess,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "删除成功",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "确认删除".withPanguSpacing(),
                    fontFamily = TangyuanGeneralFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "确定要删除选中的 ${uiState.selectedPosts.size} 篇帖子吗？此操作不可恢复。".withPanguSpacing(),
                    fontFamily = TangyuanGeneralFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSelectedPosts()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
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
private fun EmptyPostsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PostAdd,
                contentDescription = "暂无帖子",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "暂无帖子".withPanguSpacing(),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostList(
    posts: List<PostWithContent>,
    selectedPosts: Set<Int>,
    onPostClick: (Int) -> Unit,
    onPostSelect: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = posts,
            key = { it.metadata.postId }
        ) { post ->
            PostManagementItem(
                post = post,
                isSelected = selectedPosts.contains(post.metadata.postId),
                onPostClick = { onPostClick(post.metadata.postId) },
                onSelect = { onPostSelect(post.metadata.postId) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun PostManagementItem(
    post: PostWithContent,
    isSelected: Boolean,
    onPostClick: () -> Unit,
    onSelect: () -> Unit
) {
    val metadata = post.metadata
    val contentPreview = post.content.take(20) + if (post.content.length > 20) "..." else ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 复选框
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelect() }
        )

        Spacer(modifier = Modifier.size(12.dp))

        // 帖子信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contentPreview.withPanguSpacing(),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = TangyuanGeneralFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(metadata.postDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!metadata.isVisible) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "• 不可见",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = TangyuanGeneralFontFamily,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            sdf.format(date)
        }
    }
}
