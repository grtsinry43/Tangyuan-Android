package com.qingshuige.tangyuan.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "登录")
    object Talk : Screen("talk", "聊一聊")
    object Topic : Screen("topic", "侃一侃")
    object Message : Screen("message", "消息")
    object User : Screen("settings", "我的")
    object PostDetail : Screen("post_detail/{postId}/{mode}", "帖子详情") {
        fun createRoute(postId: Int, mode: String = "text") = "post_detail/$postId/$mode"
    }
    object ImageDetail : Screen("image_detail/{postId}/{imageIndex}", "图片详情") {
        fun createRoute(postId: Int, imageIndex: Int) = "image_detail/$postId/$imageIndex"
    }
}