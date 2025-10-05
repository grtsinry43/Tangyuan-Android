package com.qingshuige.tangyuan.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    data class Loading<T>(val isLoading: Boolean = true) : Resource<T>()
}

fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return this
        .map<T, Resource<T>> { Resource.Success(it) }
        .catch { emit(Resource.Error(it.message ?: "Unknown error")) }
}

fun <T> Flow<T>.handleResource(
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String) -> Unit = {}
): Flow<Resource<T>> {
    return this.asResource()
        .map { resource ->
            when (resource) {
                is Resource.Loading -> {
                    onLoading()
                }
                is Resource.Success -> {
                    onSuccess(resource.data)
                }
                is Resource.Error -> {
                    onError(resource.message)
                }
            }
            resource
        }
}