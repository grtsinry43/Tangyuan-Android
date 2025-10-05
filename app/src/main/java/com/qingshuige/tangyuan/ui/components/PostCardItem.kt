package com.qingshuige.tangyuan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.utils.withPanguSpacing

/**
 * Shimmer加载动画效果
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size,
        targetValue = 2 * size,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB0B0B0),
                Color(0xFFF0F0F0),
                Color(0xFFB0B0B0),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size, size)
        )
    ).onGloballyPositioned {
        size = it.size.width.toFloat()
    }
}

/**
 * 带有加载动画的AsyncImage组件
 */
@Composable
fun ShimmerAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )
    
    Box(
        modifier = if (onClick != null) {
            modifier.clickable { onClick() }
        } else {
            modifier
        }
    ) {
        if (painter.state is AsyncImagePainter.State.Loading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shimmerEffect()
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale
        )
    }
}

/**
 * 文章卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PostCardItem(
    postCard: PostCard,
    onPostClick: (Int) -> Unit = {},
    onAuthorClick: (Int) -> Unit = {},
    onLikeClick: (Int) -> Unit = {},
    onCommentClick: (Int) -> Unit = {},
    onShareClick: (Int) -> Unit = {},
    onBookmarkClick: (Int) -> Unit = {},
    onMoreClick: (Int) -> Unit = {},
    onImageClick: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPostClick(postCard.postId) }
            .let { mod ->
                if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        mod.sharedElement(
                            rememberSharedContentState(key = "post_card_${postCard.postId}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 500)
                            }
                        )
                    }
                } else mod
            },
        shape = TangyuanShapes.CulturalCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 作者信息栏
            PostCardHeader(
                postCard = postCard,
                onAuthorClick = onAuthorClick,
                onMoreClick = onMoreClick
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 文章内容
            PostCardContent(postCard = postCard)
            
            // 图片展示
            if (postCard.hasImages) {
                Spacer(modifier = Modifier.height(12.dp))
                PostCardImages(
                    imageUUIDs = postCard.imageUUIDs,
                    postId = postCard.postId,
                    onImageClick = onImageClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 分类标签
            PostCardCategory(postCard = postCard)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 交互按钮栏
            PostCardActions(
                postCard = postCard,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick,
                onBookmarkClick = onBookmarkClick
            )
        }
    }
}

/**
 * 文章卡片头部 - 作者信息
 */
@Composable
private fun PostCardHeader(
    postCard: PostCard,
    onAuthorClick: (Int) -> Unit,
    onMoreClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 作者头像
        ShimmerAsyncImage(
            imageUrl = "${TangyuanApplication.instance.bizDomain}images/${postCard.authorAvatar}.jpg",
            contentDescription = "${postCard.authorName}的头像",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            onClick = { onAuthorClick(postCard.authorId) }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 作者信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = postCard.authorName.withPanguSpacing(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = postCard.getTimeDisplayText(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        
        // 更多操作按钮
        IconButton(
            onClick = { onMoreClick(postCard.postId) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多操作",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 文章内容
 */
@Composable
private fun PostCardContent(postCard: PostCard) {
    if (postCard.contentPreview.isNotBlank()) {
        Text(
            text = postCard.contentPreview.withPanguSpacing(),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            fontFamily = LiteraryFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 图片展示
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostCardImages(
    imageUUIDs: List<String>,
    postId: Int,
    onImageClick: (Int, Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    when (imageUUIDs.size) {
        1 -> {
            // 单张图片
            ShimmerAsyncImage(
                imageUrl = "${TangyuanApplication.instance.bizDomain}images/${imageUUIDs[0]}.jpg",
                contentDescription = "文章图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
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
                contentScale = ContentScale.Crop,
                onClick = { onImageClick(postId, 0) }
            )
        }
        
        2 -> {
            // 两张图片
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                imageUUIDs.forEachIndexed { index, uuid ->
                    ShimmerAsyncImage(
                        imageUrl = "${TangyuanApplication.instance.bizDomain}images/$uuid.jpg",
                        contentDescription = "文章图片",
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(MaterialTheme.shapes.medium)
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
                        contentScale = ContentScale.Crop,
                        onClick = { onImageClick(postId, index) }
                    )
                }
            }
        }
        
        3 -> {
            // 三张图片
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                imageUUIDs.forEachIndexed { index, uuid ->
                    ShimmerAsyncImage(
                        imageUrl = "${TangyuanApplication.instance.bizDomain}images/$uuid.jpg",
                        contentDescription = "文章图片",
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(MaterialTheme.shapes.medium)
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
                        contentScale = ContentScale.Crop,
                        onClick = { onImageClick(postId, index) }
                    )
                }
            }
        }
        
        else -> {
            // 多张图片的处理逻辑
            // 可以显示前几张，其余用 +N 的形式展示
        }
    }
}

/**
 * 分类标签
 */
@Composable
private fun PostCardCategory(postCard: PostCard) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = postCard.categoryName,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 交互按钮栏
 */
@Composable
private fun PostCardActions(
    postCard: PostCard,
    onLikeClick: (Int) -> Unit,
    onCommentClick: (Int) -> Unit,
    onShareClick: (Int) -> Unit,
    onBookmarkClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧按钮组
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 点赞按钮
            PostActionButton(
                icon = if (postCard.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = postCard.likeCount,
                isActive = postCard.isLiked,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { onLikeClick(postCard.postId) }
            )
            
            // 评论按钮
            PostActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = postCard.commentCount,
                isActive = false,
                onClick = { onCommentClick(postCard.postId) }
            )
            
            // 分享按钮
            PostActionButton(
                icon = Icons.Outlined.Share,
                count = postCard.shareCount,
                isActive = false,
                onClick = { onShareClick(postCard.postId) }
            )
        }
        
        // 收藏按钮
        IconButton(
            onClick = { onBookmarkClick(postCard.postId) },
            modifier = Modifier.size(32.dp)
        ) {
            val bookmarkColor by animateColorAsState(
                targetValue = if (postCard.isBookmarked) 
                    MaterialTheme.colorScheme.tertiary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "bookmark_color"
            )
            
            Icon(
                imageVector = if (postCard.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (postCard.isBookmarked) "取消收藏" else "收藏",
                tint = bookmarkColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 交互按钮组件
 */
@Composable
private fun PostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "action_color"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    count < 1000 -> count.toString()
                    count < 10000 -> "${count / 1000}.${(count % 1000) / 100}k"
                    else -> "${count / 10000}.${(count % 10000) / 1000}w"
                },
                style = MaterialTheme.typography.labelSmall,
                fontFamily = LiteraryFontFamily,
                color = color,
                fontSize = 12.sp
            )
        }
    }
}