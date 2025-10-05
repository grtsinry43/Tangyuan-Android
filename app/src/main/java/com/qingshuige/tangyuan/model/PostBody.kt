package com.qingshuige.tangyuan.model

data class PostBody(
    val postId: Int = 0,
    val textContent: String? = null,
    val image1UUID: String? = null,
    val image2UUID: String? = null,
    val image3UUID: String? = null
)
