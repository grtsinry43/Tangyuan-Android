package com.qingshuige.tangyuan.analytics

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenPanel API Service
 * API documentation: https://openpanel.dev
 */
interface OpenPanelApiService {

    @POST("track")
    suspend fun trackEvent(
        @Body event: Any,
        @Header("openpanel-client-id") clientId: String,
        @Header("openpanel-client-secret") clientSecret: String,
        @Header("x-client-ip") clientIp: String? = null,
        @Header("user-agent") userAgent: String? = null
    ): Response<Unit>
}
