package com.qingshuige.tangyuan

import android.app.Application
import com.qingshuige.tangyuan.analytics.OpenPanelClient
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

        // 初始化 OpenPanel Analytics
        try {
            OpenPanelClient.initialize(this)
            // 追踪应用启动事件（不传userId，使用设备ID）
            OpenPanelClient.getInstance().track("app_launched", mapOf(
                "version" to BuildConfig.VERSION_NAME,
                "build" to BuildConfig.VERSION_CODE
            ))
        } catch (e: Exception) {
            // 如果 BuildConfig 中没有配置凭证，不影响应用运行
            if (BuildConfig.DEBUG) {
                println("OpenPanel initialization failed: ${e.message}")
            }
        }
    }
}