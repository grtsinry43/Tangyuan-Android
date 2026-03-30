package com.qingshuige.tangyuan

import android.app.Application
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.utils.PrefsManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TangyuanApplication : Application() {
    companion object {
        const val BIZ_DOMAIN = "https://ty.qingshuige.ink/"

        lateinit var instance: TangyuanApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 PrefsManager
        PrefsManager.init(this)

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