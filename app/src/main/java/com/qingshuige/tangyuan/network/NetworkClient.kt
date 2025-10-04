package com.qingshuige.tangyuan.network

import android.content.Context
import com.google.gson.Gson
import com.qingshuige.tangyuan.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val BASE_URL = "https://ty.qingshuige.ink/api/"

    fun createOkHttpClient(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(JwtInterceptor(tokenManager))
            .authenticator(JwtAuthenticator(tokenManager, BASE_URL))
            .build()
    }

    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // 添加标准Gson转换为后备
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}