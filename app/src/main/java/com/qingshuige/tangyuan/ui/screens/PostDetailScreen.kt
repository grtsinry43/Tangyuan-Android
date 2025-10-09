package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.CommentCard
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.components.CommentItem
import com.qingshuige.tangyuan.ui.components.CommentInputBar
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.utils.ShareCardGenerator
import com.qingshuige.tangyuan.utils.UIUtils
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.PostDetailViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 帖子详情页
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit = {},
    onAuthorClick: (Int) -> Unit = {},
    onCategoryClick: (Int) -> Unit = {},
    onImageClick: (Int, Int) -> Unit = { _, _ -> },
    viewModel: PostDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSharePreview by remember { mutableStateOf(false) }
    var shareCardBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 加载帖子详情
    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    Scaffold(
        topBar = {
            PostDetailTopBar(
                onBackClick = onBackClick,
                isLoading = state.isLoading,
                onShareClick = {
                    state.postCard?.let { postCard ->
                        // 在协程中生成分享卡片
                        scope.launch {
                            try {
                                // 生成深层链接（使用自定义scheme）
                                val deepLink = "tangyuan://post/$postId"

                                // 生成分享卡片
                                val bitmap = withContext(Dispatchers.IO) {
                                    ShareCardGenerator.generateShareCard(
                                        context = context,
                                        postCard = postCard,
                                        deepLink = deepLink
                                    )
                                }

                                shareCardBitmap = bitmap
                                showSharePreview = true
                            } catch (e: Exception) {
                                UIUtils.showError("生成分享卡片失败: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.postCard != null) {
                CommentInputBar(
                    isCreating = state.isCreatingComment,
                    replyToComment = state.replyToComment,
                    onSendComment = { content ->
                        val parentId = state.replyToComment?.commentId ?: 0
                        viewModel.createComment(content, parentId)
                    },
                    onCancelReply = {
                        viewModel.setReplyToComment(null)
                    }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshPostDetail,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 始终显示内容，确保共享元素有目标
            PostDetailContent(
                postCard = state.postCard,
                comments = state.comments,
                listState = listState,
                isLoadingComments = state.isLoading && state.postCard != null,
                isError = state.error != null && state.postCard == null,
                errorMessage = state.error,
                onAuthorClick = onAuthorClick,
                onCategoryClick = onCategoryClick,
                onImageClick = onImageClick,
                onReplyToComment = viewModel::setReplyToComment,
                onDeleteComment = { commentId ->
                    UIUtils.showConfirmDialog(
                        title = "删除评论",
                        message = "确定要删除这条评论吗？此操作不可撤销。",
                        confirmText = "删除",
                        dismissText = "取消",
                        onConfirm = {
                            viewModel.deleteComment(commentId)
                        }
                    )
                },
                onRetry = {
                    viewModel.clearError()
                    viewModel.loadPostDetail(postId)
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }

    // 分享预览对话框
    if (showSharePreview && shareCardBitmap != null) {
        SharePreviewDialog(
            bitmap = shareCardBitmap!!,
            onDismiss = {
                showSharePreview = false
                shareCardBitmap = null
            },
            onShare = {
                scope.launch {
                    try {
                        sharePostWithBitmap(context, shareCardBitmap!!, postId)
                        showSharePreview = false
                    } catch (e: Exception) {
                        UIUtils.showError("分享失败: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        )
    }

    // 错误提示
    state.commentError?.let { error ->
        LaunchedEffect(error) {
            // 显示错误提示，可以用SnackBar
            viewModel.clearCommentError()
        }
    }
}

/**
 * 分享预览对话框
 */
@Composable
private fun SharePreviewDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "分享预览",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 预览图片（缩小显示）
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "分享卡片预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "取消",
                            fontFamily = TangyuanGeneralFontFamily
                        )
                    }

                    // 分享按钮
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "分享",
                            fontFamily = TangyuanGeneralFontFamily
                        )
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
private fun PostDetailTopBar(
    onBackClick: () -> Unit,
    isLoading: Boolean,
    onShareClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "帖子详情",
                fontFamily = TangyuanGeneralFontFamily,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
            // 分享按钮
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * 帖子详情内容
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun  PostDetailContent(
    postCard: PostCard?,
    comments: List<CommentCard>,
    listState: LazyListState,
    isLoadingComments: Boolean,
    isError: Boolean,
    errorMessage: String?,
    onAuthorClick: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    onImageClick: (Int, Int) -> Unit,
    onReplyToComment: (CommentCard) -> Unit,
    onDeleteComment: (Int) -> Unit,
    onRetry: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    // 如果是错误状态，显示错误页面
    if (isError) {
        ErrorContent(
            message = errorMessage ?: "未知错误",
            onRetry = onRetry
        )
        return
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 帖子详情卡片 - 如果有数据就显示
        postCard?.let { card ->
            item {
                PostDetailCard(
                    postCard = card,
                    onAuthorClick = onAuthorClick,
                    onImageClick = onImageClick,
                    onCategoryClick = onCategoryClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
        }
        
        // 评论区标题
        item {
            CommentSectionHeader(commentCount = comments.size, isLoading = isLoadingComments)
        }
        
        // 评论加载状态
        if (isLoadingComments && comments.isEmpty()) {
            item {
                CommentsLoadingContent()
            }
        } else {
            // 评论列表
            items(
                items = comments,
                key = { it.commentId }
            ) { comment ->
                CommentItem(
                    comment = comment,
                    onReplyToComment = onReplyToComment,
                    onDeleteComment = onDeleteComment,
                    onAuthorClick = onAuthorClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    sharedElementPrefix = "postdetail_comment_${comment.commentId}"
                )
            }
            
            // 空评论提示
            if (comments.isEmpty() && !isLoadingComments) {
                item {
                    EmptyCommentsContent()
                }
            }
        }
    }
}

/**
 * 帖子详情卡片
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostDetailCard(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit,
    onImageClick: (Int, Int) -> Unit,
    onCategoryClick: (Int) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .let { mod ->
                if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        mod.sharedElement(
                            rememberSharedContentState(key = "post_card_${postCard.postId}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400, easing = FastOutSlowInEasing)
                            }
                        )
                    }
                } else mod
            },
        shape = TangyuanShapes.CulturalCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 作者信息
            PostDetailHeader(
                postCard = postCard,
                onAuthorClick = onAuthorClick,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 文章内容
            Text(
                text = postCard.textContent.withPanguSpacing(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp
                ),
                fontFamily = LiteraryFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 图片展示
            if (postCard.hasImages) {
                Spacer(modifier = Modifier.height(16.dp))
                PostDetailImages(
                    imageUUIDs = postCard.imageUUIDs,
                    postId = postCard.postId,
                    onImageClick = onImageClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 分类和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Section 徽标
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = postCard.getSectionName(),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = LiteraryFontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 分类徽标
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = postCard.categoryName,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = LiteraryFontFamily,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .clickable { onCategoryClick(postCard.categoryId) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = postCard.getTimeDisplayText(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 帖子详情头部
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostDetailHeader(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onAuthorClick(postCard.authorId) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${TangyuanApplication.instance.bizDomain}images/${postCard.authorAvatar}.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "${postCard.authorName}的头像",
            modifier = Modifier
                .size(48.dp)
                .let { mod ->
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            mod.sharedElement(
                                rememberSharedContentState(key = "user_avatar_${postCard.authorId}"),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                }
                            )
                        }
                    } else mod
                }
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = postCard.authorName.withPanguSpacing(),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "user_name_${postCard.authorId}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400, easing = FastOutSlowInEasing)
                            }
                        )
                    }
                } else Modifier
            )
            
            if (postCard.authorBio.isNotBlank()) {
                Text(
                    text = postCard.authorBio.withPanguSpacing(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = LiteraryFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 帖子详情图片
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostDetailImages(
    imageUUIDs: List<String>,
    postId: Int,
    onImageClick: (Int, Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    when (imageUUIDs.size) {
        1 -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${TangyuanApplication.instance.bizDomain}images/${imageUUIDs[0]}.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = "文章图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onImageClick(postId, 0) }
                    .let { mod ->
                        if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                mod.sharedElement(
                                    rememberSharedContentState(key = "post_image_${postId}_0"),
                                    animatedVisibilityScope = animatedContentScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                    }
                                )
                            }
                        } else mod
                    },
                contentScale = ContentScale.FillWidth
            )
        }
        else -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(imageUUIDs.size) { index ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${TangyuanApplication.instance.bizDomain}images/${imageUUIDs[index]}.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "文章图片",
                        modifier = Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onImageClick(postId, index) }
                            .let { mod ->
                                if (sharedTransitionScope != null && animatedContentScope != null) {
                                    with(sharedTransitionScope) {
                                        mod.sharedElement(
                                            rememberSharedContentState(key = "post_image_${postId}_$index"),
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
                }
            }
        }
    }
}

/**
 * 评论区标题
 */
@Composable
private fun CommentSectionHeader(commentCount: Int, isLoading: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "评论 ($commentCount)",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 评论加载内容
 */
@Composable
private fun CommentsLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "正在加载评论...",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空评论内容
 */
@Composable
private fun EmptyCommentsContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "还没有评论",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "来发表第一个评论吧",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 加载内容
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "正在加载详情...",
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
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
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
                    containerColor = MaterialTheme.colorScheme.primary
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
 * 使用已生成的Bitmap分享帖子
 */
private suspend fun sharePostWithBitmap(context: Context, bitmap: Bitmap, postId: Int) = withContext(Dispatchers.IO) {
    try {
        // 生成深层链接（使用自定义scheme）
        val deepLink = "tangyuan://post/$postId"

        // 保存到缓存目录
        val cacheDir = File(context.cacheDir, "share")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val shareImageFile = File(cacheDir, "share_post_${postId}_${System.currentTimeMillis()}.png")
        FileOutputStream(shareImageFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        // 获取文件URI（使用FileProvider）
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareImageFile
        )

        // 创建分享Intent - 添加预览支持
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TITLE, "糖原社区分享")
            putExtra(Intent.EXTRA_TEXT, "来自糖原社区的精彩内容：$deepLink")

            // 添加ClipData以支持预览
            clipData = android.content.ClipData.newRawUri("", imageUri)

            // 授予临时读取权限
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        // 启动系统分享面板
        withContext(Dispatchers.Main) {
            val chooserIntent = Intent.createChooser(shareIntent, "分享到").apply {
                // 为chooser也添加权限
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooserIntent)
            UIUtils.showSuccess("分享卡片已生成")
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            UIUtils.showError("分享失败: ${e.message}")
        }
        e.printStackTrace()
    }
}