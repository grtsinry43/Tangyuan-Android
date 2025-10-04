package com.qingshuige.tangyuan.model

import java.util.Date

data class Notification(
    var notificationId: Int = 0,
    var targetUserId: Int = 0,
    var targetPostId: Int = 0,
    var targetCommentId: Int = 0,
    var sourceCommentId: Int = 0,
    var sourceUserId: Int = 0,
    var isRead: Boolean = false,
    var notificationDateTime: Date? = null
)
