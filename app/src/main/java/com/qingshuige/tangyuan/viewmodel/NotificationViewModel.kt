package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.NewNotification
import com.qingshuige.tangyuan.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<NewNotification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val isMarkingAsRead: Boolean = false
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _notificationUiState = MutableStateFlow(NotificationUiState())
    val notificationUiState: StateFlow<NotificationUiState> = _notificationUiState.asStateFlow()
    
    fun getAllNotifications(userId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository getAllNotifications method
                // val notifications = notificationRepository.getAllNotifications(userId)
                // val unreadCount = notifications.count { !it.isRead }
                // _notificationUiState.value = _notificationUiState.value.copy(
                //     isLoading = false,
                //     notifications = notifications,
                //     unreadCount = unreadCount
                // )
            } catch (e: Exception) {
                _notificationUiState.value = _notificationUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isMarkingAsRead = true)
            try {
                // TODO: Call repository markAsRead method
                // notificationRepository.markAsRead(notificationId)
                
                // Update local state
                val updatedNotifications = _notificationUiState.value.notifications.map { notification ->
                    if (notification.notificationId == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                
                val newUnreadCount = updatedNotifications.count { !it.isRead }
                
                _notificationUiState.value = _notificationUiState.value.copy(
                    isMarkingAsRead = false,
                    notifications = updatedNotifications,
                    unreadCount = newUnreadCount
                )
            } catch (e: Exception) {
                _notificationUiState.value = _notificationUiState.value.copy(
                    isMarkingAsRead = false,
                    error = e.message
                )
            }
        }
    }
    
    fun markAllAsRead(userId: Int) {
        viewModelScope.launch {
            _notificationUiState.value = _notificationUiState.value.copy(isMarkingAsRead = true)
            try {
                // Mark all unread notifications as read
                val unreadNotifications = _notificationUiState.value.notifications.filter { !it.isRead }
                
                // TODO: Call repository for each unread notification or batch operation
                // unreadNotifications.forEach { notification ->
                //     notificationRepository.markAsRead(notification.notificationId)
                // }
                
                // Update local state
                val updatedNotifications = _notificationUiState.value.notifications.map { notification ->
                    notification.copy(isRead = true)
                }
                
                _notificationUiState.value = _notificationUiState.value.copy(
                    isMarkingAsRead = false,
                    notifications = updatedNotifications,
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
    
    fun getUnreadNotifications(): List<NewNotification> {
        return _notificationUiState.value.notifications.filter { !it.isRead }
    }
    
    fun getReadNotifications(): List<NewNotification> {
        return _notificationUiState.value.notifications.filter { it.isRead }
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