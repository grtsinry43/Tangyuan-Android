package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.CreateCommentDto
import com.qingshuige.tangyuan.repository.CommentRepository
import com.qingshuige.tangyuan.utils.collectFlow
import com.qingshuige.tangyuan.utils.collectFlowList
import com.qingshuige.tangyuan.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentUiState(
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val subComments: Map<Int, List<Comment>> = emptyMap(),
    val error: String? = null,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    private val _commentUiState = MutableStateFlow(CommentUiState())
    val commentUiState: StateFlow<CommentUiState> = _commentUiState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Comment>>(emptyList())
    val searchResults: StateFlow<List<Comment>> = _searchResults.asStateFlow()
    
    fun getCommentsForPost(postId: Int) {
        viewModelScope.launch {
            _commentUiState.value = _commentUiState.value.copy(isLoading = true, error = null)
            commentRepository.getCommentsForPost(postId)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { comments ->
                    _commentUiState.value = _commentUiState.value.copy(
                        isLoading = false,
                        comments = comments
                    )
                }
        }
    }
    
    fun getSubComments(parentCommentId: Int) {
        viewModelScope.launch {
            commentRepository.getSubComments(parentCommentId)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(error = e.message)
                }
                .collect { subComments ->
                    val currentSubComments = _commentUiState.value.subComments.toMutableMap()
                    currentSubComments[parentCommentId] = subComments
                    _commentUiState.value = _commentUiState.value.copy(
                        subComments = currentSubComments
                    )
                }
        }
    }
    
    fun getCommentById(commentId: Int) {
        viewModelScope.launch {
            commentRepository.getCommentById(commentId)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(error = e.message)
                }
                .collect { comment ->
                    // Handle single comment result
                }
        }
    }
    
    fun createComment(createCommentDto: CreateCommentDto) {
        viewModelScope.launch {
            _commentUiState.value = _commentUiState.value.copy(isCreating = true, error = null)
            commentRepository.createComment(createCommentDto)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(
                        isCreating = false,
                        error = e.message
                    )
                }
                .collect { result ->
                    _commentUiState.value = _commentUiState.value.copy(
                        isCreating = false,
                        createSuccess = true
                    )
                    // Refresh comments for the post
                    getCommentsForPost(createCommentDto.postId.toInt())
                }
        }
    }
    
    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(error = e.message)
                }
                .collect { success ->
                    if (success) {
                        // Remove from current comments list
                        val updatedComments = _commentUiState.value.comments.filter { 
                            it.commentId != commentId 
                        }
                        _commentUiState.value = _commentUiState.value.copy(comments = updatedComments)
                        
                        // Also remove from sub-comments if exists
                        val updatedSubComments = _commentUiState.value.subComments.mapValues { entry ->
                            entry.value.filter { it.commentId != commentId }
                        }
                        _commentUiState.value = _commentUiState.value.copy(subComments = updatedSubComments)
                    }
                }
        }
    }
    
    fun searchComments(keyword: String) {
        viewModelScope.launch {
            commentRepository.searchComments(keyword)
                .catch { e ->
                    _commentUiState.value = _commentUiState.value.copy(error = e.message)
                }
                .collect { comments ->
                    _searchResults.value = comments
                }
        }
    }
    
    fun clearError() {
        _commentUiState.value = _commentUiState.value.copy(error = null)
    }
    
    fun clearCreateSuccess() {
        _commentUiState.value = _commentUiState.value.copy(createSuccess = false)
    }
    
    fun clearComments() {
        _commentUiState.value = CommentUiState()
    }
}