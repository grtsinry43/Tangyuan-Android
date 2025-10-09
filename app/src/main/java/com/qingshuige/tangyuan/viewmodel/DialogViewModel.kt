package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import com.qingshuige.tangyuan.ui.components.ConfirmDialogData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局对话框管理ViewModel
 */
@HiltViewModel
class DialogViewModel @Inject constructor() : ViewModel() {

    private val _currentDialog = MutableStateFlow<ConfirmDialogData?>(null)
    val currentDialog: StateFlow<ConfirmDialogData?> = _currentDialog.asStateFlow()

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
        _currentDialog.value = ConfirmDialogData(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    /**
     * 关闭当前对话框
     */
    fun dismissDialog() {
        _currentDialog.value = null
    }
}

/**
 * 全局对话框管理器单例
 * 提供在非Composable上下文中使用的静态方法
 */
@Singleton
class GlobalDialogManager @Inject constructor() {

    private var viewModel: DialogViewModel? = null

    fun setViewModel(vm: DialogViewModel) {
        viewModel = vm
    }

    fun showConfirmDialog(
        title: String,
        message: String,
        confirmText: String = "确认",
        dismissText: String = "取消",
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        viewModel?.showConfirmDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    fun dismiss() {
        viewModel?.dismissDialog()
    }

    companion object {
        @Volatile
        private var instance: GlobalDialogManager? = null

        fun getInstance(): GlobalDialogManager {
            return instance ?: synchronized(this) {
                instance ?: GlobalDialogManager().also { instance = it }
            }
        }
    }
}
