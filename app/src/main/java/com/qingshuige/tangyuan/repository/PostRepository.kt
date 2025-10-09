package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatPostMetadataDto
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.model.toPostCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun getPostMetadata(postId: Int): Flow<PostMetadata> = flow {
        val response = apiInterface.getPostMetadata(postId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Post metadata not found")
        } else {
            throw Exception("Failed to get post metadata: ${response.message()}")
        }
    }
    
    fun getPostBody(postId: Int): Flow<PostBody> = flow {
        val response = apiInterface.getPostBody(postId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Post body not found")
        } else {
            throw Exception("Failed to get post body: ${response.message()}")
        }
    }
    
    fun getUserPosts(userId: Int): Flow<List<PostMetadata>> = flow {
        val response = apiInterface.getMetadatasByUserID(userId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get user posts: ${response.message()}")
        }
    }
    
    @Deprecated("Use getPhtPostMetadata instead")
    fun getRandomPosts(count: Int): Flow<List<PostMetadata>> = flow {
        val response = apiInterface.getRandomPostMetadata(count).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get random posts: ${response.message()}")
        }
    }
    
    fun getPhtPostMetadata(sectionId: Int, exceptedIds: List<Int>): Flow<List<PostMetadata>> = flow {
        val response = apiInterface.phtPostMetadata(sectionId, exceptedIds).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get pht post metadata: ${response.message()}")
        }
    }
    
    /**
     * 获取推荐文章卡片 - 聚合完整数据
     * 这是聊一聊页面的核心方法，会并行获取所有相关数据
     */
    fun getRecommendedPostCards(
        sectionId: Int, 
        exceptedIds: List<Int> = emptyList()
    ): Flow<List<PostCard>> = flow {
        try {
            // 1. 获取推荐文章列表
            val metadataResponse = apiInterface.phtPostMetadata(sectionId, exceptedIds).awaitResponse()
            if (!metadataResponse.isSuccessful) {
                throw Exception("Failed to get recommended posts: ${metadataResponse.message()}")
            }
            
            val postMetadataList = metadataResponse.body() ?: emptyList()
            if (postMetadataList.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // 2. 并行获取所有相关数据
            val postCards = coroutineScope {
                postMetadataList.map { metadata ->
                    async {
                        try {
                            // 并行获取用户、分类、文章内容
                            val userDeferred = async {
                                val userResponse = apiInterface.getUser(metadata.userId).awaitResponse()
                                userResponse.body() ?: User(userId = metadata.userId, nickName = "未知用户")
                            }
                            
                            val categoryDeferred = async {
                                val categoryResponse = apiInterface.getCategory(metadata.categoryId).awaitResponse()
                                categoryResponse.body() ?: Category(categoryId = metadata.categoryId, baseName = "未分类")
                            }
                            
                            val bodyDeferred = async {
                                val bodyResponse = apiInterface.getPostBody(metadata.postId).awaitResponse()
                                bodyResponse.body() ?: PostBody(postId = metadata.postId, textContent = "内容加载失败")
                            }
                            
                            // 等待所有数据获取完成
                            val user = userDeferred.await()
                            val category = categoryDeferred.await()
                            val body = bodyDeferred.await()
                            
                            // 转换为PostCard
                            metadata.toPostCard(user, category, body)
                        } catch (e: Exception) {
                            // 单个文章失败不影响整体，创建一个错误状态的卡片
                            PostCard(
                                postId = metadata.postId,
                                postDateTime = metadata.postDateTime,
                                isVisible = metadata.isVisible,
                                authorId = metadata.userId,
                                authorName = "加载失败",
                                authorAvatar = "",
                                categoryId = metadata.categoryId,
                                categoryName = "未知分类",
                                textContent = "内容加载失败: ${e.message}"
                            )
                        }
                    }
                }.awaitAll()
            }
            
            emit(postCards.filter { it.isVisible }) // 只返回可见的文章
            
        } catch (e: Exception) {
            throw Exception("Failed to get recommended post cards: ${e.message}")
        }
    }
    
    /**
     * 获取单个文章的完整卡片数据
     */
    fun getPostCard(postId: Int): Flow<PostCard> = flow {
        try {
            coroutineScope {
                // 并行获取所有数据
                val metadataDeferred = async {
                    val response = apiInterface.getPostMetadata(postId).awaitResponse()
                    response.body() ?: throw Exception("Post metadata not found")
                }
                
                val bodyDeferred = async {
                    val response = apiInterface.getPostBody(postId).awaitResponse()
                    response.body() ?: throw Exception("Post body not found")
                }
                
                val metadata = metadataDeferred.await()
                val body = bodyDeferred.await()
                
                // 继续并行获取用户和分类信息
                val userDeferred = async {
                    val response = apiInterface.getUser(metadata.userId).awaitResponse()
                    response.body() ?: User(userId = metadata.userId, nickName = "未知用户")
                }
                
                val categoryDeferred = async {
                    val response = apiInterface.getCategory(metadata.categoryId).awaitResponse()
                    response.body() ?: Category(categoryId = metadata.categoryId, baseName = "未分类")
                }
                
                val user = userDeferred.await()
                val category = categoryDeferred.await()
                
                emit(metadata.toPostCard(user, category, body))
            }
        } catch (e: Exception) {
            throw Exception("Failed to get post card: ${e.message}")
        }
    }
    
    fun getPostsByCategory(categoryId: Int): Flow<List<PostMetadata>> = flow {
        val response = apiInterface.getAllMetadatasByCategoryId(categoryId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
                ?: emit(emptyList())
        } else {
            throw Exception("Failed to get posts by category: ${response.message()}")
        }
    }

    /**
     * 获取分类的完整文章卡片列表
     */
    fun getCategoryPostCards(categoryId: Int): Flow<List<PostCard>> = flow {
        try {
            // 1. 获取分类下的所有文章元数据
            val metadataResponse = apiInterface.getAllMetadatasByCategoryId(categoryId).awaitResponse()
            if (!metadataResponse.isSuccessful) {
                throw Exception("Failed to get category posts: ${metadataResponse.message()}")
            }

            val postMetadataList = metadataResponse.body() ?: emptyList()
            if (postMetadataList.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // 2. 并行获取所有相关数据
            val postCards = coroutineScope {
                postMetadataList.map { metadata ->
                    async {
                        try {
                            // 并行获取用户、分类、文章内容
                            val userDeferred = async {
                                val userResponse = apiInterface.getUser(metadata.userId).awaitResponse()
                                userResponse.body() ?: User(userId = metadata.userId, nickName = "未知用户")
                            }

                            val categoryDeferred = async {
                                val categoryResponse = apiInterface.getCategory(metadata.categoryId).awaitResponse()
                                categoryResponse.body() ?: Category(categoryId = metadata.categoryId, baseName = "未分类")
                            }

                            val bodyDeferred = async {
                                val bodyResponse = apiInterface.getPostBody(metadata.postId).awaitResponse()
                                bodyResponse.body() ?: PostBody(postId = metadata.postId, textContent = "内容加载失败")
                            }

                            // 等待所有数据获取完成
                            val user = userDeferred.await()
                            val category = categoryDeferred.await()
                            val body = bodyDeferred.await()

                            // 转换为PostCard
                            metadata.toPostCard(user, category, body)
                        } catch (e: Exception) {
                            // 单个文章失败不影响整体
                            PostCard(
                                postId = metadata.postId,
                                postDateTime = metadata.postDateTime,
                                isVisible = metadata.isVisible,
                                authorId = metadata.userId,
                                authorName = "加载失败",
                                authorAvatar = "",
                                categoryId = metadata.categoryId,
                                categoryName = "未知分类",
                                textContent = "内容加载失败: ${e.message}"
                            )
                        }
                    }
                }.awaitAll()
            }

            emit(postCards.filter { it.isVisible }) // 只返回可见的文章

        } catch (e: Exception) {
            throw Exception("Failed to get category post cards: ${e.message}")
        }
    }
    
    fun createPostMetadata(metadata: CreatPostMetadataDto): Flow<Int> = flow {
        val response = apiInterface.postPostMetadata(metadata).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.get("postId")?.let { postId ->
                emit(postId)
            } ?: throw Exception("No post ID returned")
        } else {
            throw Exception("Failed to create post metadata: ${response.message()}")
        }
    }
    
    fun createPostBody(body: PostBody): Flow<Boolean> = flow {
        val response = apiInterface.postPostBody(body).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Failed to create post body: ${response.message()}")
        }
    }
    
    fun deletePost(postId: Int): Flow<Boolean> = flow {
        val response = apiInterface.deletePost(postId).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Failed to delete post: ${response.message()}")
        }
    }
    
    fun searchPosts(keyword: String): Flow<List<PostMetadata>> = flow {
        val response = apiInterface.searchPostByKeyword(keyword).awaitResponse()
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
    
    fun getNoticePost(): Flow<PostMetadata> = flow {
        val response = apiInterface.getNotice().awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("No notice post found")
        } else {
            throw Exception("Failed to get notice post: ${response.message()}")
        }
    }
}