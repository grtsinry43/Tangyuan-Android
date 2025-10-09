package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import com.qingshuige.tangyuan.ui.components.Message
import com.qingshuige.tangyuan.ui.components.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局消息管理ViewModel
 * 使用单例模式，确保全局只有一个实例
 */
@HiltViewModel
class MessageViewModel @Inject constructor() : ViewModel() {

    private val _currentMessage = MutableStateFlow<Message?>(null)
    val currentMessage: StateFlow<Message?> = _currentMessage.asStateFlow()

    /**
     * 显示成功消息
     */
    fun showSuccess(text: String, duration: Long = 3000L) {
        _currentMessage.value = Message(
            text = text,
            type = MessageType.SUCCESS,
            duration = duration
        )
    }

    /**
     * 显示警告消息
     */
    fun showWarning(text: String, duration: Long = 3000L) {
        _currentMessage.value = Message(
            text = text,
            type = MessageType.WARNING,
            duration = duration
        )
    }

    /**
     * 显示错误消息
     */
    fun showError(text: String, duration: Long = 3000L) {
        _currentMessage.value = Message(
            text = text,
            type = MessageType.ERROR,
            duration = duration
        )
    }

    /**
     * 清除当前消息
     */
    fun dismiss() {
        _currentMessage.value = null
    }
}

/**
 * 全局消息管理器单例
 * 提供在非Composable上下文中使用的静态方法
 */
@Singleton
class GlobalMessageManager @Inject constructor() {

    private var viewModel: MessageViewModel? = null

    fun setViewModel(vm: MessageViewModel) {
        viewModel = vm
    }

    fun showSuccess(text: String, duration: Long = 3000L) {
        viewModel?.showSuccess(text, duration)
    }

    fun showWarning(text: String, duration: Long = 3000L) {
        viewModel?.showWarning(text, duration)
    }

    fun showError(text: String, duration: Long = 3000L) {
        viewModel?.showError(text, duration)
    }

    fun dismiss() {
        viewModel?.dismiss()
    }

    companion object {
        @Volatile
        private var instance: GlobalMessageManager? = null

        fun getInstance(): GlobalMessageManager {
            return instance ?: synchronized(this) {
                instance ?: GlobalMessageManager().also { instance = it }
            }
        }
    }
}
