package com.qingshuige.tangyuan.model

data class Category(
    var categoryId: Int = 0,
    var baseName: String? = null,
    var baseDescription: String? = null
) {
    override fun toString(): String {
        return baseName!!
    }
}
