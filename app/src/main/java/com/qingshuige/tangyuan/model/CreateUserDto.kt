package com.qingshuige.tangyuan.model

data class CreateUserDto(
    var avatarGuid: String? = null,
    var isoRegionName: String? = null,
    var nickName: String? = null,
    var password: String? = null,
    var phoneNumber: String? = null
)
