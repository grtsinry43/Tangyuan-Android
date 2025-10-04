package com.qingshuige.tangyuan.model

import java.util.Date

data class PostMetadata(
    val postId: Int = 0,
    val userId: Int = 0,
    val postDateTime: Date? = null,
    val sectionId: Int = 0,
    val categoryId: Int = 0,
    val isVisible: Boolean = false
)
