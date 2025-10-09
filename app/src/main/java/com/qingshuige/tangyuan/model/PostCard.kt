package com.qingshuige.tangyuan.model

import java.util.Date

/**
 * 文章卡片展示数据模型
 * 聚合了PostMetadata、User、Category、PostBody的关键信息
 */
data class PostCard(
    // 文章基本信息
    val postId: Int,
    val postDateTime: Date?,
    val isVisible: Boolean,
    // 所属版块
    val sectionId: Int = 1,
    
    // 作者信息
    val authorId: Int,
    val authorName: String,
    val authorAvatar: String,
    val authorBio: String = "",
    
    // 分类信息
    val categoryId: Int,
    val categoryName: String,
    val categoryDescription: String = "",
    
    // 内容信息
    val textContent: String,
    val imageUUIDs: List<String> = emptyList(), // 图片UUID列表
    val hasImages: Boolean = false,
    
    // 互动信息（预留）
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    
    // 状态
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
) {
    
    /**
     * 获取第一张图片UUID
     */
    val firstImageUUID: String?
        get() = imageUUIDs.firstOrNull()
    
    /**
     * 获取内容预览（去除HTML标签，限制长度）
     */
    val contentPreview: String
        get() = textContent
            .replace(Regex("<[^>]*>"), "") // 移除HTML标签
            .replace(Regex("\\s+"), " ") // 合并空白字符
            .trim()
            .take(150) // 限制150字符
            .let { if (textContent.length > 150) "$it..." else it }
    
    /**
     * 判断是否有多张图片
     */
    val hasMultipleImages: Boolean
        get() = imageUUIDs.size > 1
    
    /**
     * 获取时间显示文本
     */
    fun getTimeDisplayText(): String {
        postDateTime ?: return "未知时间"
        
        val now = Date()
        val diffMillis = now.time - postDateTime.time
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        return when {
            diffMinutes < 1 -> "刚刚"
            diffMinutes < 60 -> "${diffMinutes}分钟前"
            diffHours < 24 -> "${diffHours}小时前"
            diffDays < 7 -> "${diffDays}天前"
            else -> {
                // 格式化为 MM-dd
                val calendar = java.util.Calendar.getInstance()
                calendar.time = postDateTime
                String.format("%02d-%02d", 
                    calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
            }
        }
    }

    /**
     * 获取版块名称
     */
    fun getSectionName(): String = when (sectionId) {
        0 -> "公告"
        1 -> "聊一聊"
        2 -> "侃一侃"
        else -> "其他"
    }
}

/**
 * 推荐文章列表状态
 */
data class RecommendedPostsState(
    val isLoading: Boolean = false,
    val posts: List<PostCard> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)

/**
 * PostMetadata 到 PostCard 的转换扩展
 */
fun PostMetadata.toPostCard(
    author: User,
    category: Category,
    body: PostBody
): PostCard {
    val images = listOfNotNull(
        body.image1UUID,
        body.image2UUID,
        body.image3UUID
    ).filter { it.isNotBlank() }
    
    return PostCard(
        postId = this.postId,
        postDateTime = this.postDateTime,
        isVisible = this.isVisible,
        sectionId = this.sectionId,
        
        authorId = author.userId,
        authorName = author.nickName.ifBlank { "匿名用户" },
        authorAvatar = author.avatarGuid,
        authorBio = author.bio,
        
        categoryId = category.categoryId,
        categoryName = category.baseName ?: "未分类",
        categoryDescription = category.baseDescription ?: "",
        
        textContent = body.textContent ?: "",
        imageUUIDs = images,
        hasImages = images.isNotEmpty()
    )
}