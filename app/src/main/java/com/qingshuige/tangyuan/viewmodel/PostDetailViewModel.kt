package com.qingshuige.tangyuan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.CommentCard
import com.qingshuige.tangyuan.model.CreateCommentDto
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.PostDetailState
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.PostDetailRepository
import com.qingshuige.tangyuan.utils.ImageSaveUtils
import com.qingshuige.tangyuan.utils.UIUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postDetailRepository: PostDetailRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val tokenManager = TokenManager()
    
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()
    
    private var currentPostId: Int = 0
    private var currentUserId: Int = 0
    
    /**
     * 加载帖子详情和评论 - 分离加载，先加载帖子再加载评论
     */
    fun loadPostDetail(postId: Int) {
        currentPostId = postId
        currentUserId = tokenManager.getUserIdFromToken() ?: 0
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // 先加载帖子详情，立即更新UI
                postDetailRepository.getPostCard(postId)
                    .catch { e ->
                        val friendlyMessage = when {
                            e.message?.contains("404", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                            e.message?.contains("timeout", ignoreCase = true) == true -> "网络连接超时，请检查网络设置"
                            e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                            e.message?.contains("connection", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                            e.message?.contains("host", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                            e.message?.contains("post body", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                            e.message?.contains("deleted", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                            e.message?.contains("not found", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                            else -> "这个帖子可能不存在或已删除"
                        }
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = friendlyMessage
                        )
                    }
                    .collect { postCard ->
                        // 立即更新帖子数据，确保共享元素有目标
                        _state.value = _state.value.copy(
                            postCard = postCard,
                            isLoading = false
                        )
                        
                        // 然后异步加载评论
                        loadComments(postId, currentUserId)
                    }
            } catch (e: Exception) {
                val friendlyMessage = when {
                    e.message?.contains("404", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "网络连接超时，请检查网络设置"
                    e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    e.message?.contains("connection", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    e.message?.contains("host", ignoreCase = true) == true -> "网络连接失败，请检查网络设置"
                    e.message?.contains("post body", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                    e.message?.contains("deleted", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                    e.message?.contains("not found", ignoreCase = true) == true -> "这个帖子可能不存在或已删除"
                    else -> "这个帖子可能不存在或已删除"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = friendlyMessage
                )
            }
        }
    }
    
    /**
     * 加载评论数据
     */
    private fun loadComments(postId: Int, userId: Int = 0) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true) // 这里的loading只影响评论区
            
            postDetailRepository.getCommentCardsForPost(postId, userId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载评论失败"
                    )
                }
                .collect { commentCards ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        comments = commentCards,
                        error = null
                    )
                }
        }
    }
    
    /**
     * 刷新帖子详情
     */
    fun refreshPostDetail() {
        if (currentPostId == 0) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            
            postDetailRepository.refreshPostDetail(currentPostId, currentUserId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = e.message ?: "刷新失败"
                    )
                }
                .collect { (postCard, commentCards) ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        postCard = postCard,
                        comments = commentCards,
                        error = null
                    )
                }
        }
    }
    
    /**
     * 发布新评论
     */
    fun createComment(content: String, parentCommentId: Int = 0) {
        if (currentPostId == 0 || content.isBlank()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isCreatingComment = true,
                commentError = null
            )
            
            val createCommentDto = CreateCommentDto(
                postId = currentPostId.toLong(),
                commentDateTime = Date(),
                content = content,
                parentCommentId = if (parentCommentId == 0) 0L else parentCommentId.toLong(),
                userId = currentUserId.toLong()
            )
            
            postDetailRepository.createComment(createCommentDto)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isCreatingComment = false,
                        commentError = e.message ?: "评论发布失败"
                    )
                    UIUtils.showError(e.message ?: "评论发布失败")
                }
                .collect { message ->
                    _state.value = _state.value.copy(
                        isCreatingComment = false,
                        replyToComment = null,
                        commentError = null
                    )
                    UIUtils.showSuccess("评论发布成功")
                    // 评论发布成功后，刷新评论列表
                    refreshComments()
                }
        }
    }
    
    /**
     * 删除评论
     */
    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            postDetailRepository.deleteComment(commentId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "删除评论失败"
                    )
                    UIUtils.showError(e.message ?: "删除评论失败")
                }
                .collect { success ->
                    if (success) {
                        UIUtils.showSuccess("评论已删除")
                        // 删除成功后，从本地列表中移除该评论
                        val updatedComments = _state.value.comments.filter { comment ->
                            comment.commentId != commentId &&
                            comment.replies.none { it.commentId == commentId }
                        }.map { comment ->
                            comment.copy(
                                replies = comment.replies.filter { it.commentId != commentId }
                            )
                        }

                        _state.value = _state.value.copy(comments = updatedComments)
                    }
                }
        }
    }
    
    /**
     * 设置回复目标评论
     */
    fun setReplyToComment(commentCard: CommentCard?) {
        _state.value = _state.value.copy(replyToComment = commentCard)
    }
    
    /**
     * 刷新评论列表
     */
    private fun refreshComments() {
        if (currentPostId == 0) return
        
        viewModelScope.launch {
            postDetailRepository.getCommentCardsForPost(currentPostId, currentUserId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "刷新评论失败"
                    )
                }
                .collect { commentCards ->
                    _state.value = _state.value.copy(comments = commentCards)
                }
        }
    }
    
    /**
     * 加载更多评论
     */
    fun loadMoreComments() {
        // TODO: 实现分页加载评论
        // 当前实现一次加载所有评论，后续可以根据需要实现分页
    }
    
    /**
     * 展开/收起评论回复
     */
    fun toggleReplies(commentId: Int) {
        viewModelScope.launch {
            val updatedComments = _state.value.comments.map { comment ->
                if (comment.commentId == commentId) {
                    if (comment.replies.isEmpty() && comment.replyCount > 0) {
                        // 加载回复
                        loadRepliesForComment(commentId)
                        comment
                    } else {
                        // 切换显示状态（这里简单处理，实际可能需要添加展开状态字段）
                        comment
                    }
                } else {
                    comment
                }
            }
            _state.value = _state.value.copy(comments = updatedComments)
        }
    }
    
    /**
     * 加载特定评论的回复
     */
    private fun loadRepliesForComment(commentId: Int) {
        viewModelScope.launch {
            postDetailRepository.getReplyCardsForComment(commentId, currentUserId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "加载回复失败"
                    )
                }
                .collect { replyCards ->
                    val updatedComments = _state.value.comments.map { comment ->
                        if (comment.commentId == commentId) {
                            comment.copy(replies = replyCards)
                        } else {
                            comment
                        }
                    }
                    _state.value = _state.value.copy(comments = updatedComments)
                }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * 清除评论错误状态
     */
    fun clearCommentError() {
        _state.value = _state.value.copy(commentError = null)
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _state.value = PostDetailState()
        currentPostId = 0
        currentUserId = 0
    }
    
    /**
     * 保存当前图片到本地
     */
    fun saveCurrentImage(imageUrl: String) {
        viewModelScope.launch {
            try {
                val result = ImageSaveUtils.saveImageToGallery(context, imageUrl)
                result.onSuccess { message ->
                    _state.value = _state.value.copy(
                        error = null,
                        saveMessage = message
                    )
                }.onFailure { exception ->
                    _state.value = _state.value.copy(
                        error = exception.message ?: "保存图片失败"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "保存图片失败"
                )
            }
        }
    }
    
    /**
     * 清除保存消息
     */
    fun clearSaveMessage() {
        _state.value = _state.value.copy(saveMessage = null)
    }
}