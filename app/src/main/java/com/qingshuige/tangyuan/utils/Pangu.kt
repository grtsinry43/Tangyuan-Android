package com.qingshuige.tangyuan.utils

import dev.darkokoa.pangu.Pangu

/**
 * 对字符串应用盘古之白格式化
 */
fun String.withPanguSpacing(): String {
    return Pangu.spacingText(this)
}