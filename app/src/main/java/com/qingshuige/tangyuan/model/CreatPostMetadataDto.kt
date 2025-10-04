package com.qingshuige.tangyuan.model

import java.util.Date

data class CreatPostMetadataDto(
    var isVisible: Boolean = false,
    var postDateTime: Date? = null,
    var sectionId: Int = 0,
    var categoryId: Int = 0,
    var userId: Int = 0
)
