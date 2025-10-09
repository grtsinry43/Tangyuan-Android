package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.ui.components.PostCardItem
import com.qingshuige.tangyuan.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit,
    onUserClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var localQuery by remember { mutableStateOf(uiState.query) }
    var searchSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    TextField(
                        value = localQuery,
                        onValueChange = {
                            localQuery = it
                            viewModel.updateQuery(it)
                            searchSubmitted = false
                        },
                        placeholder = { Text("搜索帖子/用户/评论") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                searchSubmitted = true
                                viewModel.searchAll()
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "搜索")
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                searchSubmitted = true
                                viewModel.searchAll()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (localQuery.isBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("输入关键词后按回车进行搜索")
                        }
                    }
                }
                if (uiState.posts.isNotEmpty()) {
                    item {
                        SectionHeader(title = "帖子")
                    }
                    items(uiState.posts, key = { it.postId }) { metadata ->
                        // 触发按需加载 PostCard（含作者、分类、正文）
                        LaunchedEffect(metadata.postId) {
                            viewModel.loadPostCard(metadata.postId)
                        }
                        val card = uiState.postCards[metadata.postId]
                        if (card != null) {
                            PostCardItem(
                                postCard = card,
                                onPostClick = onPostClick,
                                onAuthorClick = onUserClick,
                                onLikeClick = {},
                                onCommentClick = onPostClick,
                                onShareClick = {},
                                onBookmarkClick = {},
                                onMoreClick = {},
                                onImageClick = { id, index -> onPostClick(id) },
                                onCategoryClick = {},
                                showSectionBadge = true
                            )
                        } else {
                            // 占位骨架（简单用 LinearProgressIndicator 代替，可替换为 Shimmer）
                            LinearProgressIndicator(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }

                if (uiState.users.isNotEmpty()) {
                    item { SectionHeader(title = "用户") }
                    items(uiState.users, key = { it.userId }) { user ->
                        val avatarUrl = if (user.avatarGuid.isNotBlank()) {
                            "${TangyuanApplication.instance.bizDomain}images/${user.avatarGuid}.jpg"
                        } else null
                        ListItem(
                            leadingContent = {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "头像",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    fallback = painterResource(R.drawable.ic_launcher_foreground),
                                    error = painterResource(R.drawable.ic_launcher_foreground)
                                )
                            },
                            headlineContent = { Text(user.nickName) },
                            supportingContent = { Text(user.email.ifBlank { "" }) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            trailingContent = {
                                TextButton(onClick = { onUserClick(user.userId) }) {
                                    Text("查看")
                                }
                            }
                        )
                        Divider()
                    }
                }

                if (uiState.comments.isNotEmpty()) {
                    item { SectionHeader(title = "评论") }
                    items(uiState.comments, key = { it.commentId }) { comment ->
                        ListItem(
                            headlineContent = { Text(comment.content?.take(40) ?: "点击查看详情") },
                            supportingContent = { Text("来自帖子 ${comment.postId}") },
                            trailingContent = {
                                TextButton(onClick = { onPostClick(comment.postId) }) {
                                    Text("查看")
                                }
                            }
                        )
                        Divider()
                    }
                }

                if (
                    searchSubmitted &&
                    !uiState.isLoading &&
                    uiState.posts.isEmpty() && uiState.users.isEmpty() && uiState.comments.isEmpty() &&
                    uiState.query.isNotBlank()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未找到匹配结果")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}


