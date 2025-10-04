package com.qingshuige.tangyuan.api

import com.google.gson.JsonObject
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.Comment
import com.qingshuige.tangyuan.model.CreatPostMetadataDto
import com.qingshuige.tangyuan.model.CreateCommentDto
import com.qingshuige.tangyuan.model.CreateUserDto
import com.qingshuige.tangyuan.model.LoginDto
import com.qingshuige.tangyuan.model.NewNotification
import com.qingshuige.tangyuan.model.Notification
import com.qingshuige.tangyuan.model.PostBody
import com.qingshuige.tangyuan.model.PostMetadata
import com.qingshuige.tangyuan.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

// 用于 Retrofit
interface ApiInterface {
    @GET("post/metadata/{id}")
    fun getPostMetadata(@Path("id") id: Int): Call<PostMetadata>

    @GET("post/body/{id}")
    fun getPostBody(@Path("id") id: Int): Call<PostBody>

    @GET("user/{id}")
    fun getUser(@Path("id") id: Int): Call<User>

    @GET("post/metadata/user/{id}")
    fun getMetadatasByUserID(@Path("id") userId: Int): Call<List<PostMetadata>>

    @Deprecated("Use phtPostMetadata instead")
    @GET("post/metadata/random/{count}")
    fun getRandomPostMetadata(@Path("count") count: Int): Call<List<PostMetadata>>

    @POST("philotaxis/postmetadata/{sectionId}")
    fun phtPostMetadata(
        @Path("sectionId") sectionId: Int,
        @Body exceptedIds: List<Int>
    ): Call<List<PostMetadata>>

    @POST("post/metadata")
    fun postPostMetadata(@Body metadata: CreatPostMetadataDto): Call<Map<String, Int>>

    @POST("post/body")
    fun postPostBody(@Body body: PostBody): Call<ResponseBody>

    @GET("post/metadata/notice")
    fun getNotice(): Call<PostMetadata>

    @POST("user")
    fun postUser(@Body user: CreateUserDto): Call<ResponseBody>

    @DELETE("post/{id}")
    fun deletePost(@Path("id") postId: Int): Call<ResponseBody>

    @PUT("user/{id}")
    fun putUser(@Path("id") id: Int, @Body userInfo: User): Call<ResponseBody>

    @GET("comment/post/{postId}")
    fun getCommentForPost(@Path("postId") postId: Int): Call<List<Comment>>

    @GET("comment/{id}")
    fun getComment(@Path("id") id: Int): Call<Comment>

    @GET("comment/sub/{parentCommentId}")
    fun getSubComment(@Path("parentCommentId") parentCommentId: Int): Call<List<Comment>>

    @DELETE("comment/{id}")
    fun deleteComment(@Path("id") commentId: Int): Call<ResponseBody>

    @Multipart
    @POST("image/uploadjpg")
    fun postImage(@Part file: MultipartBody.Part): Call<Map<String, String>>

    @POST("auth/login")
    fun login(@Body loginDto: LoginDto): Call<Map<String, String>>

    @Deprecated("Use getAllNotificationsByUserId instead")
    @GET("notification/user/{userId}")
    fun getAllUnreadNotificationsOf(@Path("userId") userId: Int): Call<List<Notification>>

    @Deprecated("Use markNewNotificationAsRead instead")
    @GET("notification/mark/{notificationId}")
    fun markNotificationAsRead(@Path("notificationId") notificationId: Int): Call<ResponseBody>

    @GET("newnotification/{userId}")
    fun getAllNotificationsByUserId(@Path("userId") userId: Int): Call<List<NewNotification>>

    @GET("newnotification/markasread/{id}")
    fun markNewNotificationAsRead(@Path("id") id: Int): Call<ResponseBody>

    @POST("comment")
    fun postComment(@Body dto: CreateCommentDto): Call<Map<String, String>>

    @GET("category/{id}")
    fun getCategory(@Path("id") id: Int): Call<Category>

    @GET("category/all")
    fun getAllCategories(): Call<List<Category>>

    @GET("category/count/{id}")
    fun getPostCountOfCategory(@Path("id") id: Int): Call<Int>

    @GET("category/weeklynewcount/{id}")
    fun getWeeklyNewPostCountOfCategory(@Path("id") id: Int): Call<Int>

    @GET("post/metadata/category/{categoryId}")
    fun getAllMetadatasByCategoryId(@Path("categoryId") categoryId: Int): Call<List<PostMetadata>>

    @GET("post/count/category/24h/{categoryId}")
    fun get24hNewPostCountByCategoryId(@Path("categoryId") categoryId: Int): Call<Int>

    @GET("post/count/category/7d/{categoryId}")
    fun get7dNewPostCountByCategoryId(@Path("categoryId") categoryId: Int): Call<Int>

    @GET("search/post/{keyword}")
    fun searchPostByKeyword(@Path("keyword") keyword: String): Call<List<PostMetadata>>

    @GET("search/user/{keyword}")
    fun searchUserByKeyword(@Path("keyword") keyword: String): Call<List<User>>

    @GET("search/comment/{keyword}")
    fun searchCommentByKeyword(@Path("keyword") keyword: String): Call<List<Comment>>

    /* 以下为非后端方法 */
    @GET
    fun getFromUrl(@Url url: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("https://api.pgyer.com/apiv2/app/check")
    fun checkUpdate(@FieldMap params: Map<String, String>): Call<JsonObject>
}