package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun getAllCategories(): Flow<List<Category>> = flow {
        val response = apiInterface.getAllCategories().awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }
    
    fun getCategoryById(categoryId: Int): Flow<Category> = flow {
        val response = apiInterface.getCategory(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Category not found")
        } else {
            throw Exception("Failed to get category: ${response.message()}")
        }
    }
    
    fun getPostCountOfCategory(categoryId: Int): Flow<Int> = flow {
        val response = apiInterface.getPostCountOfCategory(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(0)
        } else {
            throw Exception("Failed to get post count: ${response.message()}")
        }
    }
    
    fun getWeeklyNewPostCountOfCategory(categoryId: Int): Flow<Int> = flow {
        val response = apiInterface.getWeeklyNewPostCountOfCategory(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(0)
        } else {
            throw Exception("Failed to get weekly new post count: ${response.message()}")
        }
    }
    
    fun get24hNewPostCountByCategoryId(categoryId: Int): Flow<Int> = flow {
        val response = apiInterface.get24hNewPostCountByCategoryId(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(0)
        } else {
            throw Exception("Failed to get 24h new post count: ${response.message()}")
        }
    }
    
    fun get7dNewPostCountByCategoryId(categoryId: Int): Flow<Int> = flow {
        val response = apiInterface.get7dNewPostCountByCategoryId(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
                ?: emit(0)
        } else {
            throw Exception("Failed to get 7d new post count: ${response.message()}")
        }
    }
}