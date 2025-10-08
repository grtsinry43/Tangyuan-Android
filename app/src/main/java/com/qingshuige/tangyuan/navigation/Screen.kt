package com.qingshuige.tangyuan.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "登录")
    object Talk : Screen("talk", "聊一聊")
    object Topic : Screen("topic", "侃一侃")
    object Message : Screen("message", "消息")
    object User : Screen("settings", "我的")
    object About : Screen("about", "关于")

    object DesignSystem : Screen("design_system", "设计系统")
    object PostDetail : Screen("post_detail/{postId}", "帖子详情") {
        fun createRoute(postId: Int) = "post_detail/$postId"
    }
    object ImageDetail : Screen("image_detail/{postId}/{imageIndex}", "图片详情") {
        fun createRoute(postId: Int, imageIndex: Int) = "image_detail/$postId/$imageIndex"
    }
    object UserDetail : Screen("user_detail/{userId}", "用户详情") {
        fun createRoute(userId: Int) = "user_detail/$userId"
    }
}