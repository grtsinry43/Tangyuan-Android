package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun uploadImage(file: MultipartBody.Part): Flow<Map<String, String>> = flow {
        val response = apiInterface.postImage(file).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to upload image: ${response.message()}")
        }
    }
}