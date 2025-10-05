package com.qingshuige.tangyuan.model

import java.util.Date

data class Comment(
    val commentId: Int = 0,
    val parentCommentId: Int = 0,
    val userId: Int = 0,
    val postId: Int = 0,
    val content: String? = null,
    val imageGuid: String? = null,
    val commentDateTime: Date? = null
)
