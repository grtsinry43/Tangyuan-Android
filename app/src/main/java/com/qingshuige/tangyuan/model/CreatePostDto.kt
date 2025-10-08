package com.qingshuige.tangyuan.model

import java.util.Date

data class CreatePostDto(
    val textContent: String,
    val categoryId: Int,
    val sectionId: Int, // 0 或 1
    val isVisible: Boolean = true,
    val imageUUIDs: List<String> = emptyList()
) {
    
    fun toCreatPostMetadataDto(userId: Int): CreatPostMetadataDto {
        return CreatPostMetadataDto(
            isVisible = isVisible,
            postDateTime = Date(),
            sectionId = sectionId,
            categoryId = categoryId,
            userId = userId
        )
    }
    
    fun toPostBody(postId: Int): PostBody {
        return PostBody(
            postId = postId,
            textContent = textContent,
            image1UUID = imageUUIDs.getOrNull(0),
            image2UUID = imageUUIDs.getOrNull(1),
            image3UUID = imageUUIDs.getOrNull(2)
        )
    }
}

data class CreatePostState(
    val isLoading: Boolean = false,
    val content: String = "",
    val selectedCategoryId: Int? = null,
    val selectedSectionId: Int = 0, // 默认分区0
    val selectedImageUris: List<String> = emptyList(),
    val uploadedImageUUIDs: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Map<String, Float> = emptyMap(),
    val error: String? = null,
    val success: Boolean = false
) {
    
    val canPost: Boolean
        get() = content.isNotBlank() && 
                selectedCategoryId != null && 
                !isLoading && 
                !isUploading &&
                uploadedImageUUIDs.size == selectedImageUris.size
    
    val remainingImageSlots: Int
        get() = maxOf(0, 3 - selectedImageUris.size)
        
    val hasImages: Boolean
        get() = selectedImageUris.isNotEmpty()
        
    val isContentValid: Boolean
        get() = content.isNotBlank() && content.length <= 2000
        
    val contentCharCount: Int
        get() = content.length
}