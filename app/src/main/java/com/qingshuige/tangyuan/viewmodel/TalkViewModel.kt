package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.RecommendedPostsState
import com.qingshuige.tangyuan.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TalkViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecommendedPostsState())
    val uiState: StateFlow<RecommendedPostsState> = _uiState.asStateFlow()
    
    // 已经获取过的文章ID列表，用于避免重复
    private val _loadedPostIds = mutableSetOf<Int>()
    
    // 默认分区ID（聊一聊）
    private val defaultSectionId = 1
    
    init {
        loadRecommendedPosts()
    }
    
    /**
     * 加载推荐文章
     */
    fun loadRecommendedPosts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (isRefresh) {
                    _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
                    _loadedPostIds.clear()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                
                postRepository.getRecommendedPostCards(
                    sectionId = defaultSectionId,
                    exceptedIds = if (isRefresh) emptyList() else _loadedPostIds.toList()
                )
                .catch { e ->
                    val friendlyMessage = when {
                        e.message?.contains("404", ignoreCase = true) == true -> "暂无更多内容"
                        e.message?.contains("timeout", ignoreCase = true) == true -> "网络连接超时，请检查网络设置"
                        e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                        e.message?.contains("connection", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                        e.message?.contains("host", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                        else -> "网络连接失败，请检查网络设置"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = friendlyMessage
                    )
                }
                .collect { newPosts ->
                    // 更新已加载的文章ID
                    _loadedPostIds.addAll(newPosts.map { it.postId })
                    
                    val currentPosts = if (isRefresh) {
                        newPosts
                    } else {
                        _uiState.value.posts + newPosts
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        posts = currentPosts,
                        hasMore = newPosts.isNotEmpty(),
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                val friendlyMessage = when {
                    e.message?.contains("404", ignoreCase = true) == true -> "暂无更多内容"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "网络连接超时，请检查网络设置"
                    e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    e.message?.contains("connection", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    e.message?.contains("host", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    else -> "网络连接失败，请检查网络设置"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = friendlyMessage
                )
            }
        }
    }
    
    /**
     * 刷新文章列表
     */
    fun refreshPosts() {
        loadRecommendedPosts(isRefresh = true)
    }
    
    /**
     * 加载更多文章
     */
    fun loadMorePosts() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        loadRecommendedPosts(isRefresh = false)
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 点赞文章
     */
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            try {
                val currentPosts = _uiState.value.posts.toMutableList()
                val postIndex = currentPosts.indexOfFirst { it.postId == postId }
                
                if (postIndex != -1) {
                    val post = currentPosts[postIndex]
                    val updatedPost = post.copy(
                        isLiked = !post.isLiked,
                        likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                    )
                    currentPosts[postIndex] = updatedPost
                    
                    _uiState.value = _uiState.value.copy(posts = currentPosts)
                    
                    // TODO: 调用API更新点赞状态
                    // likeRepository.toggleLike(postId)
                }
            } catch (e: Exception) {
                // 如果API调用失败，回滚UI状态
                // 这里可以添加错误处理逻辑
            }
        }
    }
    
    /**
     * 收藏文章
     */
    fun toggleBookmark(postId: Int) {
        viewModelScope.launch {
            try {
                val currentPosts = _uiState.value.posts.toMutableList()
                val postIndex = currentPosts.indexOfFirst { it.postId == postId }
                
                if (postIndex != -1) {
                    val post = currentPosts[postIndex]
                    val updatedPost = post.copy(isBookmarked = !post.isBookmarked)
                    currentPosts[postIndex] = updatedPost
                    
                    _uiState.value = _uiState.value.copy(posts = currentPosts)
                    
                    // TODO: 调用API更新收藏状态
                    // bookmarkRepository.toggleBookmark(postId)
                }
            } catch (e: Exception) {
                // 如果API调用失败，回滚UI状态
            }
        }
    }
    
    /**
     * 获取单个文章详情（用于点击跳转）
     */
    fun getPostDetail(postId: Int): PostCard? {
        return _uiState.value.posts.find { it.postId == postId }
    }
    
    /**
     * 分享文章
     */
    fun sharePost(postId: Int) {
        viewModelScope.launch {
            try {
                val currentPosts = _uiState.value.posts.toMutableList()
                val postIndex = currentPosts.indexOfFirst { it.postId == postId }
                
                if (postIndex != -1) {
                    val post = currentPosts[postIndex]
                    val updatedPost = post.copy(shareCount = post.shareCount + 1)
                    currentPosts[postIndex] = updatedPost
                    
                    _uiState.value = _uiState.value.copy(posts = currentPosts)
                    
                    // TODO: 调用分享相关的逻辑
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    /**
     * 举报文章
     */
    fun reportPost(postId: Int, reason: String) {
        viewModelScope.launch {
            try {
                // TODO: 调用举报API
                // reportRepository.reportPost(postId, reason)
                
                // 暂时从列表中移除被举报的文章
                val currentPosts = _uiState.value.posts.filter { it.postId != postId }
                _uiState.value = _uiState.value.copy(posts = currentPosts)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "举报失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 关注/取消关注作者
     */
    fun toggleFollowAuthor(authorId: Int) {
        viewModelScope.launch {
            try {
                // TODO: 调用关注API
                // followRepository.toggleFollow(authorId)
                
                // 更新UI中该作者的所有文章状态
                // 这里可以添加相关逻辑
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "操作失败: ${e.message}"
                )
            }
        }
    }
}