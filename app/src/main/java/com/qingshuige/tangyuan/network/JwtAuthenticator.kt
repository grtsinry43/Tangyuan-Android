package com.qingshuige.tangyuan.network

import com.google.gson.Gson
import com.qingshuige.tangyuan.model.LoginDto
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class JwtAuthenticator(private val tm: TokenManager, private val baseUrl: String) :
    Authenticator {
    
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    
    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        // 创建登录请求体
        val loginDto = LoginDto().apply {
            phoneNumber = tm.phoneNumber
            password = tm.password
        }
        
        val json = gson.toJson(loginDto)
        val requestBody = json.toRequestBody(mediaType)
        
        // 创建登录请求
        val loginRequest = Request.Builder()
            .url("${baseUrl}auth/login")
            .post(requestBody)
            .build()
        
        // 创建新的 OkHttpClient 用于登录请求（避免递归）
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        return try {
            // 执行登录请求
            val loginResponse = client.newCall(loginRequest).execute()
            
            if (loginResponse.isSuccessful) {
                val responseBody = loginResponse.body?.string()
                val tokenResponse = gson.fromJson(responseBody, Map::class.java)
                val newToken = tokenResponse?.values?.firstOrNull() as? String
                
                if (newToken != null) {
                    // 更新 token
                    tm.token = newToken
                    
                    // 重试原始请求，添加新的 access token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .header("X-Refresh-Attempt", "true")
                        .build()
                } else {
                    null // 无法获取新 token，放弃重试
                }
            } else {
                null // 登录失败，放弃重试
            }
        } catch (e: Exception) {
            null // 异常情况，放弃重试
        }
    }
}
