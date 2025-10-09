package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by editProfileViewModel.uiState.collectAsState()

    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    // 处理错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            kotlinx.coroutines.delay(3000)
            editProfileViewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "编辑个人资料",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = TangyuanGeneralFontFamily,
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
                        onClick = { editProfileViewModel.saveProfile() },
                        enabled = !uiState.isLoading && uiState.hasChanges,
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
                            Text("保存")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 头像编辑区域
                AvatarEditSection(
                    avatarUrl = uiState.currentUser?.let {
                        "${TangyuanApplication.instance.bizDomain}images/${it.avatarGuid}.jpg"
                    },
                    onAvatarClick = { /* TODO: 实现头像选择 */ },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )

                // 昵称输入
                ProfileInputField(
                    label = "昵称",
                    value = uiState.nickName,
                    onValueChange = editProfileViewModel::updateNickName,
                    icon = Icons.Default.Person,
                    placeholder = "请输入昵称"
                )

                // 邮箱输入
                ProfileInputField(
                    label = "邮箱",
                    value = uiState.email,
                    onValueChange = editProfileViewModel::updateEmail,
                    icon = Icons.Default.Email,
                    placeholder = "请输入邮箱地址",
                    keyboardType = KeyboardType.Email
                )

                // 地区输入
                ProfileInputField(
                    label = "地区",
                    value = uiState.region,
                    onValueChange = editProfileViewModel::updateRegion,
                    icon = Icons.Default.LocationOn,
                    placeholder = "请输入所在地区"
                )

                // 个人简介输入
                ProfileInputField(
                    label = "个人简介",
                    value = uiState.bio,
                    onValueChange = editProfileViewModel::updateBio,
                    icon = Icons.Default.Person,
                    placeholder = "介绍一下自己吧...",
                    minLines = 4,
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 错误提示
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AvatarEditSection(
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像显示区域
        Box {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .let { mod ->
                        if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                mod.sharedElement(
                                    rememberSharedContentState(key = "edit_profile_avatar"),
                                    animatedVisibilityScope = animatedContentScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                    }
                                )
                            }
                        } else mod
                    },
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher_foreground),
                fallback = painterResource(R.drawable.ic_launcher_foreground)
            )

            // 编辑按钮
            IconButton(
                onClick = onAvatarClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "更换头像",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击更换头像",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = TangyuanGeneralFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = TangyuanGeneralFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (minLines > 1) 12.dp else 0.dp),
                verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .then(if (minLines > 1) Modifier.padding(top = 12.dp) else Modifier)
                )

                Spacer(modifier = Modifier.size(12.dp))

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = TangyuanGeneralFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    minLines = minLines,
                    maxLines = maxLines,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = TangyuanGeneralFontFamily
                    )
                )
            }
        }
    }
}
