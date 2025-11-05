package com.qingshuige.tangyuan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * 设备标识工具类
 * 用于生成和管理设备唯一标识，支持匿名用户追踪
 */
object DeviceIdentifier {

    private val Context.deviceIdDataStore: DataStore<Preferences> by preferencesDataStore(name = "device_id")
    private val DEVICE_ID_KEY = stringPreferencesKey("unique_device_id")

    /**
     * 获取或生成设备唯一标识
     * 优先使用保存的UUID，如果不存在则生成新的并保存
     */
    suspend fun getDeviceId(context: Context): String {
        // 先尝试从 DataStore 读取
        val savedId = context.deviceIdDataStore.data.map { preferences ->
            preferences[DEVICE_ID_KEY]
        }.first()

        return if (savedId != null) {
            savedId
        } else {
            // 生成新的设备ID并保存
            val newId = generateDeviceId(context)
            context.deviceIdDataStore.edit { preferences ->
                preferences[DEVICE_ID_KEY] = newId
            }
            newId
        }
    }

    /**
     * 同步获取设备ID（用于不方便使用协程的地方）
     * 注意：这会阻塞线程，应该在后台线程调用
     */
    fun getDeviceIdSync(context: Context): String = runBlocking {
        getDeviceId(context)
    }

    /**
     * 生成设备唯一标识
     * 优先使用 Android ID，如果获取失败则使用 UUID
     */
    @SuppressLint("HardwareIds")
    private fun generateDeviceId(context: Context): String {
        return try {
            // 尝试获取 Android ID
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            // 检查是否为有效的 Android ID（不是 null 且不是默认值）
            if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
                "android_$androidId"
            } else {
                // 如果 Android ID 无效，使用 UUID
                "uuid_${UUID.randomUUID()}"
            }
        } catch (e: Exception) {
            // 如果出错，生成随机 UUID
            "uuid_${UUID.randomUUID()}"
        }
    }

    /**
     * 获取设备元信息
     */
    fun getDeviceMetadata(): Map<String, String> {
        return mapOf(
            "device_brand" to Build.BRAND,
            "device_manufacturer" to Build.MANUFACTURER,
            "device_model" to Build.MODEL,
            "device_name" to Build.DEVICE,
            "android_version" to Build.VERSION.RELEASE,
            "android_sdk" to Build.VERSION.SDK_INT.toString(),
            "android_codename" to Build.VERSION.CODENAME
        )
    }

    /**
     * 获取完整的设备信息（用于调试）
     */
    fun getFullDeviceInfo(): String {
        return buildString {
            append("Brand: ${Build.BRAND}\n")
            append("Manufacturer: ${Build.MANUFACTURER}\n")
            append("Model: ${Build.MODEL}\n")
            append("Device: ${Build.DEVICE}\n")
            append("Android Version: ${Build.VERSION.RELEASE}\n")
            append("SDK: ${Build.VERSION.SDK_INT}\n")
            append("Codename: ${Build.VERSION.CODENAME}")
        }
    }
}
