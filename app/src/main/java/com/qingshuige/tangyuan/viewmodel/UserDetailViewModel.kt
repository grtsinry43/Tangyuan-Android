package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
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
                    loadUserPosts(userId, userInfo)
                }
        }
    }

    /**
     * 加载用户的帖子列表，包含完整的PostCard信息
     */
    private fun loadUserPosts(userId: Int, user: User) {
        viewModelScope.launch {
            _isPostsLoading.value = true
            
            userRepository.getUserPosts(userId)
                .catch { e ->
                    // 帖子加载失败不影响用户信息显示
                    _isPostsLoading.value = false
                }
                .collect { posts ->
                    // 并行获取每个帖子的完整信息
                    val postCards = posts.map { postMetadata ->
                        async {
                            try {
                                postMetadata.toPostCard(user, userRepository)
                            } catch (e: Exception) {
                                // 如果获取详细信息失败，返回简化版本
                                postMetadata.toSimplePostCard(user)
                            }
                        }
                    }.map { it.await() }
                    
                    // 按时间倒序排序，新的在前面
                    _userPosts.value = postCards.sortedByDescending { it.postDateTime }
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

/**
 * PostMetadata扩展函数：转换为完整的PostCard
 */
private suspend fun PostMetadata.toPostCard(
    author: User,
    userRepository: UserRepository
): PostCard = kotlinx.coroutines.coroutineScope {
    return@coroutineScope try {
        // 并行获取PostBody和Category
        val postBodyDeferred = async {
            var result: PostBody? = null
            userRepository.getPostBody(this@toPostCard.postId)
                .catch { /* 忽略错误，使用默认null值 */ }
                .collect { result = it }
            result
        }
        val categoryDeferred = async {
            var result: Category? = null
            userRepository.getCategory(this@toPostCard.categoryId)
                .catch { /* 忽略错误，使用默认null值 */ }
                .collect { result = it }
            result
        }
        
        val postBody = postBodyDeferred.await()
        val category = categoryDeferred.await()
        
        // 提取图片UUID列表
        val imageUUIDs = listOfNotNull(
            postBody?.image1UUID,
            postBody?.image2UUID,
            postBody?.image3UUID
        ).filter { it.isNotBlank() }
        
        PostCard(
            postId = this@toPostCard.postId,
            postDateTime = this@toPostCard.postDateTime,
            isVisible = this@toPostCard.isVisible,
            
            authorId = author.userId,
            authorName = author.nickName.ifBlank { "匿名用户" },
            authorAvatar = author.avatarGuid,
            authorBio = author.bio ?: "",
            
            categoryId = this@toPostCard.categoryId,
            categoryName = category?.baseName ?: "未分类",
            categoryDescription = category?.baseDescription ?: "",
            
            textContent = postBody?.textContent ?: "内容获取失败",
            imageUUIDs = imageUUIDs,
            hasImages = imageUUIDs.isNotEmpty(),
            
            // 默认互动数据
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            isLiked = false,
            isBookmarked = false
        )
    } catch (e: Exception) {
        // 如果获取详细信息失败，返回简化版本
        this@toPostCard.toSimplePostCard(author)
    }
}

/**
 * PostMetadata扩展函数：转换为简化的PostCard（作为备用）
 */
private fun PostMetadata.toSimplePostCard(author: User): PostCard {
    return PostCard(
        postId = this.postId,
        postDateTime = this.postDateTime,
        isVisible = this.isVisible,
        
        authorId = author.userId,
        authorName = author.nickName.ifBlank { "匿名用户" },
        authorAvatar = author.avatarGuid,
        authorBio = author.bio ?: "",
        
        categoryId = this.categoryId,
        categoryName = "分类 ${this.categoryId}", // 简化显示
        categoryDescription = "",
        
        textContent = "点击查看完整内容...",
        imageUUIDs = emptyList(),
        hasImages = false,
        
        // 默认互动数据
        likeCount = 0,
        commentCount = 0,
        shareCount = 0,
        isLiked = false,
        isBookmarked = false
    )
}