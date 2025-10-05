package com.qingshuige.tangyuan.model

import java.util.Date

data class CreatPostMetadataDto(
    val isVisible: Boolean = false,
    val postDateTime: Date? = null,
    val sectionId: Int = 0,
    val categoryId: Int = 0,
    val userId: Int = 0
)
