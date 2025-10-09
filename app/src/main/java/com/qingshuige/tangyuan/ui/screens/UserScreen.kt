package com.qingshuige.tangyuan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.ui.theme.EnglishFontFamily
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanShapes
import com.qingshuige.tangyuan.ui.theme.TangyuanTheme
import com.qingshuige.tangyuan.ui.theme.TangyuanTypography
import com.qingshuige.tangyuan.utils.UIUtils
import com.qingshuige.tangyuan.utils.withPanguSpacing
import com.qingshuige.tangyuan.viewmodel.UserViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserScreen(
    onEditProfile: () -> Unit = {},
    onPostManagement: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAbout: () -> Unit = {},
    onDesignSystem: () -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: androidx.compose.animation.AnimatedContentScope? = null
) {
    val loginState by userViewModel.loginState.collectAsState()
    val userUiState by userViewModel.userUiState.collectAsState()

    // 当前用户信息
    val currentUser = loginState.user ?: userUiState.currentUser
    val isLoggedIn = userViewModel.isLoggedIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isLoggedIn && currentUser != null) {
            // 用户信息卡片
            UserInfoCard(
                user = currentUser,
                onEditClick = onEditProfile,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 菜单选项
            MenuSection(
                onPostManagement = onPostManagement,
                onSettings = onSettings,
                onAbout = onAbout,
                onDesignSystem = onDesignSystem,
                onLogout = {
                    UIUtils.showConfirmDialog(
                        title = "退出登录",
                        message = "确定要退出登录吗？",
                        confirmText = "退出",
                        dismissText = "取消",
                        onConfirm = {
                            userViewModel.logout()
                            UIUtils.showSuccess("已退出登录")
                        }
                    )
                }
            )
        } else {
            // 未登录状态
            NotLoggedInContent()
            Spacer(modifier = Modifier.height(24.dp))
            MenuSectionNotLogin(
                onSettings = onSettings,
                onAbout = onAbout,
                onDesignSystem = onDesignSystem
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UserInfoCard(
    user: User,
    onEditClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: androidx.compose.animation.AnimatedContentScope? = null
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像和编辑按钮
            Box {
                AsyncImage(
                    model = "${TangyuanApplication.instance.bizDomain}images/${user.avatarGuid}.jpg",
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
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 昵称 - 一行
            Text(
                text = user.nickName.withPanguSpacing(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 用户ID - 一行
            Text(
                text = "ID: ${user.userId}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = EnglishFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 手机号 - 一行
            if (user.phoneNumber.isNotEmpty()) {
                Text(
                    text = "手机号: ${user.phoneNumber.replaceRange(3, 7, "****")}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = EnglishFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 地区和邮箱胶囊 - 一行
            AnimatedVisibility(
                visible = user.isoRegionName.isNotBlank() || user.email.isNotBlank(),
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(400, delayMillis = 100)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 地区信息胶囊
                    if (user.isoRegionName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "地区",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.isoRegionName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = TangyuanGeneralFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 邮箱信息胶囊
                    if (user.email.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "邮箱",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = TangyuanGeneralFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 用户签名
            if (user.bio.isNotBlank()) {
                Text(
                    text = user.bio.withPanguSpacing(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = LiteraryFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun UserInfoCardPreview() {
    TangyuanTheme {
        Surface {
            UserInfoCard(
                user = User(
                    userId = 123456,
                    nickName = "示例用户",
                    phoneNumber = "13800138000",
                    isoRegionName = "中国 北京",
                    email = "example@email.com",
                    bio = "这是一个示例用户的个人简介，展示了如何在用户信息卡片中显示多行文本。",
                    avatarGuid = "default_avatar"
                ),
                onEditClick = {}
            )
        }
    }
}

@Composable
private fun UserStats(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "帖子",
                value = "0" // TODO: 从用户数据中获取
            )

            VerticalDivider()

            StatItem(
                label = "关注",
                value = "0" // TODO: 从用户数据中获取
            )

            VerticalDivider()

            StatItem(
                label = "粉丝",
                value = "0" // TODO: 从用户数据中获取
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TangyuanTypography.numberMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Composable
private fun MenuSection(
    onPostManagement: () -> Unit,
    onDesignSystem: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    Text(
        text = "功能菜单",
        style = MaterialTheme.typography.titleMedium,
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                icon = Icons.Default.PostAdd,
                title = "帖子管理",
                subtitle = "管理我的帖子和草稿",
                onClick = onPostManagement
            )

//            HorizontalDivider(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//            )
//
//            MenuItem(
//                icon = Icons.Default.Settings,
//                title = "设置",
//                subtitle = "个性化设置和隐私选项",
//                onClick = onSettings
//            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MenuItem(
                icon = Icons.Default.DesignServices,
                title = "关于糖原设计系统",
                subtitle = "了解 App 的设计系统与排版规范",
                onClick = onDesignSystem,
                showDivider = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "版本信息和帮助",
                onClick = onAbout
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MenuItem(
                icon = Icons.Default.Logout,
                title = "退出登录",
                subtitle = "安全退出当前账号",
                onClick = onLogout,
                showDivider = false,
                isDanger = true
            )
        }
    }
}

@Composable
private fun MenuSectionNotLogin(
    onSettings: () -> Unit,
    onDesignSystem: () -> Unit,
    onAbout: () -> Unit
) {
    Text(
        text = "功能菜单",
        style = MaterialTheme.typography.titleMedium,
        fontFamily = TangyuanGeneralFontFamily,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
//            MenuItem(
//                icon = Icons.Default.Settings,
//                title = "设置",
//                subtitle = "个性化设置和隐私选项",
//                onClick = onSettings
//            )
//
//            HorizontalDivider(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//            )

            MenuItem(
                icon = Icons.Default.DesignServices,
                title = "关于糖原设计系统",
                subtitle = "了解 App 的设计系统与排版规范",
                onClick = onDesignSystem,
                showDivider = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "版本信息和帮助",
                onClick = onAbout,
                showDivider = false
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = TangyuanGeneralFontFamily,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = TangyuanGeneralFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun NotLoggedInContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "糖原社区",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "欢迎来到糖原社区",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = LiteraryFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "登录后查看个人信息",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = LiteraryFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}