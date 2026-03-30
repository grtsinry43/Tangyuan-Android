package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatPostMetadataDto
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.PostRepository
import com.qingshuige.tangyuan.repository.UserRepository
import com.qingshuige.tangyuan.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetail(
    val metadata: PostMetadata,
    val body: PostBody,
    val author: User? = null,
    val category: Category? = null
)

data class PostUiState(
    val isLoading: Boolean = false,
    val posts: List<PostMetadata> = emptyList(),
    val currentPost: PostDetail? = null,
    val error: String? = null,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _postUiState = MutableStateFlow(PostUiState())
    val postUiState: StateFlow<PostUiState> = _postUiState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<PostMetadata>>(emptyList())
    val searchResults: StateFlow<List<PostMetadata>> = _searchResults.asStateFlow()
    
    private val _noticePost = MutableStateFlow<PostMetadata?>(null)
    val noticePost: StateFlow<PostMetadata?> = _noticePost.asStateFlow()
    
    fun getPostDetail(postId: Int) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val metadataDeferred = async { postRepository.getPostMetadata(postId).first() }
                    val bodyDeferred = async { postRepository.getPostBody(postId).first() }

                    val metadata = metadataDeferred.await()
                    val body = bodyDeferred.await()

                    val authorDeferred = async { userRepository.getUserById(metadata.userId).first() }
                    val categoryDeferred = async { categoryRepository.getCategoryById(metadata.categoryId).first() }

                    val author = try { authorDeferred.await() } catch (_: Exception) { null }
                    val category = try { categoryDeferred.await() } catch (_: Exception) { null }

                    val postDetail = PostDetail(metadata, body, author, category)
                    _postUiState.value = _postUiState.value.copy(
                        isLoading = false,
                        currentPost = postDetail
                    )
                }
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getUserPosts(userId: Int) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository getUserPosts method
                // val posts = postRepository.getUserPosts(userId)
                // _postUiState.value = _postUiState.value.copy(
                //     isLoading = false,
                //     posts = posts
                // )
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getRandomPosts(count: Int) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository getRandomPosts method
                // val posts = postRepository.getRandomPosts(count)
                // _postUiState.value = _postUiState.value.copy(
                //     isLoading = false,
                //     posts = posts
                // )
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getPostsByCategory(categoryId: Int) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository getPostsByCategory method
                // val posts = postRepository.getPostsByCategory(categoryId)
                // _postUiState.value = _postUiState.value.copy(
                //     isLoading = false,
                //     posts = posts
                // )
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun createPost(metadata: CreatPostMetadataDto, body: PostBody) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isCreating = true, error = null)
            try {
                val postId = postRepository.createPostMetadata(metadata).first()
                val success = postRepository.createPostBody(body.copy(postId = postId)).first()
                if (success) {
                    _postUiState.value = _postUiState.value.copy(
                        isCreating = false,
                        createSuccess = true
                    )
                } else {
                    throw Exception("Failed to create post body")
                }
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isCreating = false,
                    error = e.message
                )
            }
        }
    }
    
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                // TODO: Call repository deletePost method
                // postRepository.deletePost(postId)
                // Remove from current posts list
                val updatedPosts = _postUiState.value.posts.filter { it.postId != postId }
                _postUiState.value = _postUiState.value.copy(posts = updatedPosts)
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(error = e.message)
            }
        }
    }
    
    fun searchPosts(keyword: String) {
        viewModelScope.launch {
            try {
                // TODO: Call repository searchPosts method
                // val posts = postRepository.searchPosts(keyword)
                // _searchResults.value = posts
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(error = e.message)
            }
        }
    }
    
    fun getNoticePost() {
        viewModelScope.launch {
            try {
                postRepository.getNoticePost()
                    .catch { e ->
                        _postUiState.value = _postUiState.value.copy(error = e.message)
                    }
                    .collect { notice ->
                        _noticePost.value = notice
                    }
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(error = e.message)
                // 追踪失败
                try {
                    val userId = tokenManager.getUserIdFromToken()?.toString()
                    OpenPanelClient.getInstance().track("get_notice_fail", mapOf(
                        "error" to (e.message ?: "unknown")
                    ), userId = userId)
                } catch (trackingError: Exception) {
                    // OpenPanel 追踪失败不影响主要功能
                }
            }
        }
    }
    
    fun getPhtPostMetadata(sectionId: Int, exceptedIds: List<Int>) {
        viewModelScope.launch {
            _postUiState.value = _postUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository getPhtPostMetadata method
                // val posts = postRepository.getPhtPostMetadata(sectionId, exceptedIds)
                // _postUiState.value = _postUiState.value.copy(
                //     isLoading = false,
                //     posts = posts
                // )
            } catch (e: Exception) {
                _postUiState.value = _postUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun clearError() {
        _postUiState.value = _postUiState.value.copy(error = null)
    }
    
    fun clearCreateSuccess() {
        _postUiState.value = _postUiState.value.copy(createSuccess = false)
    }

    fun clearNoticePost() {
        _noticePost.value = null
    }
}