package com.qingshuige.tangyuan.model

import java.util.Date

data class Comment(
    var commentId: Int = 0,
    var parentCommentId: Int = 0,
    var userId: Int = 0,
    var postId: Int = 0,
    var content: String? = null,
    var imageGuid: String? = null,
    var commentDateTime: Date? = null
)
