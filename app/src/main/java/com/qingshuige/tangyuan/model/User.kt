package com.qingshuige.tangyuan.model

data class User(
    val userId: Int = 0,
    val nickName: String = "",
    val phoneNumber: String = "",
    val isoRegionName: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarGuid: String = "",
    val password: String = ""
)