package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.NewNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun getAllNotifications(userId: Int): Flow<List<NewNotification>> = flow {
        val response = apiInterface.getAllNotificationsByUserId(userId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
                ?: emit(emptyList())
        } else {
            when (response.code()) {
                404 -> emit(emptyList()) // 404表示没有通知，返回空列表
                else -> throw Exception("Failed to get notifications: ${response.message()}")
            }
        }
    }
    
    fun markAsRead(notificationId: Int): Flow<Boolean> = flow {
        val response = apiInterface.markNewNotificationAsRead(notificationId).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Failed to mark notification as read: ${response.message()}")
        }
    }
}