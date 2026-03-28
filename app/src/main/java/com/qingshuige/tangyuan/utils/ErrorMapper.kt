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
        isNetworkError(e) -> "网络连接失败，请检查网络设置"
        else -> "网络连接失败，请检查网络设置"
    }

    /**
     * 详情加载场景的错误映射（如帖子详情、评论等）
     */
    fun toDetailErrorMessage(e: Throwable): String = when {
        e.message?.contains("404", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
        e.message?.contains("post body", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
        e.message?.contains("deleted", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
        e.message?.contains("not found", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
        isNetworkError(e) -> "网络连接失败，请检查网络设置"
        else -> "这个帖子可能不存在或已删除"
    }

    private fun isNetworkError(e: Throwable): Boolean {
        val msg = e.message ?: return false
        return msg.contains("timeout", ignoreCase = true) ||
                msg.contains("network", ignoreCase = true) ||
                msg.contains("connection", ignoreCase = true) ||
                msg.contains("host", ignoreCase = true)
    }
}
