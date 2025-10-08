package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatePostDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatePostRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flow<List<Category>> = flow {
        try {
            val response = apiInterface.getAllCategories().awaitResponse()
            if (response.isSuccessful) {
                response.body()?.let { emit(it) } 
                    ?: emit(emptyList())
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }
    
    /**
     * 创建新帖子
     * 1. 先创建PostMetadata获取postId
     * 2. 再创建PostBody
     */
    suspend fun createPost(createPostDto: CreatePostDto, userId: Int): Result<Int> {
        return try {
            // 1. 创建PostMetadata
            val metadataDto = createPostDto.toCreatPostMetadataDto(userId)
            val metadataResponse = apiInterface.postPostMetadata(metadataDto).awaitResponse()
            
            if (!metadataResponse.isSuccessful) {
                return Result.failure(Exception("Failed to create post metadata: ${metadataResponse.message()}"))
            }
            
            val postId = metadataResponse.body()?.get("postId") 
                ?: return Result.failure(Exception("No post ID returned"))
            
            // 2. 创建PostBody
            val postBody = createPostDto.toPostBody(postId)
            val bodyResponse = apiInterface.postPostBody(postBody).awaitResponse()
            
            if (!bodyResponse.isSuccessful) {
                return Result.failure(Exception("Failed to create post body: ${bodyResponse.message()}"))
            }
            
            Result.success(postId)
            
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}