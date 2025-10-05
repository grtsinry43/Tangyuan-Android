package com.qingshuige.tangyuan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.CommentCard
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.utils.withPanguSpacing

/**
 * 评论项组件
 */
@Composable
fun CommentItem(
    comment: CommentCard,
    onReplyToComment: (CommentCard) -> Unit = {},
    onDeleteComment: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 评论主体
            CommentMainContent(
                comment = comment,
                onReplyToComment = onReplyToComment,
                onDeleteComment = onDeleteComment
            )
            
            // 回复列表
            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CommentReplies(
                    replies = comment.replies,
                    onReplyToComment = onReplyToComment,
                    onDeleteComment = onDeleteComment
                )
            }
        }
    }
}

/**
 * 评论主体内容
 */
@Composable
private fun CommentMainContent(
    comment: CommentCard,
    onReplyToComment: (CommentCard) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    Column {
        // 评论头部 - 用户信息
        CommentHeader(comment = comment)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 评论内容
        Text(
            text = comment.content.withPanguSpacing(),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 评论图片
        if (comment.hasImage) {
            Spacer(modifier = Modifier.height(8.dp))
            CommentImage(imageGuid = comment.imageGuid!!)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 评论操作栏
        CommentActions(
            comment = comment,
            onReplyToComment = onReplyToComment,
            onDeleteComment = onDeleteComment
        )
    }
}

/**
 * 评论头部 - 用户信息
 */
@Composable
private fun CommentHeader(comment: CommentCard) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 用户头像
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${TangyuanApplication.instance.bizDomain}images/${comment.authorAvatar}.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "${comment.authorName}的头像",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 用户信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = comment.authorName.withPanguSpacing(),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 时间
        Text(
            text = comment.getTimeDisplayText(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

/**
 * 评论图片
 */
@Composable
private fun CommentImage(imageGuid: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("${TangyuanApplication.instance.bizDomain}images/$imageGuid.jpg")
            .crossfade(true)
            .build(),
        contentDescription = "评论图片",
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Fit
    )
}

/**
 * 评论操作栏
 */
@Composable
private fun CommentActions(
    comment: CommentCard,
    onReplyToComment: (CommentCard) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧操作
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 点赞按钮
            CommentActionButton(
                icon = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = comment.likeCount,
                isActive = comment.isLiked,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { /* TODO: 实现点赞 */ }
            )
            
            // 回复按钮
            CommentActionButton(
                icon = Icons.Outlined.Reply,
                text = "回复",
                isActive = false,
                onClick = { onReplyToComment(comment) }
            )
        }
        
        // 右侧操作
        if (comment.canDelete) {
            IconButton(
                onClick = { onDeleteComment(comment.commentId) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除评论",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 评论操作按钮
 */
@Composable
private fun CommentActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int = 0,
    text: String = "",
    isActive: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "comment_action_color"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = LiteraryFontFamily,
                color = color,
                fontSize = 11.sp
            )
        }
        
        if (text.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = LiteraryFontFamily,
                color = color,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 评论回复列表
 */
@Composable
private fun CommentReplies(
    replies: List<CommentCard>,
    onReplyToComment: (CommentCard) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            replies.forEach { reply ->
                ReplyItem(
                    reply = reply,
                    onReplyToComment = onReplyToComment,
                    onDeleteComment = onDeleteComment
                )
                
                if (reply != replies.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 回复项组件
 */
@Composable
private fun ReplyItem(
    reply: CommentCard,
    onReplyToComment: (CommentCard) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    Column {
        // 回复头部
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${TangyuanApplication.instance.bizDomain}images/${reply.authorAvatar}.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = "${reply.authorName}的头像",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = reply.authorName.withPanguSpacing(),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = reply.getTimeDisplayText(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 回复内容
        Text(
            text = reply.content.withPanguSpacing(),
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 18.sp
            ),
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 回复图片
        if (reply.hasImage) {
            Spacer(modifier = Modifier.height(6.dp))
            CommentImage(imageGuid = reply.imageGuid!!)
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // 回复操作
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommentActionButton(
                icon = if (reply.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = reply.likeCount,
                isActive = reply.isLiked,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { /* TODO: 实现点赞 */ }
            )
            
            CommentActionButton(
                icon = Icons.Outlined.Reply,
                text = "回复",
                isActive = false,
                onClick = { onReplyToComment(reply) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (reply.canDelete) {
                IconButton(
                    onClick = { onDeleteComment(reply.commentId) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除回复",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * 评论输入栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputBar(
    isCreating: Boolean = false,
    replyToComment: CommentCard? = null,
    onSendComment: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            // 回复提示栏
            AnimatedVisibility(
                visible = replyToComment != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                replyToComment?.let { comment ->
                    ReplyIndicator(
                        comment = comment,
                        onCancel = onCancelReply
                    )
                }
            }
            
            // 输入栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 输入框
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (replyToComment != null) "回复 ${replyToComment.authorName}" else "说点什么...",
                            fontFamily = LiteraryFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commentText.isNotBlank() && !isCreating) {
                                onSendComment(commentText)
                                commentText = ""
                            }
                        }
                    ),
                    maxLines = 4
                )
                
                // 发送按钮
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank() && !isCreating) {
                            onSendComment(commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank() && !isCreating,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送",
                            tint = if (commentText.isNotBlank()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 回复指示器
 */
@Composable
private fun ReplyIndicator(
    comment: CommentCard,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Reply,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "回复 ${comment.authorName}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = LiteraryFontFamily,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消回复",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}