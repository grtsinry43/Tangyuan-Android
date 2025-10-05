package com.qingshuige.tangyuan.model

import java.util.Date

/**
 * 评论卡片展示数据模型
 * 聚合了Comment、User信息的完整展示数据
 */
data class CommentCard(
    // 评论基本信息
    val commentId: Int,
    val parentCommentId: Int = 0,
    val postId: Int,
    val content: String,
    val imageGuid: String? = null,
    val commentDateTime: Date?,
    
    // 作者信息
    val authorId: Int,
    val authorName: String,
    val authorAvatar: String,
    val authorBio: String = "",
    
    // 互动信息
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    
    // 状态
    val isLiked: Boolean = false,
    val canDelete: Boolean = false,
    val canReply: Boolean = true,
    
    // 子评论
    val replies: List<CommentCard> = emptyList(),
    val hasMoreReplies: Boolean = false
) {
    /**
     * 是否为回复评论
     */
    val isReply: Boolean
        get() = parentCommentId != 0
    
    /**
     * 是否有图片
     */
    val hasImage: Boolean
        get() = !imageGuid.isNullOrBlank()
    
    /**
     * 获取时间显示文本
     */
    fun getTimeDisplayText(): String {
        commentDateTime ?: return "未知时间"
        
        val now = Date()
        val diffMillis = now.time - commentDateTime.time
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        return when {
            diffMinutes < 1 -> "刚刚"
            diffMinutes < 60 -> "${diffMinutes}分钟前"
            diffHours < 24 -> "${diffHours}小时前"
            diffDays < 7 -> "${diffDays}天前"
            else -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = commentDateTime
                String.format("%02d-%02d", 
                    calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
            }
        }
    }
}

/**
 * 帖子详情页状态数据模型
 */
data class PostDetailState(
    val isLoading: Boolean = false,
    val postCard: PostCard? = null,
    val comments: List<CommentCard> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isLoadingMoreComments: Boolean = false,
    val hasMoreComments: Boolean = true,
    val currentCommentPage: Int = 0,
    
    // 评论输入状态
    val isCreatingComment: Boolean = false,
    val commentError: String? = null,
    val replyToComment: CommentCard? = null
)

/**
 * Comment 到 CommentCard 的转换扩展
 */
fun Comment.toCommentCard(
    author: User,
    replies: List<CommentCard> = emptyList(),
    hasMoreReplies: Boolean = false,
    currentUserId: Int = 0
): CommentCard {
    return CommentCard(
        commentId = this.commentId,
        parentCommentId = this.parentCommentId,
        postId = this.postId,
        content = this.content ?: "",
        imageGuid = this.imageGuid,
        commentDateTime = this.commentDateTime,
        
        authorId = author.userId,
        authorName = author.nickName.ifBlank { "匿名用户" },
        authorAvatar = author.avatarGuid,
        authorBio = author.bio,
        
        replies = replies,
        hasMoreReplies = hasMoreReplies,
        canDelete = author.userId == currentUserId
    )
}