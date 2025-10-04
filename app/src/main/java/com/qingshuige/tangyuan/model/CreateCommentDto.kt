package com.qingshuige.tangyuan.model

import java.util.Date

data class CreateCommentDto(
    var commentDateTime: Date? = null,
    var content: String? = null,
    var imageGuid: String? = null,
    var parentCommentId: Long = 0,
    var postId: Long = 0,
    var userId: Long = 0
)
