package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 用户信息状态
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // 用户帖子列表状态
    private val _userPosts = MutableStateFlow<List<PostMetadata>>(emptyList())
    val userPosts: StateFlow<List<PostMetadata>> = _userPosts.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 帖子加载状态
    private val _isPostsLoading = MutableStateFlow(false)
    val isPostsLoading: StateFlow<Boolean> = _isPostsLoading.asStateFlow()

    /**
     * 加载用户详细信息
     */
    fun loadUserDetails(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            userRepository.getUserById(userId)
                .catch { e ->
                    _errorMessage.value = e.message ?: "获取用户信息失败"
                    _isLoading.value = false
                }
                .collect { userInfo ->
                    _user.value = userInfo
                    _isLoading.value = false
                    // 获取用户信息成功后，加载用户的帖子
                    loadUserPosts(userId)
                }
        }
    }

    /**
     * 加载用户的帖子列表
     */
    private fun loadUserPosts(userId: Int) {
        viewModelScope.launch {
            _isPostsLoading.value = true
            
            userRepository.getUserPosts(userId)
                .catch { e ->
                    // 帖子加载失败不影响用户信息显示
                    _isPostsLoading.value = false
                }
                .collect { posts ->
                    _userPosts.value = posts
                    _isPostsLoading.value = false
                }
        }
    }

    /**
     * 刷新用户数据
     */
    fun refreshUserData(userId: Int) {
        loadUserDetails(userId)
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 更新用户信息
     */
    fun updateUserInfo(userId: Int, updatedUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            userRepository.updateUser(userId, updatedUser)
                .catch { e ->
                    _errorMessage.value = e.message ?: "更新用户信息失败"
                    _isLoading.value = false
                }
                .collect { success ->
                    if (success) {
                        _user.value = updatedUser
                    }
                    _isLoading.value = false
                }
        }
    }
}