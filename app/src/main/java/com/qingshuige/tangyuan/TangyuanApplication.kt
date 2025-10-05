package com.qingshuige.tangyuan

import android.app.Application
import com.qingshuige.tangyuan.network.NetworkClient
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.utils.PrefsManager
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@HiltAndroidApp
class TangyuanApplication : Application() {
    val bizDomain = "https://ty.qingshuige.ink/"
    companion object {
        lateinit var instance: TangyuanApplication
            private set
    }

    // 全局实例
    lateinit var tokenManager: TokenManager
    lateinit var okHttpClient: OkHttpClient
    lateinit var retrofit: Retrofit

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 PrefsManager
        PrefsManager.init(this)

        // 初始化 TokenManager
        tokenManager = TokenManager(this)

        // 初始化网络客户端
        okHttpClient = NetworkClient.createOkHttpClient(this)
        retrofit = NetworkClient.createRetrofit(okHttpClient)
    }
}