package com.qingshuige.tangyuan.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.model.Category
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.utils.UIUtils
import com.qingshuige.tangyuan.viewmodel.CreatePostViewModel

// 新增：用于管理 BottomSheet 状态的枚举
private enum class BottomSheetType { NONE, SECTION, CATEGORY }

// 新增：用于表示分区的简单数据类
private data class Section(val id: Int, val name: String)

private val sections = listOf(Section(1, "聊一聊"), Section(2, "侃一侃"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    sectionId: Int? = 1,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // 修改：使用枚举来管理活动的 BottomSheet
    var activeSheet by remember { mutableStateOf(BottomSheetType.NONE) }

    // 图片选择器 - 使用 PickVisualMedia 更可靠
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // 获取持久化权限
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // 某些 URI 可能不支持持久化权限，继续处理
            }

            viewModel.addImageAndUpload(context, it)
        }
    }

    // 监听发布成功
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            UIUtils.showSuccess("发布成功")
            onPostSuccess()
            viewModel.resetState()
        }
    }

    LaunchedEffect(sectionId) {
        sectionId?.let { viewModel.selectSection(it) }
    }

    // 显示错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            UIUtils.showError(it)
            viewModel.clearError()
        }
    }

    // 新增/修改：处理 BottomSheet 的显示逻辑
    when (activeSheet) {
        BottomSheetType.SECTION -> {
            SelectionBottomSheet(
                title = "选择分区",
                items = sections,
                selectedItem = sections.find { it.id == uiState.selectedSectionId },
                onItemSelected = { section ->
                    viewModel.selectSection(section.id)
                    activeSheet = BottomSheetType.NONE
                },
                onDismiss = { activeSheet = BottomSheetType.NONE }
            ) { section, isSelected ->
                SelectionListItem(
                    text = section.name,
                    isSelected = isSelected
                )
            }
        }

        BottomSheetType.CATEGORY -> {
            SelectionBottomSheet(
                title = "选择分类",
                items = uiState.categories,
                selectedItem = uiState.categories.find { it.categoryId == uiState.selectedCategoryId },
                isLoading = uiState.isLoadingCategories,
                onItemSelected = { category ->
                    viewModel.selectCategory(category.categoryId!!)
                    activeSheet = BottomSheetType.NONE
                },
                onDismiss = { activeSheet = BottomSheetType.NONE }
            ) { category, isSelected ->
                SelectionListItem(
                    text = category.baseName ?: "未知分类",
                    description = category.baseDescription,
                    isSelected = isSelected
                )
            }
        }

        BottomSheetType.NONE -> {
            // 不显示任何 BottomSheet
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "发布动态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.createPost() },
                        enabled = uiState.canPost,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("发布")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 内容输入
            ContentInput(
                content = uiState.content,
                onContentChange = { viewModel.updateContent(it) },
                charCount = uiState.contentCharCount,
                isValid = uiState.isContentValid
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 修改：使用新的选择器样式
                SelectionField(
                    label = "选择分区",
                    selectedValueText = sections.find { it.id == uiState.selectedSectionId }?.name
                        ?: "请选择分区",
                    onClick = { activeSheet = BottomSheetType.SECTION }
                )

                // 修改：使用新的选择器样式
                SelectionField(
                    label = "选择分类",
                    selectedValueText = uiState.categories.find { it.categoryId == uiState.selectedCategoryId }?.baseName
                        ?: "请选择分类",
                    isLoading = uiState.isLoadingCategories,
                    onClick = { activeSheet = BottomSheetType.CATEGORY }
                )
            }

            // 图片选择
            ImageSelector(
                selectedImages = uiState.selectedImageUris,
                uploadProgress = uiState.uploadProgress,
                remainingSlots = uiState.remainingImageSlots,
                onAddImage = {
                    imagePickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemoveImage = { viewModel.removeImageAt(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 新增：统一的选择器按钮样式
@Composable
private fun RowScope.SelectionField(
    label: String,
    selectedValueText: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isLoading) "加载中..." else selectedValueText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedValueText.startsWith("请选择"))
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开选择",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// 新增：通用的半屏选择器组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionBottomSheet(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    itemContent: @Composable (item: T, isSelected: Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有可选项", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                    items(items) { item ->
                        Box(modifier = Modifier.clickable { onItemSelected(item) }) {
                            itemContent(item, item == selectedItem)
                        }
                    }
                }
            }
        }
    }
}

// 新增：选择列表中的条目样式
@Composable
fun SelectionListItem(
    text: String,
    description: String? = null,
    isSelected: Boolean
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontFamily = LiteraryFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}


@Composable
private fun ContentInput(
    content: String,
    onContentChange: (String) -> Unit,
    charCount: Int,
    isValid: Boolean
) {
    Column {
//        Text(
//            text = "内容",
//            style = MaterialTheme.typography.titleSmall,
//            fontWeight = FontWeight.SemiBold,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (isValid)
                    MaterialTheme.colorScheme.outline
                else
                    MaterialTheme.colorScheme.error
            )
        ) {
            Column {
                TextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    placeholder = {
                        Text(
                            text = "分享你的想法...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 15
                )

                // 字数统计
                Text(
                    text = "$charCount / 2000",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (charCount > 2000)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ImageSelector(
    selectedImages: List<String>,
    uploadProgress: Map<String, Float>,
    remainingSlots: Int,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "图片 (最多3张)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${selectedImages.size} / 3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 显示已选择的图片
            selectedImages.forEachIndexed { index, imageUri ->
                ImagePreview(
                    imageUri = imageUri,
                    uploadProgress = uploadProgress[imageUri],
                    onRemove = { onRemoveImage(index) }
                )
            }

            // 添加图片按钮
            if (remainingSlots > 0) {
                AddImageButton(onClick = onAddImage)
            }
        }
    }
}

@Composable
private fun ImagePreview(
    imageUri: String,
    uploadProgress: Float?,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "选择的图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 上传进度
        if (uploadProgress != null && uploadProgress < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }

        // 删除按钮
        Surface(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddImageButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(100.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "添加图片",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "添加",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}