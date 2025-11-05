package com.qingshuige.tangyuan.analytics

import com.google.gson.annotations.SerializedName

/**
 * OpenPanel event tracking models
 */

/**
 * Track event payload
 */
data class TrackEvent(
    @SerializedName("type")
    val type: String = "track",
    @SerializedName("payload")
    val payload: TrackPayload
)

data class TrackPayload(
    @SerializedName("name")
    val name: String,
    @SerializedName("properties")
    val properties: Map<String, Any>? = null
)

/**
 * Identify user payload
 */
data class IdentifyEvent(
    @SerializedName("type")
    val type: String = "identify",
    @SerializedName("payload")
    val payload: IdentifyPayload
)

data class IdentifyPayload(
    @SerializedName("profileId")
    val profileId: String,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("properties")
    val properties: Map<String, Any>? = null
)

/**
 * Increment property payload
 */
data class IncrementEvent(
    @SerializedName("type")
    val type: String = "increment",
    @SerializedName("payload")
    val payload: IncrementPayload
)

data class IncrementPayload(
    @SerializedName("profileId")
    val profileId: String,
    @SerializedName("property")
    val property: String,
    @SerializedName("value")
    val value: Int = 1
)

/**
 * Decrement property payload
 */
data class DecrementEvent(
    @SerializedName("type")
    val type: String = "decrement",
    @SerializedName("payload")
    val payload: DecrementPayload
)

data class DecrementPayload(
    @SerializedName("profileId")
    val profileId: String,
    @SerializedName("property")
    val property: String,
    @SerializedName("value")
    val value: Int = 1
)

/**
 * API error response
 */
data class OpenPanelErrorResponse(
    @SerializedName("error")
    val error: String,
    @SerializedName("status")
    val status: Int
)
