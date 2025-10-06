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
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.PostDetailViewModel
import kotlin.math.max
import kotlin.math.min

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
    onSwitchToTextMode: () -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val state by viewModel.state.collectAsState()
    
    // 加载帖子详情
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
            
            // 主要内容
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部导航栏
                ImageDetailTopBar(
                    onBackClick = onBackClick,
                    currentIndex = pagerState.currentPage + 1,
                    totalCount = imageUUIDs.size
                )
                
                // 图片轮播区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUUIDs.isNotEmpty()) {
                        ImagePager(
                            imageUUIDs = imageUUIDs,
                            postId = postCard.postId,
                            pagerState = pagerState,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope
                        )
                    }
                }
                
                // 底部内容区域（模糊遮罩）
                BottomContentOverlay(
                    postCard = postCard,
                    onAuthorClick = onAuthorClick,
                    onSwitchToTextMode = onSwitchToTextMode
                )
            }
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
                .data("${TangyuanApplication.instance.bizDomain}images/${imageUUIDs[currentPage]}.jpg")
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
    totalCount: Int
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * 图片轮播组件
 */
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
        modifier = Modifier.fillMaxSize()
    ) { page ->
        ZoomableImage(
            imageUrl = "${TangyuanApplication.instance.bizDomain}images/${imageUUIDs[page]}.jpg",
            postId = postId,
            imageIndex = page,
            contentDescription = "图片 ${page + 1}",
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }
}

/**
 * 可缩放的图片组件
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ZoomableImage(
    imageUrl: String,
    postId: Int,
    imageIndex: Int,
    contentDescription: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        val maxX = (scale - 1) * 300
        val maxY = (scale - 1) * 300
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .let { mod ->
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            mod.sharedElement(
                                rememberSharedContentState(key = "post_image_${postId}_${imageIndex}"),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                },
                                placeHolderSize = SharedTransitionScope.PlaceHolderSize.animatedSize,
                                renderInOverlayDuringTransition = false
                            )
                        }
                    } else mod
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformableState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2f
                            offset = Offset.Zero
                        }
                    )
                },
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 底部内容遮罩
 */
@Composable
private fun BottomContentOverlay(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit,
    onSwitchToTextMode: () -> Unit
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
            // 作者信息
            PostAuthorInfo(
                postCard = postCard,
                onAuthorClick = onAuthorClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 文章内容
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
            
            // 分类和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = postCard.categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = LiteraryFontFamily,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = postCard.getTimeDisplayText(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = TangyuanGeneralFontFamily,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 上滑提示 - 放在最下面居中
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
@Composable
private fun PostAuthorInfo(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit
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
                fontWeight = FontWeight.SemiBold
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = LiteraryFontFamily,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = LiteraryFontFamily,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
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