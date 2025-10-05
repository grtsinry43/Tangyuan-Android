package com.qingshuige.tangyuan.model

import java.util.Date

data class NewNotification(
    val notificationId: Int = 0,
    val type: String? = null,
    val targetUserId: Int = 0,
    val sourceId: Int = 0,
    val sourceType: String? = null,
    val isRead: Boolean = false,
    val createDate: Date? = null
)
