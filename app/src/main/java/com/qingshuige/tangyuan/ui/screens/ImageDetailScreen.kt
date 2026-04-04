package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.utils.ErrorMapper
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.PostDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 图片详情页面 - 以图片为主的展示界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImageDetailScreen(
    postId: Int,
    initialImageIndex: Int = 0,
    onBackClick: () -> Unit = {},
    onAuthorClick: (Int) -> Unit = {},
    onCategoryClick: (Int) -> Unit = {},
    onSwitchToTextMode: () -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.clearSaveMessage()
        }
    }

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }
    
    // 当有帖子数据时才显示内容
    state.postCard?.let { postCard ->
        val imageUUIDs = postCard.imageUUIDs
        val validImageIndex = initialImageIndex.coerceIn(0, imageUUIDs.size - 1)
        val pagerState = rememberPagerState(
            initialPage = validImageIndex,
            pageCount = { imageUUIDs.size }
        )
        
        Box(modifier = Modifier.fillMaxSize()) {
            // 背景图片（模糊效果）
            BackgroundBlurredImage(
                imageUUIDs = imageUUIDs,
                currentPage = pagerState.currentPage
            )

            // 图片全屏铺满
            if (imageUUIDs.isNotEmpty()) {
                ImagePager(
                    imageUUIDs = imageUUIDs,
                    postId = postCard.postId,
                    pagerState = pagerState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }

            // 顶部导航栏（浮在图片上方）
            ImageDetailTopBar(
                onBackClick = onBackClick,
                currentIndex = pagerState.currentPage + 1,
                totalCount = imageUUIDs.size,
                onSaveClick = {
                    val currentImageUrl =
                        "${TangyuanApplication.BIZ_DOMAIN}images/${imageUUIDs[pagerState.currentPage]}.jpg"
                    viewModel.saveCurrentImage(currentImageUrl)
                }
            )

            // 底部内容区域（浮在图片下方，不挤压图片空间）
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomContentOverlay(
                    postCard = postCard,
                    onAuthorClick = onAuthorClick,
                    onCategoryClick = onCategoryClick,
                    onSwitchToTextMode = onSwitchToTextMode,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }

    // 加载状态
    if (state.isLoading && state.postCard == null) {
        LoadingContent()
    }

    // 错误状态
    state.error?.let { error ->
        ErrorContent(
            message = error,
            onRetry = {
                viewModel.clearError()
                viewModel.loadPostDetail(postId)
            }
        )
    }
}

/**
 * 背景模糊图片
 */
@Composable
private fun BackgroundBlurredImage(
    imageUUIDs: List<String>,
    currentPage: Int
) {
    if (imageUUIDs.isNotEmpty() && currentPage < imageUUIDs.size) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${TangyuanApplication.BIZ_DOMAIN}images/${imageUUIDs[currentPage]}.jpg")
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
    }

    // 渐变遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.4f),
                        Color.Black.copy(alpha = 0.1f),
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
    )
}

