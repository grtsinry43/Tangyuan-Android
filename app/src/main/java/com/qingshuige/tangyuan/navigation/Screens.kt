package com.qingshuige.tangyuan.navigation

sealed class Screen(val route: String, val title: String) {
    object Talk : Screen("talk", "聊一聊")
    object Topic : Screen("topic", "侃一侃")
    object Message : Screen("message", "消息")
    object User : Screen("settings", "我的")
}