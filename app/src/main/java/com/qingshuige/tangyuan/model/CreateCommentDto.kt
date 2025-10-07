package com.qingshuige.tangyuan.model

import java.util.Date

data class CreateCommentDto(
    @Deprecated(
        message = "后台自动生成，无需传递",
        replaceWith = ReplaceWith("null")
    ) val commentDateTime: Date? = null,
    val content: String? = null,
    @Deprecated(
        message = "字段已废弃，无需传递",
        replaceWith = ReplaceWith("null")
    )
    val imageGuid: String? = null,
    val parentCommentId: Long? = 0,
    val postId: Long = 0,
    val userId: Long = 0
)
