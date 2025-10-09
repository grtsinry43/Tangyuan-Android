package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.repository.CommentRepository
import com.qingshuige.tangyuan.repository.PostRepository
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val posts: List<PostMetadata> = emptyList(),
    val users: List<User> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val error: String? = null,
    val postCards: Map<Int, PostCard> = emptyMap()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchAll() {
        val keyword = _uiState.value.query.trim()
        if (keyword.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                posts = emptyList(), users = emptyList(), comments = emptyList(), error = null, postCards = emptyMap()
            )
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val postsFlow = postRepository.searchPosts(keyword).catch { e ->
                emit(emptyList())
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            val usersFlow = userRepository.searchUsers(keyword).catch { e ->
                emit(emptyList())
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            val commentsFlow = commentRepository.searchComments(keyword).catch { e ->
                emit(emptyList())
                _uiState.value = _uiState.value.copy(error = e.message)
            }

            combine(postsFlow, usersFlow, commentsFlow) { posts, users, comments ->
                Triple(posts, users, comments)
            }.collect { (posts, users, comments) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    posts = posts,
                    users = users,
                    comments = comments
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun loadPostCard(postId: Int) {
        // 已加载则跳过
        if (_uiState.value.postCards.containsKey(postId)) return

        viewModelScope.launch {
            try {
                postRepository.getPostCard(postId).catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }.collect { card ->
                    val newMap = _uiState.value.postCards.toMutableMap()
                    newMap[postId] = card
                    _uiState.value = _uiState.value.copy(postCards = newMap)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}


