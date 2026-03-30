package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.NewNotification
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.CommentRepository
import com.qingshuige.tangyuan.repository.NotificationRepository
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

data class NotificationWithUserAndComment(
    val notification: NewNotification,
    val user: User?,
    val comment: Comment?,
    val postId: Int? // 根据通知类型解析出的帖子 ID
)

enum class NotificationCategory(val label: String) {
    ALL("全部"),
    COMMENT("评论"),
    REPLY("回复"),
    SOCIAL("赞与关注"),
    OTHER("其他");

    fun matches(item: NotificationWithUserAndComment): Boolean {
        if (this == ALL) return true
        return when (item.notification.type.orEmpty()) {
            "comment" -> this == COMMENT
            "reply" -> this == REPLY
            "like", "follow" -> this == SOCIAL
            else -> this == OTHER
        }
    }
}

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notificationsWithData: List<NotificationWithUserAndComment> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val isMarkingAsRead: Boolean = false,
    val selectedCategory: NotificationCategory = NotificationCategory.ALL
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val tokenManager: TokenManager
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
                    // 追踪失败
                    try {
                        val userId = tokenManager.getUserIdFromToken()?.toString()
                        OpenPanelClient.getInstance().track("load_notification_fail", mapOf(
                            "error" to (e.message ?: "unknown")
                        ), userId = userId)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
                }
                .collect { notifications ->
                    val notificationsWithData = supervisorScope {
                        notifications.map { notification ->
                            async { processNotification(notification) }
                        }.awaitAll()
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
            "comment", "reply" -> buildCommentNotification(notification)
            "like" -> NotificationWithUserAndComment(
                notification = notification,
                user = null,
                comment = null,
                postId = notification.sourceId
            )
            "follow" -> NotificationWithUserAndComment(
                notification = notification,
                user = fetchUser(notification.sourceId),
                comment = null,
                postId = null
            )
            else -> NotificationWithUserAndComment(
                notification = notification,
                user = fetchUser(notification.sourceId),
                comment = null,
                postId = null
            )
        }
    }

    private suspend fun buildCommentNotification(notification: NewNotification): NotificationWithUserAndComment {
        val comment = fetchComment(notification.sourceId)
        val user = comment?.let { fetchUser(it.userId) }
        return NotificationWithUserAndComment(
            notification = notification,
            user = user,
            comment = comment,
            postId = comment?.postId
        )
    }

    private suspend fun fetchUser(userId: Int): User? {
        return runCatching {
            userRepository.getUserById(userId).first()
        }.getOrNull()
    }

    private suspend fun fetchComment(commentId: Int): Comment? {
        return runCatching {
            commentRepository.getCommentById(commentId).first()
        }.getOrNull()
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
                    // 追踪失败
                    try {
                        val userId = tokenManager.getUserIdFromToken()?.toString()
                        OpenPanelClient.getInstance().track("notification_mark_read_fail", mapOf(
                            "error" to (e.message ?: "unknown")
                        ), userId = userId)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
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

    fun markAllAsRead() {
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

    fun selectCategory(category: NotificationCategory) {
        _notificationUiState.value = _notificationUiState.value.copy(selectedCategory = category)
    }

    fun clearNotifications() {
        _notificationUiState.value = NotificationUiState()
    }
}
