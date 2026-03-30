package com.qingshuige.tangyuan.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.qingshuige.tangyuan.analytics.OpenPanelClient
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.model.CreatePostDto
import com.qingshuige.tangyuan.model.CreatePostDraft
import com.qingshuige.tangyuan.model.CreatePostState
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.CreatePostRepository
import com.qingshuige.tangyuan.repository.MediaRepository
import com.qingshuige.tangyuan.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val mediaRepository: MediaRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostState())
    val uiState: StateFlow<CreatePostState> = _uiState.asStateFlow()
    private val gson = Gson()
    private var draftSaveJob: Job? = null
    private var draftInitialized = false

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
                    // 追踪失败
                    try {
                        val userId = tokenManager.getUserIdFromToken()?.toString()
                        OpenPanelClient.getInstance().track("new_post_get_category_fail", mapOf(
                            "error" to (e.message ?: "unknown")
                        ), userId = userId)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
                }
                .collect { categories ->
                    val currentCategoryId = _uiState.value.selectedCategoryId
                    _uiState.value = _uiState.value.copy(
                        isLoadingCategories = false,
                        categories = categories,
                        selectedCategoryId = categories.firstOrNull { it.categoryId == currentCategoryId }?.categoryId
                            ?: currentCategoryId
                            ?: categories.firstOrNull()?.categoryId
                    )
                }
        }
    }

    fun initializeDraft(initialSectionId: Int? = null) {
        if (draftInitialized) return
        draftInitialized = true
        viewModelScope.launch {
            restoreDraft(initialSectionId ?: _uiState.value.selectedSectionId)
        }
    }

    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
        scheduleDraftSave()
    }

    /**
     * 选择分类
     */
    fun selectCategory(categoryId: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        scheduleDraftSave()
    }

    /**
     * 选择分区 (0: 聊一聊, 1: 侃一侃)
     */
    fun selectSection(sectionId: Int) {
        if (sectionId == _uiState.value.selectedSectionId) return
        viewModelScope.launch {
            persistDraftNow()
            restoreDraft(sectionId)
        }
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
            scheduleDraftSave()
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
            scheduleDraftSave()
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
            scheduleDraftSave()
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
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
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
                        // 追踪失败
                        try {
                            val userId = tokenManager.getUserIdFromToken()?.toString()
                            OpenPanelClient.getInstance().track("upload_img_fail", mapOf(
                                "error" to (e.message ?: "unknown")
                            ), userId = userId)
                        } catch (trackingError: Exception) {
                            // OpenPanel 追踪失败不影响主要功能
                        }
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
                            scheduleDraftSave()
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
                    viewModelScope.launch {
                        clearCurrentDraft()
                    }

                    // 追踪发帖成功
                    try {
                        OpenPanelClient.getInstance().track("post_created", mapOf(
                            "content_length" to state.content.length,
                            "has_images" to state.selectedImageUris.isNotEmpty(),
                            "image_count" to state.selectedImageUris.size,
                            "category_id" to (state.selectedCategoryId ?: -1),
                            "section_id" to state.selectedSectionId,
                            "success" to true
                        ), userId = userId.toString())

                        // 为用户增加发帖计数
                        OpenPanelClient.getInstance().increment(userId.toString(), "total_posts", 1)
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "发布失败: ${e.message}"
                    )

                    // 追踪发帖失败
                    try {
                        OpenPanelClient.getInstance().track("post_created", mapOf(
                            "content_length" to state.content.length,
                            "has_images" to state.selectedImageUris.isNotEmpty(),
                            "image_count" to state.selectedImageUris.size,
                            "success" to false,
                            "error" to (e.message ?: "unknown")
                        ), userId = userId.toString())
                    } catch (trackingError: Exception) {
                        // OpenPanel 追踪失败不影响主要功能
                    }
                }
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        draftSaveJob?.cancel()
        _uiState.value = CreatePostState(categories = _uiState.value.categories)
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearDraftStatus() {
        _uiState.value = _uiState.value.copy(draftStatus = null)
    }

    fun clearDraft() {
        viewModelScope.launch {
            clearCurrentDraft()
            val categories = _uiState.value.categories
            _uiState.value = CreatePostState(
                categories = categories,
                selectedSectionId = _uiState.value.selectedSectionId,
                selectedCategoryId = categories.firstOrNull()?.categoryId,
                draftStatus = "草稿已清空"
            )
        }
    }

    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(500)
            persistDraftNow()
        }
    }

    private suspend fun restoreDraft(sectionId: Int) {
        val categories = _uiState.value.categories
        val draft = loadDraft(sectionId)
        _uiState.value = if (draft != null) {
            _uiState.value.copy(
                content = draft.content,
                selectedCategoryId = draft.selectedCategoryId ?: categories.firstOrNull()?.categoryId,
                selectedSectionId = draft.selectedSectionId,
                selectedImageUris = draft.selectedImageUris,
                uploadedImageUUIDs = draft.uploadedImageUUIDs,
                uploadProgress = emptyMap(),
                isUploading = false,
                draftStatus = "已恢复草稿"
            )
        } else {
            _uiState.value.copy(
                content = "",
                selectedCategoryId = categories.firstOrNull()?.categoryId,
                selectedSectionId = sectionId,
                selectedImageUris = emptyList(),
                uploadedImageUUIDs = emptyList(),
                uploadProgress = emptyMap(),
                isUploading = false,
                draftStatus = null
            )
        }
    }

    private suspend fun loadDraft(sectionId: Int): CreatePostDraft? {
        val raw = PrefsManager.getString(draftKey(sectionId))
        if (raw.isBlank()) return null
        return runCatching {
            gson.fromJson(raw, CreatePostDraft::class.java)
        }.getOrNull()
    }

    private suspend fun persistDraftNow() {
        val state = _uiState.value
        val key = draftKey(state.selectedSectionId)
        if (!state.hasDraftContent) {
            PrefsManager.remove(key)
            return
        }

        val draft = CreatePostDraft(
            content = state.content,
            selectedCategoryId = state.selectedCategoryId,
            selectedSectionId = state.selectedSectionId,
            selectedImageUris = state.selectedImageUris,
            uploadedImageUUIDs = state.uploadedImageUUIDs
        )
        PrefsManager.putString(key, gson.toJson(draft))
    }

    private suspend fun clearCurrentDraft() {
        draftSaveJob?.cancel()
        PrefsManager.remove(draftKey(_uiState.value.selectedSectionId))
    }

    private fun draftKey(sectionId: Int): String {
        val userId = tokenManager.getUserIdFromToken() ?: 0
        return "${PrefsManager.Keys.CREATE_POST_DRAFT_PREFIX}_${userId}_$sectionId"
    }

    /**
     * 将 Uri 转换为 File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")

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
