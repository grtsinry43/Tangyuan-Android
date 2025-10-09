package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.CreateCommentDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun getCommentsForPost(postId: Int): Flow<List<Comment>> = flow {
        val response = apiInterface.getCommentForPost(postId).awaitResponse()
        
        // 处理404情况（没有评论时API返回404）
        if (response.code() == 404) {
            emit(emptyList())
            return@flow
        }
        
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get comments: ${response.message()}")
        }
    }
    
    fun getCommentById(commentId: Int): Flow<Comment> = flow {
        val response = apiInterface.getComment(commentId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Comment not found")
        } else {
            throw Exception("Failed to get comment: ${response.message()}")
        }
    }
    
    fun getSubComments(parentCommentId: Int): Flow<List<Comment>> = flow {
        val response = apiInterface.getSubComment(parentCommentId).awaitResponse()
        
        // 处理404情况（没有子评论时API返回404）
        if (response.code() == 404) {
            emit(emptyList())
            return@flow
        }
        
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get sub comments: ${response.message()}")
        }
    }
    
    fun createComment(createCommentDto: CreateCommentDto): Flow<Map<String, String>> = flow {
        val response = apiInterface.postComment(createCommentDto).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to create comment: ${response.message()}")
        }
    }
    
    fun deleteComment(commentId: Int): Flow<Boolean> = flow {
        val response = apiInterface.deleteComment(commentId).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Failed to delete comment: ${response.message()}")
        }
    }
    
    fun searchComments(keyword: String): Flow<List<Comment>> = flow {
        val response = apiInterface.searchCommentByKeyword(keyword).awaitResponse()
        // 404 视为未找到结果
        if (response.code() == 404) {
            emit(emptyList())
            return@flow
        }
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Search failed: ${response.message()}")
        }
    }
}