package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.PostRepository
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
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 用户信息状态
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // 用户帖子列表状态 - 改为PostCard列表
    private val _userPosts = MutableStateFlow<List<PostCard>>(emptyList())
    val userPosts: StateFlow<List<PostCard>> = _userPosts.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 帖子加载状态
    private val _isPostsLoading = MutableStateFlow(false)
    val isPostsLoading: StateFlow<Boolean> = _isPostsLoading.asStateFlow()

    // 分页相关
    private var allPostMetadatas: List<PostMetadata> = emptyList()
    private var currentPage = 0
    private val pageSize = 10
    
    // 是否还有更多
    private val _hasMorePosts = MutableStateFlow(false)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    // 总帖子数
    private val _totalPostsCount = MutableStateFlow(0)
    val totalPostsCount: StateFlow<Int> = _totalPostsCount.asStateFlow()

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
                    // 追踪失败
                    try {
                        val userId1 = tokenManager.getUserIdFromToken()?.toString()
                        OpenPanelClient.getInstance().track("load_user_info_fail", mapOf(
                            "destUserId" to userId,
                            "error" to (e.message ?: "unknown")
                        ), userId = userId1)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
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
     * 加载用户的帖子列表（Metadata），并初始化第一页
     */
    private fun loadUserPosts(userId: Int) {
        viewModelScope.launch {
            _isPostsLoading.value = true
            
            // 使用 PostRepository 获取 Metadatas
            // 注意：PostRepository.getUserPosts 返回的是 Flow<List<PostMetadata>>
            postRepository.getUserPosts(userId)
                .catch { e ->
                    _isPostsLoading.value = false
                    // 追踪失败
                    try {
                        val userId1 = tokenManager.getUserIdFromToken()?.toString()
                        OpenPanelClient.getInstance().track("load_user_posts_fail", mapOf(
                            "destUserId" to userId,
                            "error" to (e.message ?: "unknown")
                        ), userId = userId1)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
                }
                .collect { posts ->
                    // 保存所有 Metadata，按时间倒序
                    allPostMetadatas = posts.sortedByDescending { it.postDateTime }
                    _totalPostsCount.value = allPostMetadatas.size
                    currentPage = 0
                    _userPosts.value = emptyList()
                    
                    if (allPostMetadatas.isNotEmpty()) {
                        _hasMorePosts.value = true
                        loadMorePosts() // 加载第一页
                    } else {
                        _hasMorePosts.value = false
                        _isPostsLoading.value = false
                    }
                }
        }
    }

    /**
     * 加载下一页帖子详情
     */
    fun loadMorePosts() {
        if (_isPostsLoading.value && currentPage > 0) return // 如果正在加载且不是初始化，则跳过
        if (currentPage * pageSize >= allPostMetadatas.size) return

        viewModelScope.launch {
            _isPostsLoading.value = true
            
            val startIndex = currentPage * pageSize
            val endIndex = minOf(startIndex + pageSize, allPostMetadatas.size)
            val chunk = allPostMetadatas.subList(startIndex, endIndex)
            val chunkIds = chunk.map { it.postId }

            postRepository.getPostCards(chunkIds)
                .catch { 
                     // 局部失败不影响整体流程
                }
                .collect { newCards ->
                    val currentList = _userPosts.value.toMutableList()
                    currentList.addAll(newCards)
                    _userPosts.value = currentList
                    
                    currentPage++
                    _hasMorePosts.value = currentPage * pageSize < allPostMetadatas.size
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