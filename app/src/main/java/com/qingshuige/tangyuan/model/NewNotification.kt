package com.qingshuige.tangyuan.model

import java.util.Date

data class NewNotification(
    var notificationId: Int = 0,
    var type: String? = null,
    var targetUserId: Int = 0,
    var sourceId: Int = 0,
    var sourceType: String? = null,
    var isRead: Boolean = false,
    var createDate: Date? = null
)
