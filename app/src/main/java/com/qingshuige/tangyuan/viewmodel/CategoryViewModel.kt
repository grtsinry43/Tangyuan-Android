package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryStats(
    val postCount: Int = 0,
    val weeklyNewCount: Int = 0,
    val dailyNewCount: Int = 0,
    val sevenDayNewCount: Int = 0
)

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val currentCategory: Category? = null,
    val categoryStats: CategoryStats? = null,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()
    
    private val _categoryStatsMap = MutableStateFlow<Map<Int, CategoryStats>>(emptyMap())
    val categoryStatsMap: StateFlow<Map<Int, CategoryStats>> = _categoryStatsMap.asStateFlow()
    
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
                // val categories = categoryRepository.getAllCategories()
                // _categoryUiState.value = _categoryUiState.value.copy(
                //     isLoading = false,
                //     categories = categories
                // )
                
                // Load stats for each category
                // categories.forEach { category ->
                //     getCategoryStats(category.categoryId)
                // }
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
            categoryStats = null
        )
    }
}