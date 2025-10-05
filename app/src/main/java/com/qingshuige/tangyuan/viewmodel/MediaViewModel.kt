package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadResult(
    val success: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null
)

data class MediaUiState(
    val isUploading: Boolean = false,
    val uploadResult: UploadResult? = null,
    val uploadHistory: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {
    
    private val _mediaUiState = MutableStateFlow(MediaUiState())
    val mediaUiState: StateFlow<MediaUiState> = _mediaUiState.asStateFlow()
    
    fun uploadImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            _mediaUiState.value = _mediaUiState.value.copy(isUploading = true, error = null)
            mediaRepository.uploadImage(file)
                .catch { e ->
                    val uploadResult = UploadResult(
                        success = false,
                        error = e.message
                    )
                    _mediaUiState.value = _mediaUiState.value.copy(
                        isUploading = false,
                        uploadResult = uploadResult,
                        error = e.message
                    )
                }
                .collect { result ->
                    val uploadResult = UploadResult(
                        success = true,
                        imageUrl = result["url"] // Assuming API returns URL in response
                    )
                    
                    // Add to upload history
                    val updatedHistory = _mediaUiState.value.uploadHistory + listOf(uploadResult.imageUrl!!)
                    
                    _mediaUiState.value = _mediaUiState.value.copy(
                        isUploading = false,
                        uploadResult = uploadResult,
                        uploadHistory = updatedHistory
                    )
                }
        }
    }
    
    fun clearUploadResult() {
        _mediaUiState.value = _mediaUiState.value.copy(uploadResult = null)
    }
    
    fun clearError() {
        _mediaUiState.value = _mediaUiState.value.copy(error = null)
    }
    
    fun getUploadHistory(): List<String> {
        return _mediaUiState.value.uploadHistory
    }
    
    fun clearUploadHistory() {
        _mediaUiState.value = _mediaUiState.value.copy(uploadHistory = emptyList())
    }
    
    fun removeFromHistory(imageUrl: String) {
        val updatedHistory = _mediaUiState.value.uploadHistory.filter { it != imageUrl }
        _mediaUiState.value = _mediaUiState.value.copy(uploadHistory = updatedHistory)
    }
}