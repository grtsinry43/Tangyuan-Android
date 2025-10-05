package com.qingshuige.tangyuan.model

data class CreateUserDto(
    val avatarGuid: String? = null,
    val isoRegionName: String? = null,
    val nickName: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null
)
