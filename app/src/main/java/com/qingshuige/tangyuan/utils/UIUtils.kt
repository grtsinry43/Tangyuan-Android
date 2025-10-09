package com.qingshuige.tangyuan.utils

import com.qingshuige.tangyuan.viewmodel.GlobalDialogManager
import com.qingshuige.tangyuan.viewmodel.GlobalMessageManager

/**
 * 全局UI工具类
 * 提供便捷的消息提示和对话框调用方法
 *
 * 使用示例：
 * ```kotlin
 * // 在任何地方显示消息
 * UIUtils.showSuccess("操作成功")
 * UIUtils.showWarning("请注意")
 * UIUtils.showError("操作失败")
 *
 * // 显示确认对话框
 * UIUtils.showConfirmDialog(
 *     title = "删除确认",
 *     message = "确定要删除这条记录吗？",
 *     onConfirm = {
 *         // 执行删除操作
 *     }
 * )
 * ```
 */
object UIUtils {

    private val messageManager by lazy { GlobalMessageManager.getInstance() }
    private val dialogManager by lazy { GlobalDialogManager.getInstance() }

    /**
     * 显示成功消息
     * @param text 消息文本
     * @param duration 显示时长（毫秒），默认3000ms
     */
    fun showSuccess(text: String, duration: Long = 3000L) {
        messageManager.showSuccess(text, duration)
    }

    /**
     * 显示警告消息
     * @param text 消息文本
     * @param duration 显示时长（毫秒），默认3000ms
     */
    fun showWarning(text: String, duration: Long = 3000L) {
        messageManager.showWarning(text, duration)
    }

    /**
     * 显示错误消息
     * @param text 消息文本
     * @param duration 显示时长（毫秒），默认3000ms
     */
    fun showError(text: String, duration: Long = 3000L) {
        messageManager.showError(text, duration)
    }

    /**
     * 显示确认对话框
     * @param title 对话框标题
     * @param message 对话框消息内容
     * @param confirmText 确认按钮文本，默认"确认"
     * @param dismissText 取消按钮文本，默认"取消"
     * @param onConfirm 确认回调
     * @param onDismiss 取消回调
     */
    fun showConfirmDialog(
        title: String,
        message: String,
        confirmText: String = "确认",
        dismissText: String = "取消",
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        dialogManager.showConfirmDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    /**
     * 关闭当前显示的消息
     */
    fun dismissMessage() {
        messageManager.dismiss()
    }

    /**
     * 关闭当前显示的对话框
     */
    fun dismissDialog() {
        dialogManager.dismiss()
    }
}

/**
 * Composable扩展函数，用于在Compose上下文中快速调用
 */
object ComposeUIUtils {

    /**
     * 显示成功消息
     */
    fun showSuccess(text: String, duration: Long = 3000L) {
        UIUtils.showSuccess(text, duration)
    }

    /**
     * 显示警告消息
     */
    fun showWarning(text: String, duration: Long = 3000L) {
        UIUtils.showWarning(text, duration)
    }

    /**
     * 显示错误消息
     */
    fun showError(text: String, duration: Long = 3000L) {
        UIUtils.showError(text, duration)
    }

    /**
     * 显示确认对话框
     */
    fun showConfirmDialog(
        title: String,
        message: String,
        confirmText: String = "确认",
        dismissText: String = "取消",
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        UIUtils.showConfirmDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}
