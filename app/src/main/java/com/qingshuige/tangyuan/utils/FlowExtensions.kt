package com.qingshuige.tangyuan.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

fun <T> ViewModel.collectFlow(
    flow: Flow<T>,
    uiState: MutableStateFlow<UiState<T>>,
    onLoading: () -> Unit = { uiState.value = uiState.value.copy(isLoading = true, error = null) },
    onSuccess: (T) -> Unit = { data -> uiState.value = UiState(data = data) },
    onError: (String) -> Unit = { error -> uiState.value = UiState(error = error) }
) {
    viewModelScope.launch {
        onLoading()
        flow.catch { e ->
            onError(e.message ?: "Unknown error occurred")
        }.collect { data ->
            onSuccess(data)
        }
    }
}

fun <T> ViewModel.collectFlowList(
    flow: Flow<List<T>>,
    uiState: MutableStateFlow<UiState<List<T>>>,
    onLoading: () -> Unit = { uiState.value = uiState.value.copy(isLoading = true, error = null) },
    onSuccess: (List<T>) -> Unit = { data -> uiState.value = UiState(data = data) },
    onError: (String) -> Unit = { error -> uiState.value = UiState(error = error) }
) {
    viewModelScope.launch {
        onLoading()
        flow.catch { e ->
            onError(e.message ?: "Unknown error occurred")
        }.collect { data ->
            onSuccess(data)
        }
    }
}