package com.qingshuige.tangyuan.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatePostDto
import com.qingshuige.tangyuan.model.CreatePostState
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.CreatePostRepository
import com.qingshuige.tangyuan.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostRepository: CreatePostRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val tokenManager = TokenManager()

    private val _uiState = MutableStateFlow(CreatePostState())
    val uiState: StateFlow<CreatePostState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * 加载所有分类
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true, error = null)
            createPostRepository.getAllCategories()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingCategories = false,
                        error = "加载分类失败: ${e.message}"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingCategories = false,
                        categories = categories,
                        selectedCategoryId = categories.firstOrNull()?.categoryId
                    )
                }
        }
    }

    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    /**
     * 选择分类
     */
    fun selectCategory(categoryId: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    /**
     * 选择分区 (0: 聊一聊, 1: 侃一侃)
     */
    fun selectSection(sectionId: Int) {
        _uiState.value = _uiState.value.copy(selectedSectionId = sectionId)
    }

    /**
     * 添加图片 URI
     */
    fun addImageUri(uri: String) {
        Log.d("CreatePostViewModel", "Adding image URI: $uri")
        val currentImages = _uiState.value.selectedImageUris
        if (currentImages.size < 3) {
            _uiState.value = _uiState.value.copy(
                selectedImageUris = currentImages + uri
            )
        }
        Log.d("CreatePostViewModel", "Updated image URIs: ${_uiState.value.selectedImageUris}")
    }

    /**
     * 移除图片
     */
    fun removeImageAt(index: Int) {
        val currentImages = _uiState.value.selectedImageUris.toMutableList()
        val currentUUIDs = _uiState.value.uploadedImageUUIDs.toMutableList()

        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            if (index in currentUUIDs.indices) {
                currentUUIDs.removeAt(index)
            }
            _uiState.value = _uiState.value.copy(
                selectedImageUris = currentImages,
                uploadedImageUUIDs = currentUUIDs
            )
        }
    }

    fun addImageAndUpload(context: Context, uri: Uri) {
        val uriString = uri.toString()
        val currentImages = _uiState.value.selectedImageUris

        if (currentImages.size < 3) {
            // 1. 更新 URI 列表
            val updatedImages = currentImages + uriString
            _uiState.value = _uiState.value.copy(
                selectedImageUris = updatedImages
            )
            Log.d("CreatePostViewModel", "Updated image URIs: $updatedImages")

            // 2. 使用刚刚更新的列表来获取正确的索引
            val newIndex = updatedImages.size - 1

            // 3. 使用正确的索引来调用上传逻辑
            uploadImage(context, uri, newIndex)
        }
    }

    /**
     * 上传单张图片，不要直接调用这个方法，所有状态让ViewModel管理
     */
    private fun uploadImage(context: Context, uri: Uri, index: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isUploading = true,
                    uploadProgress = _uiState.value.uploadProgress + (uri.toString() to 0.5f)
                )

                // 将 Uri 转换为 File
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // 上传图片
                mediaRepository.uploadImage(body)
                    .catch { e ->
                        Log.e("CreatePostViewModel", "Image upload error: ${e}")
                        _uiState.value = _uiState.value.copy(
                            isUploading = false,
                            error = "图片上传失败: ${e.message}",
                            uploadProgress = _uiState.value.uploadProgress - uri.toString()
                        )
                        file.delete()
                    }
                    .collect { result ->
                        // API 返回的是 Map<String, String>，其中包含图片的 UUID
                        val imageUUID = result["guid"]

                        Log.d("CreatePostViewModel", "Image uploaded with UUID: $imageUUID")
                        Log.d(
                            "CreatePostViewModel",
                            "Current uploaded UUIDs: ${_uiState.value.uploadedImageUUIDs}"
                        )
                        Log.d(
                            "CreatePostViewModel",
                            "Current selected URIs: ${_uiState.value.selectedImageUris}"
                        )
                        Log.d("CreatePostViewModel", "Index: $index")

                        if (imageUUID != null) {
                            val currentUUIDs = _uiState.value.uploadedImageUUIDs.toMutableList()

                            // 确保列表长度足够
                            while (currentUUIDs.size <= index) {
                                currentUUIDs.add("")
                            }
                            currentUUIDs[index] = imageUUID

                            _uiState.value = _uiState.value.copy(
                                uploadedImageUUIDs = currentUUIDs,
                                uploadProgress = _uiState.value.uploadProgress + (uri.toString() to 1f)
                            )
                        }

                        // 检查是否所有图片都上传完成
                        val allUploaded = _uiState.value.selectedImageUris.size ==
                                _uiState.value.uploadedImageUUIDs.size
                        if (allUploaded) {
                            _uiState.value = _uiState.value.copy(isUploading = false)
                        }

                        file.delete()
                    }
            } catch (e: Exception) {
                Log.e("CreatePostViewModel", "Image upload error: ${e}")
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = "图片处理失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 上传所有图片
     */
    fun uploadAllImages(context: Context) {
        viewModelScope.launch {
            _uiState.value.selectedImageUris.forEachIndexed { index, uriString ->
                if (index >= _uiState.value.uploadedImageUUIDs.size ||
                    _uiState.value.uploadedImageUUIDs[index].isEmpty()
                ) {
                    uploadImage(context, Uri.parse(uriString), index)
                }
            }
        }
    }

    /**
     * 发布帖子
     */
    fun createPost() {
        viewModelScope.launch {
            val state = _uiState.value

            // 验证
            if (state.content.isBlank()) {
                _uiState.value = state.copy(error = "请输入内容")
                return@launch
            }

            if (state.selectedCategoryId == null) {
                _uiState.value = state.copy(error = "请选择分类")
                return@launch
            }

            // 检查图片是否都已上传
            if (state.selectedImageUris.isNotEmpty() &&
                state.selectedImageUris.size != state.uploadedImageUUIDs.size
            ) {
                _uiState.value = state.copy(error = "图片正在上传中，请稍候")
                return@launch
            }

            val userId = tokenManager.getUserIdFromToken()
            if (userId == null) {
                _uiState.value = state.copy(error = "请先登录")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            val createPostDto = CreatePostDto(
                textContent = state.content,
                categoryId = state.selectedCategoryId,
                sectionId = state.selectedSectionId,
                isVisible = true,
                imageUUIDs = state.uploadedImageUUIDs
            )

            createPostRepository.createPost(createPostDto, userId)
                .onSuccess { postId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "发布失败: ${e.message}"
                    )
                }
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = CreatePostState(categories = _uiState.value.categories)
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 将 Uri 转换为 File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("无法打开输入流")

            if (!file.exists() || file.length() == 0L) {
                throw IllegalStateException("文件创建失败或为空")
            }
        } catch (e: Exception) {
            Log.e("CreatePostViewModel", "Uri to File conversion failed", e)
            file.delete()
            throw e
        }

        return file
    }
}