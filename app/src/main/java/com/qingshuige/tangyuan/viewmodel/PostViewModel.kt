package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatPostMetadataDto
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.repository.PostRepository
import com.qingshuige.tangyuan.repository.UserRepository
import com.qingshuige.tangyuan.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val categoryRepository: CategoryRepository
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
                var metadata: PostMetadata? = null
                var body: PostBody? = null
                var author: User? = null
                var category: Category? = null
                
                postRepository.getPostMetadata(postId)
                    .catch { e -> throw e }
                    .collect { postMetadata ->
                        metadata = postMetadata
                        
                        // Get post body
                        postRepository.getPostBody(postId)
                            .catch { e -> throw e }
                            .collect { postBody ->
                                body = postBody
                            }
                        
                        // Get author
                        userRepository.getUserById(postMetadata.userId)
                            .catch { e -> throw e }
                            .collect { user ->
                                author = user
                            }
                        
                        // Get category
                        categoryRepository.getCategoryById(postMetadata.categoryId)
                            .catch { e -> throw e }
                            .collect { cat ->
                                category = cat
                            }
                        
                        val postDetail = PostDetail(metadata!!, body!!, author, category)
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
                var postId: Int? = null
                
                postRepository.createPostMetadata(metadata)
                    .catch { e -> throw e }
                    .collect { id ->
                        postId = id
                        
                        postRepository.createPostBody(body.copy(postId = id))
                            .catch { e -> throw e }
                            .collect { success ->
                                if (success) {
                                    _postUiState.value = _postUiState.value.copy(
                                        isCreating = false,
                                        createSuccess = true
                                    )
                                } else {
                                    throw Exception("Failed to create post body")
                                }
                            }
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