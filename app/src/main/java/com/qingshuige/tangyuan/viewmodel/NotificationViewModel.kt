package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.NewNotification
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.repository.CommentRepository
import com.qingshuige.tangyuan.repository.NotificationRepository
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationWithUserAndComment(
    val notification: NewNotification,
    val user: User?,
    val comment: Comment?,
    val postId: Int? // 根据通知类型解析出的帖子 ID
)

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notificationsWithData: List<NotificationWithUserAndComment> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val isMarkingAsRead: Boolean = false
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _notificationUiState = MutableStateFlow(NotificationUiState())
    val notificationUiState: StateFlow<NotificationUiState> = _notificationUiState.asStateFlow()

    fun getAllNotifications(userId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isLoading = true, error = null)

            notificationRepository.getAllNotifications(userId)
                .catch { e ->
                    _notificationUiState.value = _notificationUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { notifications ->
                    // 处理每个通知，根据类型获取相应的信息
                    val notificationsWithData = notifications.map { notification ->
                        processNotification(notification)
                    }

                    val unreadCount = notifications.count { !it.isRead }
                    _notificationUiState.value = _notificationUiState.value.copy(
                        isLoading = false,
                        notificationsWithData = notificationsWithData,
                        unreadCount = unreadCount
                    )
                }
        }
    }

    private suspend fun processNotification(notification: NewNotification): NotificationWithUserAndComment {
        return when (notification.sourceType) {
            "comment", "reply" -> {
                // sourceId 是评论 ID，需要获取评论信息
                var comment: Comment? = null
                var user: User? = null
                var postId: Int? = null

                try {
                    commentRepository.getCommentById(notification.sourceId)
                        .catch { /* 忽略错误 */ }
                        .collect { commentData ->
                            comment = commentData
                            postId = commentData.postId

                            // 从评论中获取用户 ID，然后获取用户信息
                            userRepository.getUserById(commentData.userId)
                                .catch { /* 忽略错误 */ }
                                .collect { userData ->
                                    user = userData
                                }
                        }
                } catch (e: Exception) {
                    // 忽略错误，继续处理
                }

                NotificationWithUserAndComment(
                    notification = notification,
                    user = user,
                    comment = comment,
                    postId = postId
                )
            }

            "like" -> {
                // sourceId 是帖子 ID
                NotificationWithUserAndComment(
                    notification = notification,
                    user = null, // 点赞通知可能没有用户信息
                    comment = null,
                    postId = notification.sourceId
                )
            }

            "follow" -> {
                // sourceId 是用户 ID
                var user: User? = null
                try {
                    userRepository.getUserById(notification.sourceId)
                        .catch { /* 忽略错误 */ }
                        .collect { userData ->
                            user = userData
                        }
                } catch (e: Exception) {
                    // 忽略错误
                }

                NotificationWithUserAndComment(
                    notification = notification,
                    user = user,
                    comment = null,
                    postId = null
                )
            }

            else -> {
                // 未知类型，尝试作为用户 ID 处理
                var user: User? = null
                try {
                    userRepository.getUserById(notification.sourceId)
                        .catch { /* 忽略错误 */ }
                        .collect { userData ->
                            user = userData
                        }
                } catch (e: Exception) {
                    // 忽略错误
                }

                NotificationWithUserAndComment(
                    notification = notification,
                    user = user,
                    comment = null,
                    postId = null
                )
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isMarkingAsRead = true)

            notificationRepository.markAsRead(notificationId)
                .catch { e ->
                    _notificationUiState.value = _notificationUiState.value.copy(
                        isMarkingAsRead = false,
                        error = e.message
                    )
                }
                .collect { success ->
                    if (success) {
                        // Update local state
                        val updatedNotifications = _notificationUiState.value.notificationsWithData.map { item ->
                            if (item.notification.notificationId == notificationId) {
                                item.copy(notification = item.notification.copy(isRead = true))
                            } else {
                                item
                            }
                        }

                        val newUnreadCount = updatedNotifications.count { !it.notification.isRead }

                        _notificationUiState.value = _notificationUiState.value.copy(
                            isMarkingAsRead = false,
                            notificationsWithData = updatedNotifications,
                            unreadCount = newUnreadCount
                        )
                    }
                }
        }
    }

    fun markAllAsRead(userId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isMarkingAsRead = true)

            try {
                // Mark all unread notifications as read
                val unreadNotifications = _notificationUiState.value.notificationsWithData
                    .filter { !it.notification.isRead }
                    .map { it.notification }

                unreadNotifications.forEach { notification ->
                    notificationRepository.markAsRead(notification.notificationId)
                        .catch { e ->
                            throw e
                        }
                        .collect { /* Ignore individual results */ }
                }

                // Update local state
                val updatedNotifications = _notificationUiState.value.notificationsWithData.map { item ->
                    item.copy(notification = item.notification.copy(isRead = true))
                }

                _notificationUiState.value = _notificationUiState.value.copy(
                    isMarkingAsRead = false,
                    notificationsWithData = updatedNotifications,
                    unreadCount = 0
                )
            } catch (e: Exception) {
                _notificationUiState.value = _notificationUiState.value.copy(
                    isMarkingAsRead = false,
                    error = e.message
                )
            }
        }
    }

    fun getUnreadCount(): Int {
        return _notificationUiState.value.unreadCount
    }

    fun refreshNotifications(userId: Int) {
        getAllNotifications(userId)
    }

    fun clearError() {
        _notificationUiState.value = _notificationUiState.value.copy(error = null)
    }

    fun clearNotifications() {
        _notificationUiState.value = NotificationUiState()
    }
}