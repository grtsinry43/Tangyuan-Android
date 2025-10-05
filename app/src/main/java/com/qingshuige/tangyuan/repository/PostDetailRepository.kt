package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.CommentCard
import com.qingshuige.tangyuan.model.CreateCommentDto
import com.qingshuige.tangyuan.model.PostCard
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.model.toCommentCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostDetailRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {
    
    /**
     * 单独获取帖子详情，不获取评论
     */
    fun getPostCard(postId: Int): Flow<PostCard> = flow {
        postRepository.getPostCard(postId).collect { postCard ->
            emit(postCard)
        }
    }
    
    /**
     * 获取帖子详情和评论的完整数据
     */
    fun getPostDetailWithComments(
        postId: Int,
        currentUserId: Int = 0
    ): Flow<Pair<PostCard, List<CommentCard>>> = flow {
        try {
            coroutineScope {
                // 并行获取帖子详情和评论列表
                val postDeferred = async {
                    postRepository.getPostCard(postId)
                }
                
                val commentsDeferred = async {
                    getCommentCardsForPost(postId, currentUserId)
                }
                
                // 等待两个数据都获取完成
                var postCard: PostCard? = null
                var commentCards: List<CommentCard> = emptyList()
                
                postDeferred.await().collect { post ->
                    postCard = post
                }
                
                commentsDeferred.await().collect { comments ->
                    commentCards = comments
                }
                
                postCard?.let { post ->
                    emit(Pair(post, commentCards))
                } ?: throw Exception("Failed to load post details")
            }
        } catch (e: Exception) {
            throw Exception("Failed to get post detail with comments: ${e.message}")
        }
    }
    
    /**
     * 获取帖子的所有评论卡片数据（包含作者信息）
     */
    fun getCommentCardsForPost(
        postId: Int,
        currentUserId: Int = 0
    ): Flow<List<CommentCard>> = flow {
        try {
            // 1. 获取主评论列表
            val commentsResponse = apiInterface.getCommentForPost(postId).awaitResponse()
            
            // 处理404情况（没有评论时API返回404）
            if (commentsResponse.code() == 404) {
                emit(emptyList())
                return@flow
            }
            
            if (!commentsResponse.isSuccessful) {
                throw Exception("Failed to get comments: ${commentsResponse.message()}")
            }
            
            val comments = commentsResponse.body() ?: emptyList()
            if (comments.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // 2. 过滤出主评论（parentCommentId == 0）
            val mainComments = comments.filter { it.parentCommentId == 0 }
            
            // 3. 并行获取所有主评论的作者信息和子评论
            val commentCards = coroutineScope {
                mainComments.map { comment ->
                    async {
                        try {
                            // 并行获取作者信息和子评论
                            val authorDeferred = async {
                                val userResponse = apiInterface.getUser(comment.userId).awaitResponse()
                                userResponse.body() ?: User(userId = comment.userId, nickName = "未知用户")
                            }
                            
                            val repliesDeferred = async {
                                getReplyCardsForComment(comment.commentId, currentUserId)
                            }
                            
                            val author = authorDeferred.await()
                            var replies: List<CommentCard> = emptyList()
                            
                            repliesDeferred.await().collect { replyCards ->
                                replies = replyCards
                            }
                            
                            comment.toCommentCard(
                                author = author,
                                replies = replies,
                                hasMoreReplies = false, // TODO: 实现分页时处理
                                currentUserId = currentUserId
                            )
                        } catch (e: Exception) {
                            // 单个评论失败不影响整体
                            comment.toCommentCard(
                                author = User(userId = comment.userId, nickName = "加载失败"),
                                currentUserId = currentUserId
                            )
                        }
                    }
                }.awaitAll()
            }
            
            emit(commentCards.sortedByDescending { it.commentDateTime })
            
        } catch (e: Exception) {
            throw Exception("Failed to get comment cards: ${e.message}")
        }
    }
    
    /**
     * 获取某个评论的回复列表
     */
    fun getReplyCardsForComment(
        parentCommentId: Int,
        currentUserId: Int = 0
    ): Flow<List<CommentCard>> = flow {
        try {
            val repliesResponse = apiInterface.getSubComment(parentCommentId).awaitResponse()
            
            // 处理404情况（没有回复时API返回404）
            if (repliesResponse.code() == 404) {
                emit(emptyList())
                return@flow
            }
            
            if (!repliesResponse.isSuccessful) {
                throw Exception("Failed to get replies: ${repliesResponse.message()}")
            }
            
            val replies = repliesResponse.body() ?: emptyList()
            if (replies.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // 并行获取所有回复的作者信息
            val replyCards = coroutineScope {
                replies.map { reply ->
                    async {
                        try {
                            val userResponse = apiInterface.getUser(reply.userId).awaitResponse()
                            val author = userResponse.body() ?: User(userId = reply.userId, nickName = "未知用户")
                            
                            reply.toCommentCard(
                                author = author,
                                currentUserId = currentUserId
                            )
                        } catch (e: Exception) {
                            reply.toCommentCard(
                                author = User(userId = reply.userId, nickName = "加载失败"),
                                currentUserId = currentUserId
                            )
                        }
                    }
                }.awaitAll()
            }
            
            emit(replyCards.sortedBy { it.commentDateTime })
            
        } catch (e: Exception) {
            throw Exception("Failed to get reply cards: ${e.message}")
        }
    }
    
    /**
     * 创建新评论
     */
    fun createComment(createCommentDto: CreateCommentDto): Flow<String> = flow {
        try {
            val response = apiInterface.postComment(createCommentDto).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.get("message")?.let { message ->
                    emit(message)
                } ?: emit("评论发布成功")
            } else {
                throw Exception("Failed to create comment: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create comment: ${e.message}")
        }
    }
    
    /**
     * 删除评论
     */
    fun deleteComment(commentId: Int): Flow<Boolean> = flow {
        try {
            val response = apiInterface.deleteComment(commentId).awaitResponse()
            if (response.isSuccessful) {
                emit(true)
            } else {
                throw Exception("Failed to delete comment: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to delete comment: ${e.message}")
        }
    }
    
    /**
     * 刷新帖子详情页数据
     */
    fun refreshPostDetail(
        postId: Int,
        currentUserId: Int = 0
    ): Flow<Pair<PostCard, List<CommentCard>>> = flow {
        // 直接调用getPostDetailWithComments，因为数据都是实时获取的
        getPostDetailWithComments(postId, currentUserId).collect { result ->
            emit(result)
        }
    }
}