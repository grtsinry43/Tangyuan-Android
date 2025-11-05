package com.qingshuige.tangyuan.analytics

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.qingshuige.tangyuan.BuildConfig
import com.qingshuige.tangyuan.utils.DeviceIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * OpenPanel analytics client
 * Provides methods to track events, identify users, and manage user properties
 */
class OpenPanelClient private constructor(
    private val context: Context,
    private val clientId: String,
    private val clientSecret: String,
    private val baseUrl: String
) {

    private val apiService: OpenPanelApiService
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userAgent: String
    private var deviceId: String? = null

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        apiService = retrofit.create(OpenPanelApiService::class.java)

        // Generate user agent
        userAgent = "Tangyuan/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; " +
                "${Build.MANUFACTURER} ${Build.MODEL})"

        // Initialize device ID asynchronously
        scope.launch {
            deviceId = DeviceIdentifier.getDeviceId(context)
        }
    }

    /**
     * Get device ID (blocks until available)
     */
    private fun getDeviceIdSync(): String {
        return deviceId ?: DeviceIdentifier.getDeviceIdSync(context)
    }

    /**
     * Track a custom event
     * @param eventName Name of the event to track
     * @param properties Optional properties associated with the event
     * @param userId User ID if logged in (will use device ID if null)
     */
    fun track(eventName: String, properties: Map<String, Any>? = null, userId: String? = null) {
        scope.launch {
            try {
                // 合并设备元信息和自定义属性
                val deviceMetadata = DeviceIdentifier.getDeviceMetadata()
                val mergedProperties = mutableMapOf<String, Any>()
                mergedProperties.putAll(deviceMetadata)
                properties?.let { mergedProperties.putAll(it) }

                // 使用 userId（如果已登录）或设备ID（如果未登录）
                val profileId = userId ?: getDeviceIdSync()
                val isLoggedIn = userId != null

                mergedProperties["profile_id"] = profileId
                mergedProperties["is_logged_in"] = isLoggedIn

                val event = TrackEvent(
                    payload = TrackPayload(
                        name = eventName,
                        properties = mergedProperties
                    )
                )

                val response = apiService.trackEvent(
                    event = event,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    userAgent = userAgent,
                    clientIp = null // IP will be detected by server
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    if (BuildConfig.DEBUG) {
                        println("OpenPanel Track Error: ${response.code()} - ${response.message()}")
                        println("Error body: $errorBody")
                        println("Request: type=track, name=$eventName, properties=$mergedProperties")
                    }
                    handleError(response.code(), response.message())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    /**
     * Identify a user with profile information
     * @param profileId Unique identifier for the user
     * @param firstName User's first name (optional)
     * @param lastName User's last name (optional)
     * @param email User's email (optional)
     * @param properties Additional custom properties (optional)
     */
    fun identify(
        profileId: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        properties: Map<String, Any>? = null
    ) {
        scope.launch {
            try {
                val event = IdentifyEvent(
                    payload = IdentifyPayload(
                        profileId = profileId,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        properties = properties
                    )
                )

                val response = apiService.trackEvent(
                    event = event,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    userAgent = userAgent
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    if (BuildConfig.DEBUG) {
                        println("OpenPanel Identify Error: ${response.code()} - ${response.message()}")
                        println("Error body: $errorBody")
                        println("Request: type=identify, profileId=$profileId")
                    }
                    handleError(response.code(), response.message())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    /**
     * Increment a numeric property for a user
     * @param profileId User's profile ID
     * @param property Property name to increment
     * @param value Amount to increment (default: 1)
     */
    fun increment(profileId: String, property: String, value: Int = 1) {
        scope.launch {
            try {
                val event = IncrementEvent(
                    payload = IncrementPayload(
                        profileId = profileId,
                        property = property,
                        value = value
                    )
                )

                val response = apiService.trackEvent(
                    event = event,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    userAgent = userAgent
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    if (BuildConfig.DEBUG) {
                        println("OpenPanel Increment Error: ${response.code()} - ${response.message()}")
                        println("Error body: $errorBody")
                    }
                    handleError(response.code(), response.message())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    /**
     * Decrement a numeric property for a user
     * @param profileId User's profile ID
     * @param property Property name to decrement
     * @param value Amount to decrement (default: 1)
     */
    fun decrement(profileId: String, property: String, value: Int = 1) {
        scope.launch {
            try {
                val event = DecrementEvent(
                    payload = DecrementPayload(
                        profileId = profileId,
                        property = property,
                        value = value
                    )
                )

                val response = apiService.trackEvent(
                    event = event,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    userAgent = userAgent
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    if (BuildConfig.DEBUG) {
                        println("OpenPanel Decrement Error: ${response.code()} - ${response.message()}")
                        println("Error body: $errorBody")
                    }
                    handleError(response.code(), response.message())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleError(code: Int, message: String) {
        if (BuildConfig.DEBUG) {
            println("OpenPanel API Error: $code - $message")
        }
    }

    private fun handleException(e: Exception) {
        if (BuildConfig.DEBUG) {
            println("OpenPanel Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var instance: OpenPanelClient? = null

        /**
         * Initialize the OpenPanel client
         * Should be called once in Application.onCreate()
         */
        fun initialize(
            context: Context,
            clientId: String = BuildConfig.OPENPANEL_CLIENT_ID,
            clientSecret: String = BuildConfig.OPENPANEL_CLIENT_SECRET,
            baseUrl: String = BuildConfig.OPENPANEL_BASE_URL
        ) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = OpenPanelClient(
                            context.applicationContext,
                            clientId,
                            clientSecret,
                            baseUrl
                        )
                    }
                }
            }
        }

        /**
         * Get the singleton instance of OpenPanelClient
         * @throws IllegalStateException if initialize() has not been called
         */
        fun getInstance(): OpenPanelClient {
            return instance ?: throw IllegalStateException(
                "OpenPanelClient must be initialized before use. Call initialize() in Application.onCreate()"
            )
        }
    }
}
