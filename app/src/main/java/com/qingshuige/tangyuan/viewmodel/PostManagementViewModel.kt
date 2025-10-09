package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.PostWithContent
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.PostRepository
import com.qingshuige.tangyuan.utils.UIUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostManagementUiState(
    val isLoading: Boolean = false,
    val posts: List<PostWithContent> = emptyList(),
    val selectedPosts: Set<Int> = emptySet(), // 选中的帖子 ID
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PostManagementViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostManagementUiState())
    val uiState: StateFlow<PostManagementUiState> = _uiState.asStateFlow()

    private val tokenManager = TokenManager()

    init {
        loadUserPosts()
    }

    fun loadUserPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = tokenManager.getUserIdFromToken()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "请先登录"
                )
                return@launch
            }

            postRepository.getUserPosts(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { metadataList ->
                    // 获取每个帖子的内容
                    val postsWithContent = metadataList.map { metadata ->
                        var content = "帖子 #${metadata.postId}"
                        try {
                            postRepository.getPostBody(metadata.postId)
                                .catch { /* 忽略单个帖子内容获取失败 */ }
                                .collect { body ->
                                    content = body.textContent ?: "无内容"
                                }
                        } catch (e: Exception) {
                            // 忽略错误
                        }
                        PostWithContent(metadata, content)
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = postsWithContent
                    )
                }
        }
    }

    fun togglePostSelection(postId: Int) {
        val currentSelected = _uiState.value.selectedPosts
        val newSelected = if (currentSelected.contains(postId)) {
            currentSelected - postId
        } else {
            currentSelected + postId
        }
        _uiState.value = _uiState.value.copy(selectedPosts = newSelected)
    }

    fun selectAll() {
        val allPostIds = _uiState.value.posts.map { it.metadata.postId }.toSet()
        _uiState.value = _uiState.value.copy(selectedPosts = allPostIds)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPosts = emptySet())
    }

    fun deleteSelectedPosts() {
        val selectedPosts = _uiState.value.selectedPosts
        if (selectedPosts.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)

            try {
                var hasError = false
                selectedPosts.forEach { postId ->
                    postRepository.deletePost(postId)
                        .catch { e ->
                            hasError = true
                            _uiState.value = _uiState.value.copy(
                                error = "删除失败: ${e.message}"
                            )
                            UIUtils.showError("删除失败: ${e.message}")
                        }
                        .collect { /* 忽略单个删除结果 */ }
                }

                if (!hasError) {
                    // 删除成功后，刷新列表
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteSuccess = true,
                        selectedPosts = emptySet()
                    )
                    UIUtils.showSuccess("删除成功")
                    loadUserPosts()
                } else {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "删除失败: ${e.message}"
                )
                UIUtils.showError("删除失败: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }
}
