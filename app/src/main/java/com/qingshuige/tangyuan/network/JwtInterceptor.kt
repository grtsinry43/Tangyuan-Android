package com.qingshuige.tangyuan.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class JwtInterceptor(private val tm: TokenManager) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tm.token
        if (token != null) {
            val modifiedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(modifiedRequest)
        }
        return chain.proceed(originalRequest)
    }
}
