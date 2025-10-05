package com.qingshuige.tangyuan.model

import java.util.Date

data class Notification(
    val notificationId: Int = 0,
    val targetUserId: Int = 0,
    val targetPostId: Int = 0,
    val targetCommentId: Int = 0,
    val sourceCommentId: Int = 0,
    val sourceUserId: Int = 0,
    val isRead: Boolean = false,
    val notificationDateTime: Date? = null
)
