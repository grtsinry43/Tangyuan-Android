package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.model.CreateUserDto
import com.qingshuige.tangyuan.model.LoginDto
import com.qingshuige.tangyuan.ui.components.AuroraBackground
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.utils.ValidationUtils
import com.qingshuige.tangyuan.viewmodel.UserViewModel

// 登录/注册模式枚举
enum class AuthMode {
    LOGIN, REGISTER
}

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    
    val loginState by userViewModel.loginState.collectAsState()
    val userUiState by userViewModel.userUiState.collectAsState()

    // 清除错误信息当切换模式时
    LaunchedEffect(authMode) {
        userViewModel.clearError()
    }

    // 登录成功后返回
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            navController.popBackStack()
        }
    }

    AuroraBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 品牌标题区域
                BrandHeader()

                // 认证卡片
                AuthCard(
                    authMode = authMode,
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    nickname = nickname,
                    onNicknameChange = { nickname = it },
                    onLogin = {
                        userViewModel.login(
                            LoginDto(
                                phoneNumber = phoneNumber,
                                password = password
                            )
                        )
                    },
                    onRegister = {
                        userViewModel.register(
                            CreateUserDto(
                                phoneNumber = phoneNumber,
                                password = password,
                                nickName = nickname,
                                avatarGuid = "8f416888-2ca4-4cda-8882-7f06a89630a2", // 默认头像
                                isoRegionName = "CN"
                            )
                        )
                    },
                    loginState = loginState,
                    userUiState = userUiState
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 切换模式按钮
                AuthModeSwitch(
                    authMode = authMode,
                    onModeChange = { authMode = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 底部装饰文案
                Text(
                    text = "欢迎来到糖原社区，看看你的嵴",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    fontFamily = LiteraryFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        // Logo
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "糖原社区Logo",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "糖原社区",
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            fontFamily = LiteraryFontFamily,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "假装这里有一句 slogan",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            ),
            fontFamily = LiteraryFontFamily,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AuthCard(
    authMode: AuthMode,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    loginState: com.qingshuige.tangyuan.viewmodel.LoginState,
    userUiState: com.qingshuige.tangyuan.viewmodel.UserUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = TangyuanShapes.CulturalCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 动态标题
            AnimatedContent(
                targetState = authMode,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState == AuthMode.REGISTER) it else -it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState == AuthMode.REGISTER) -it else it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                },
                label = "auth_title"
            ) { mode ->
                Text(
                    text = if (mode == AuthMode.LOGIN) "欢迎回来" else "加入社区",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    fontFamily = LiteraryFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // 手机号输入框
            val phoneError = if (phoneNumber.isNotEmpty()) ValidationUtils.getPhoneNumberError(phoneNumber) else null
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = {
                    Text(
                        "手机号",
                        fontFamily = LiteraryFontFamily
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                isError = phoneError != null || loginState.error != null || userUiState.error != null,
                singleLine = true,
                supportingText = phoneError?.let { 
                    { Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = LiteraryFontFamily
                    ) }
                }
            )

            // 密码输入框
            val passwordError = if (password.isNotEmpty()) ValidationUtils.getPasswordError(password) else null
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = {
                    Text(
                        "密码",
                        fontFamily = LiteraryFontFamily
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null || loginState.error != null || userUiState.error != null,
                singleLine = true,
                supportingText = passwordError?.let { 
                    { Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = LiteraryFontFamily
                    ) }
                }
            )

            // 注册专用字段
            AnimatedVisibility(
                visible = authMode == AuthMode.REGISTER,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeOut()
            ) {
                Column {
                    // 确认密码
                    val confirmPasswordError = if (confirmPassword.isNotEmpty()) 
                        ValidationUtils.getConfirmPasswordError(password, confirmPassword) else null
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = {
                            Text(
                                "确认密码",
                                fontFamily = LiteraryFontFamily
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        singleLine = true,
                        supportingText = confirmPasswordError?.let { 
                            { Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = LiteraryFontFamily
                            ) }
                        }
                    )

                    // 昵称
                    val nicknameError = if (nickname.isNotEmpty()) 
                        ValidationUtils.getNicknameError(nickname) else null
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        label = {
                            Text(
                                "昵称",
                                fontFamily = LiteraryFontFamily
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        isError = nicknameError != null || userUiState.error != null,
                        singleLine = true,
                        supportingText = nicknameError?.let { 
                            { Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = LiteraryFontFamily
                            ) }
                        }
                    )
                }
            }

            // 错误提示
            val errorMessage = loginState.error ?: userUiState.error
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = LiteraryFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 主要操作按钮
            val isLoading = loginState.isLoading || userUiState.isLoading
            
            val isFormValid = when (authMode) {
                AuthMode.LOGIN -> {
                    phoneNumber.isNotBlank() && password.isNotBlank() && 
                    ValidationUtils.getPhoneNumberError(phoneNumber) == null && 
                    ValidationUtils.getPasswordError(password) == null
                }
                AuthMode.REGISTER -> {
                    phoneNumber.isNotBlank() && password.isNotBlank() && 
                    confirmPassword.isNotBlank() && nickname.isNotBlank() && 
                    ValidationUtils.getPhoneNumberError(phoneNumber) == null && 
                    ValidationUtils.getPasswordError(password) == null && 
                    ValidationUtils.getConfirmPasswordError(password, confirmPassword) == null && 
                    ValidationUtils.getNicknameError(nickname) == null
                }
            }

            Button(
                onClick = {
                    if (authMode == AuthMode.LOGIN) {
                        onLogin()
                    } else {
                        onRegister()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading && isFormValid,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "登录中..." else "注册中...",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = LiteraryFontFamily
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = authMode,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (targetState == AuthMode.REGISTER) it else -it },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { if (targetState == AuthMode.REGISTER) -it else it },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        },
                        label = "button_text"
                    ) { mode ->
                        Text(
                            text = if (mode == AuthMode.LOGIN) "进入社区" else "加入社区",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            ),
                            fontFamily = LiteraryFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthModeSwitch(
    authMode: AuthMode,
    onModeChange: (AuthMode) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (authMode == AuthMode.LOGIN) "还没有账号？" else "已有账号？",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        TextButton(
            onClick = {
                onModeChange(
                    if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                )
            }
        ) {
            AnimatedContent(
                targetState = authMode,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState == AuthMode.REGISTER) it else -it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState == AuthMode.REGISTER) -it else it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                },
                label = "switch_text"
            ) { mode ->
                Text(
                    text = if (mode == AuthMode.LOGIN) "立即注册" else "去登录",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = LiteraryFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
