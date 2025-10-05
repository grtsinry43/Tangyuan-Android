package com.qingshuige.tangyuan.model

import java.util.Date

data class CreateCommentDto(
    val commentDateTime: Date? = null,
    val content: String? = null,
    val imageGuid: String? = null,
    val parentCommentId: Long = 0,
    val postId: Long = 0,
    val userId: Long = 0
)
