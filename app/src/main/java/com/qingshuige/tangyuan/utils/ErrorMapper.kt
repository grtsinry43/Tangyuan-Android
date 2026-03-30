package com.qingshuige.tangyuan.utils

/**
 * 将异常消息转换为用户友好的错误提示
 */
object ErrorMapper {

    /**
     * 列表加载场景的错误映射（如推荐列表、分类文章列表等）
     */
    fun toListErrorMessage(e: Throwable): String = when {
        e.message?.contains("404", ignoreCase = true) == true -> "暂无更多内容"
        isNetworkError(e) -> "网络连接失败"
        else -> "加载失败"
    }

    /**
     * 详情加载场景的错误映射（如帖子详情、评论等）
     */
    fun toDetailErrorMessage(e: Throwable): String = when {
        e.message?.contains("404", ignoreCase = true) == true -> "帖子不存在"
        e.message?.contains("post body", ignoreCase = true) == true -> "帖子不存在"
        e.message?.contains("deleted", ignoreCase = true) == true -> "帖子不存在"
        e.message?.contains("not found", ignoreCase = true) == true -> "帖子不存在"
        isNetworkError(e) -> "网络连接失败"
        else -> "加载失败"
    }

    /**
     * 用户详情加载场景的错误映射
     */
    fun toUserDetailErrorMessage(e: Throwable): String = when {
        e.message?.contains("404", ignoreCase = true) == true -> "用户不存在"
        e.message?.contains("not found", ignoreCase = true) == true -> "用户不存在"
        e.message?.contains("不存在", ignoreCase = true) == true -> "用户不存在"
        isNetworkError(e) -> "网络连接失败"
        else -> "加载失败"
    }

    /**
     * 为错误标题补充更柔和的副文案
     */
    fun toLiteraryCaption(message: String): String = when {
        message.contains("帖子不存在") ->
            "你似乎来到了一片没有知识的荒原"
        message.contains("用户不存在") ->
            "这里很安静，你要找的人似乎从未来过"
        message.contains("网络连接失败") ->
            "你似乎短暂地回到了没有互联网的时代"
        message.contains("暂无更多内容") ->
            "路已经走到尽头，前面暂时没有新的回声"
        else ->
            "风停在半路，答案暂时还没有抵达"
    }

    private fun isNetworkError(e: Throwable): Boolean {
        val msg = e.message ?: return false
        return msg.contains("timeout", ignoreCase = true) ||
                msg.contains("network", ignoreCase = true) ||
                msg.contains("connection", ignoreCase = true) ||
                msg.contains("host", ignoreCase = true)
    }
}