/**
 * 顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageDetailTopBar(
    onBackClick: () -> Unit,
    currentIndex: Int,
    totalCount: Int,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "$currentIndex / $totalCount",
                fontFamily = TangyuanGeneralFontFamily,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
        },
        actions = {
            // 保存图片按钮
            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "保存图片",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImagePager(
    imageUUIDs: List<String>,
    postId: Int,
    pagerState: PagerState,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        // 不再手动控制 userScrollEnabled —— 由 ZoomableImage 的手势选择性消费来决定
    ) { page ->
        ZoomableImage(
            imageUrl = "${TangyuanApplication.BIZ_DOMAIN}images/${imageUUIDs[page]}.jpg",
            postId = postId,
            imageIndex = page,
            isCurrentPage = pagerState.settledPage == page,
            contentDescription = "图片 ${page + 1}",
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }
}

/**
 * 可缩放图片组件
 *
 * 核心逻辑：使用 awaitEachGesture 手动处理手势，选择性消费事件：
 * - 未缩放时：不消费手势，让 HorizontalPager 处理左右滑动
 * - 缩放中（双指）：消费手势，处理缩放和平移
 * - 已缩放单指拖动：消费手势处理平移，但在水平边界处放行给 HorizontalPager
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ZoomableImage(
    imageUrl: String,
    postId: Int,
    imageIndex: Int,
    isCurrentPage: Boolean,
    contentDescription: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // 切换页面时重置缩放
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            // 缩放 & 平移：使用自定义手势处理，选择性消费事件
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (canceled) break

                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        val pointerCount = event.changes.count { it.pressed }

                        // 根据容器实际尺寸计算平移边界
                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                        val maxX = ((newScale - 1f) * containerSize.width / 2f)
                            .coerceAtLeast(0f)
                        val maxY = ((newScale - 1f) * containerSize.height / 2f)
                            .coerceAtLeast(0f)

                        val proposedOffset = Offset(
                            x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                            y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                        )

                        when {
                            // 双指操作：始终消费（缩放+平移）
                            pointerCount >= 2 -> {
                                scale = newScale
                                offset = proposedOffset
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                            // 单指 + 已缩放：处理平移，边界处放行给 Pager
                            pointerCount == 1 && scale > 1.01f -> {
                                val atLeftEdge = offset.x >= maxX - 0.5f
                                val atRightEdge = offset.x <= -maxX + 0.5f
                                val isHorizontalSwipe =
                                    kotlin.math.abs(panChange.x) > kotlin.math.abs(panChange.y) * 1.5f

                                val shouldPassThrough = isHorizontalSwipe && (
                                    (atLeftEdge && panChange.x > 0) ||
                                    (atRightEdge && panChange.x < 0)
                                )

                                if (!shouldPassThrough) {
                                    offset = proposedOffset
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                                // shouldPassThrough 时不消费，HorizontalPager 自动接管
                            }
                            // 单指 + 未缩放：不消费，让 HorizontalPager 处理滑动
                        }
                    } while (event.changes.any { it.pressed })

                    // 手势结束：接近 1x 时自动回弹
                    if (scale < 1.05f) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }
            }
            // 双击缩放
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.01f) {
                            // 已缩放 → 重置
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            // 未缩放 → 放大到点击位置
                            val targetScale = 2.5f
                            val centerX = containerSize.width / 2f
                            val centerY = containerSize.height / 2f
                            val maxX = ((targetScale - 1f) * containerSize.width / 2f)
                            val maxY = ((targetScale - 1f) * containerSize.height / 2f)
                            scale = targetScale
                            offset = Offset(
                                x = ((centerX - tapOffset.x) * (targetScale - 1f))
                                    .coerceIn(-maxX, maxX),
                                y = ((centerY - tapOffset.y) * (targetScale - 1f))
                                    .coerceIn(-maxY, maxY)
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .then(
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "post_image_${postId}_${imageIndex}"),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                }
                            )
                        }
                    } else Modifier
                )
        )
    }
}



/**
 * 底部内容遮罩
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BottomContentOverlay(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    onSwitchToTextMode: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = -100f // 上滑超过100px触发切换

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
            .offset(y = offsetY.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetY < swipeThreshold) {
                            onSwitchToTextMode()
                        } else {
                            offsetY = 0f
                        }
                    }
                ) { _, dragAmount ->
                    offsetY = (offsetY + dragAmount.y).coerceAtMost(0f)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            PostAuthorInfo(
                postCard = postCard,
                onAuthorClick = onAuthorClick,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = postCard.textContent.withPanguSpacing(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                fontFamily = LiteraryFontFamily,
                color = Color.White,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = postCard.getSectionName(),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = LiteraryFontFamily,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 分类徽标
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = postCard.categoryName,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = LiteraryFontFamily,
                            color = Color.White,
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
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "上滑查看详情",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * 作者信息组件
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostAuthorInfo(
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
                .data("${TangyuanApplication.BIZ_DOMAIN}images/${postCard.authorAvatar}.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "${postCard.authorName}的头像",
            modifier = Modifier
                .size(48.dp)
                .let { mod ->
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            mod.sharedElement(
                                rememberSharedContentState(key = "post_avatar_${postCard.postId}"),
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
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "post_name_${postCard.postId}"),
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
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 加载状态组件
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White
            )
            Text(
                text = "正在加载图片...",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = Color.White
            )
        }
    }
}

/**
 * 错误状态组件
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    val caption = remember(message) { ErrorMapper.toLiteraryCaption(message) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LiteraryFontFamily,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = LiteraryFontFamily,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重试",
                    fontFamily = LiteraryFontFamily,
                    color = Color.White
                )
            }
        }
    }
}
