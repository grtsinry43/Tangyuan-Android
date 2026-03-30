package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.repository.CategoryRepository
import com.qingshuige.tangyuan.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.network.TokenManager
import kotlin.math.min

data class CategoryStats(
    val postCount: Int = 0,
    val weeklyNewCount: Int = 0,
    val dailyNewCount: Int = 0,
    val sevenDayNewCount: Int = 0
)

data class CategoryUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val categories: List<Category> = emptyList(),
    val currentCategory: Category? = null,
    val categoryStats: CategoryStats? = null,
    val posts: List<PostCard> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = false,
    val totalPostsCount: Int = 0
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()
    
    private val _categoryStatsMap = MutableStateFlow<Map<Int, CategoryStats>>(emptyMap())
    val categoryStatsMap: StateFlow<Map<Int, CategoryStats>> = _categoryStatsMap.asStateFlow()

    // 分页相关
    private var allPostMetadatas: List<PostMetadata> = emptyList()
    private var currentPage = 0
    private val pageSize = 10
    
    fun getAllCategories() {
        viewModelScope.launch {
            _categoryUiState.value = _categoryUiState.value.copy(isLoading = true, error = null)
            categoryRepository.getAllCategories()
                .catch { e ->
                    _categoryUiState.value = _categoryUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { categories ->
                    _categoryUiState.value = _categoryUiState.value.copy(
                        isLoading = false,
                        categories = categories
                    )
                }
        }
    }
    
    fun getCategoryById(categoryId: Int) {
        viewModelScope.launch {
            _categoryUiState.value = _categoryUiState.value.copy(isLoading = true, error = null)
            categoryRepository.getCategoryById(categoryId)
                .catch { e ->
                    _categoryUiState.value = _categoryUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { category ->
                    _categoryUiState.value = _categoryUiState.value.copy(
                        isLoading = false,
                        currentCategory = category
                    )
                }
        }
    }
    
    fun getCategoryStats(categoryId: Int) {
        viewModelScope.launch {
            try {
                var postCount = 0
                var weeklyNewCount = 0
                var dailyNewCount = 0
                var sevenDayNewCount = 0
                
                categoryRepository.getPostCountOfCategory(categoryId)
                    .catch { throw it }
                    .collect { count -> postCount = count }
                
                categoryRepository.getWeeklyNewPostCountOfCategory(categoryId)
                    .catch { throw it }
                    .collect { count -> weeklyNewCount = count }
                
                categoryRepository.get24hNewPostCountByCategoryId(categoryId)
                    .catch { throw it }
                    .collect { count -> dailyNewCount = count }
                
                categoryRepository.get7dNewPostCountByCategoryId(categoryId)
                    .catch { throw it }
                    .collect { count -> sevenDayNewCount = count }
                
                val stats = CategoryStats(
                    postCount = postCount,
                    weeklyNewCount = weeklyNewCount,
                    dailyNewCount = dailyNewCount,
                    sevenDayNewCount = sevenDayNewCount
                )
                
                _categoryUiState.value = _categoryUiState.value.copy(categoryStats = stats)
                
                // Also update the stats map
                val updatedStatsMap = _categoryStatsMap.value.toMutableMap()
                updatedStatsMap[categoryId] = stats
                _categoryStatsMap.value = updatedStatsMap
            } catch (e: Exception) {
                _categoryUiState.value = _categoryUiState.value.copy(error = e.message)
            }
        }
    }
    
    fun loadAllCategoriesWithStats() {
        viewModelScope.launch {
            _categoryUiState.value = _categoryUiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Call repository to get all categories
            } catch (e: Exception) {
                _categoryUiState.value = _categoryUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getStatsForCategory(categoryId: Int): CategoryStats? {
        return _categoryStatsMap.value[categoryId]
    }
    
    fun clearError() {
        _categoryUiState.value = _categoryUiState.value.copy(error = null)
    }
    
    fun clearCurrentCategory() {
        _categoryUiState.value = _categoryUiState.value.copy(
            currentCategory = null,
            categoryStats = null,
            posts = emptyList(),
            hasMore = false,
            isLoadingMore = false
        )
        allPostMetadatas = emptyList()
        currentPage = 0
    }

    fun loadCategoryDetail(categoryId: Int) {
        viewModelScope.launch {
            _categoryUiState.value = _categoryUiState.value.copy(isLoading = true, error = null)
            try {
                // Load category info
                categoryRepository.getCategoryById(categoryId)
                    .catch { throw it }
                    .collect { category ->
                        _categoryUiState.value = _categoryUiState.value.copy(
                            currentCategory = category
                        )
                    }

                // Load category stats
                var dailyNewCount = 0
                var sevenDayNewCount = 0

                categoryRepository.get24hNewPostCountByCategoryId(categoryId)
                    .catch { throw it }
                    .collect { count -> dailyNewCount = count }

                categoryRepository.get7dNewPostCountByCategoryId(categoryId)
                    .catch { throw it }
                    .collect { count -> sevenDayNewCount = count }

                val stats = CategoryStats(
                    dailyNewCount = dailyNewCount,
                    sevenDayNewCount = sevenDayNewCount
                )

                _categoryUiState.value = _categoryUiState.value.copy(categoryStats = stats)

                // Load posts (Metadata first)
                postRepository.getPostsByCategory(categoryId)
                    .catch { throw it }
                    .collect { posts ->
                        allPostMetadatas = posts.sortedByDescending { it.postDateTime }
                        currentPage = 0
                        _categoryUiState.value = _categoryUiState.value.copy(
                            posts = emptyList(),
                            isLoading = false,
                            totalPostsCount = allPostMetadatas.size
                        )
                        
                        if (allPostMetadatas.isNotEmpty()) {
                            _categoryUiState.value = _categoryUiState.value.copy(hasMore = true)
                            loadMorePosts() // Load first page
                        } else {
                            _categoryUiState.value = _categoryUiState.value.copy(hasMore = false)
                        }
                    }
            } catch (e: Exception) {
                _categoryUiState.value = _categoryUiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                // 追踪失败
                try {
                    val userId = tokenManager.getUserIdFromToken()?.toString()
                    OpenPanelClient.getInstance().track("load_category_fail", mapOf(
                        "category_id" to categoryId,
                        "error" to (e.message ?: "unknown")
                    ), userId = userId)
                } catch (trackingError: Exception) {
                    // OpenPanel 追踪失败不影响主要功能
                }
            }
        }
    }
    
    fun loadMorePosts() {
        if (_categoryUiState.value.isLoadingMore) return
        if (currentPage * pageSize >= allPostMetadatas.size) return

        viewModelScope.launch {
            _categoryUiState.value = _categoryUiState.value.copy(isLoadingMore = true)
            
            val startIndex = currentPage * pageSize
            val endIndex = min(startIndex + pageSize, allPostMetadatas.size)
            val chunk = allPostMetadatas.subList(startIndex, endIndex)
            val chunkIds = chunk.map { it.postId }

            try {
                postRepository.getPostCards(chunkIds)
                    .catch {
                       throw it
                    }
                    .collect { newCards ->
                        val currentList = _categoryUiState.value.posts.toMutableList()
                        currentList.addAll(newCards)

                        currentPage++
                        val hasMore = currentPage * pageSize < allPostMetadatas.size

                        _categoryUiState.value = _categoryUiState.value.copy(
                            posts = currentList,
                            isLoadingMore = false,
                            hasMore = hasMore
                        )
                    }
            } catch (e: Exception) {
                // 追踪失败
                try {
                    val userId = tokenManager.getUserIdFromToken()?.toString()
                    OpenPanelClient.getInstance().track(
                        "category_load_more_fail", mapOf(
                            "chunk_size" to chunk.size,
                            "chunk_ids" to chunkIds.toString(),
                            "error" to (e.message ?: "unknown")
                        ), userId = userId
                    )
                } catch (trackingError: Exception) {
                    // OpenPanel 追踪失败不影响主要功能
                }
            }
        }
    }
}