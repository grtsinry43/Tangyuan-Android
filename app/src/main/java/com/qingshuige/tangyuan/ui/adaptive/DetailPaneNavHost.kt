package com.qingshuige.tangyuan.ui.adaptive

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.qingshuige.tangyuan.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qingshuige.tangyuan.ui.screens.AboutScreen
import com.qingshuige.tangyuan.ui.screens.CategoryScreen
import com.qingshuige.tangyuan.ui.screens.DesignSystemScreen
import com.qingshuige.tangyuan.ui.screens.EditProfileScreen
import com.qingshuige.tangyuan.ui.screens.ImageDetailScreen
import com.qingshuige.tangyuan.ui.screens.PostDetailScreen
import com.qingshuige.tangyuan.ui.screens.PostManagementScreen
import com.qingshuige.tangyuan.ui.screens.ThemeSettingsScreen
import com.qingshuige.tangyuan.ui.screens.UserDetailScreen

private val DetailEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/**
 * 详情面板路由常量
 */
object DetailRoutes {
    const val EMPTY = "detail_empty"
    const val POST_DETAIL = "detail_post/{postId}"
    const val USER_DETAIL = "detail_user/{userId}"
    const val IMAGE_DETAIL = "detail_image/{postId}/{imageIndex}"
    const val CATEGORY_DETAIL = "detail_category/{categoryId}"
    const val EDIT_PROFILE = "detail_edit_profile"
    const val POST_MANAGEMENT = "detail_post_management"
    const val ABOUT = "detail_about"
    const val THEME_SETTINGS = "detail_theme_settings"
    const val DESIGN_SYSTEM = "detail_design_system"

    fun postDetail(postId: Int) = "detail_post/$postId"
    fun userDetail(userId: Int) = "detail_user/$userId"
    fun imageDetail(postId: Int, imageIndex: Int) = "detail_image/$postId/$imageIndex"
    fun categoryDetail(categoryId: Int) = "detail_category/$categoryId"
}

/**
 * 详情面板的独立导航宿主
 *
 * 在双栏模式下，右侧面板使用自己的 NavController，
 * 支持 PostDetail → UserDetail → PostDetail 等深度导航。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailPaneNavHost(
    detailRoute: String?,
    onNavigateFullScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val detailNavController = rememberNavController()

    // 响应外部导航请求（从列表面板触发）
    LaunchedEffect(detailRoute) {
        if (detailRoute != null) {
            detailNavController.navigate(detailRoute) {
                popUpTo(DetailRoutes.EMPTY) { inclusive = false }
            }
        } else {
            // 清空详情面板
            detailNavController.popBackStack(DetailRoutes.EMPTY, inclusive = false)
        }
    }

    NavHost(
        navController = detailNavController,
        startDestination = DetailRoutes.EMPTY,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = {
            fadeIn(animationSpec = tween(250, easing = DetailEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200, easing = DetailEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(200, easing = DetailEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250, easing = DetailEasing))
        }
    ) {
        // 空占位
        composable(DetailRoutes.EMPTY) {
            EmptyDetailPlaceholder()
        }

        // 帖子详情
        composable(
            route = DetailRoutes.POST_DETAIL,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            PostDetailScreen(
                postId = postId,
                onBackClick = {
                    if (!detailNavController.popBackStack()) {
                        // 已到栈底，无处可退
                    }
                },
                onAuthorClick = { authorId ->
                    detailNavController.navigate(DetailRoutes.userDetail(authorId))
                },
                onCategoryClick = { categoryId ->
                    detailNavController.navigate(DetailRoutes.categoryDetail(categoryId))
                },
                onImageClick = { pId, imageIndex ->
                    detailNavController.navigate(DetailRoutes.imageDetail(pId, imageIndex))
                },
                sharedTransitionScope = null,
                animatedContentScope = null
            )
        }

        // 用户详情
        composable(
            route = DetailRoutes.USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserDetailScreen(
                userId = userId,
                onBackClick = { detailNavController.popBackStack() },
                onPostClick = { postId ->
                    detailNavController.navigate(DetailRoutes.postDetail(postId))
                },
                onImageClick = { postId, imageIndex ->
                    detailNavController.navigate(DetailRoutes.imageDetail(postId, imageIndex))
                },
                onFollowClick = { /* TODO */ },
                onCategoryClick = { categoryId ->
                    detailNavController.navigate(DetailRoutes.categoryDetail(categoryId))
                },
                sharedTransitionScope = null,
                animatedContentScope = null
            )
        }

        // 图片详情
        composable(
            route = DetailRoutes.IMAGE_DETAIL,
            arguments = listOf(
                navArgument("postId") { type = NavType.IntType },
                navArgument("imageIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0
            ImageDetailScreen(
                postId = postId,
                initialImageIndex = imageIndex,
                onBackClick = { detailNavController.popBackStack() },
                onAuthorClick = { authorId ->
                    detailNavController.navigate(DetailRoutes.userDetail(authorId))
                },
                onCategoryClick = { categoryId ->
                    detailNavController.navigate(DetailRoutes.categoryDetail(categoryId))
                },
                onSwitchToTextMode = {
                    detailNavController.navigate(DetailRoutes.postDetail(postId)) {
                        popUpTo(DetailRoutes.imageDetail(postId, imageIndex)) {
                            inclusive = true
                        }
                    }
                },
                sharedTransitionScope = null,
                animatedContentScope = null
            )
        }

        // 分类详情
        composable(
            route = DetailRoutes.CATEGORY_DETAIL,
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            CategoryScreen(
                categoryId = categoryId,
                onBackClick = { detailNavController.popBackStack() },
                onPostClick = { postId ->
                    detailNavController.navigate(DetailRoutes.postDetail(postId))
                },
                onAuthorClick = { authorId ->
                    detailNavController.navigate(DetailRoutes.userDetail(authorId))
                },
                onImageClick = { postId, imageIndex ->
                    detailNavController.navigate(DetailRoutes.imageDetail(postId, imageIndex))
                },
                sharedTransitionScope = null,
                animatedContentScope = null
            )
        }

        // 编辑个人资料
        composable(DetailRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onBackClick = { detailNavController.popBackStack() },
                onSaveSuccess = { detailNavController.popBackStack() },
                sharedTransitionScope = null,
                animatedContentScope = null
            )
        }

        // 帖子管理
        composable(DetailRoutes.POST_MANAGEMENT) {
            PostManagementScreen(
                onBackClick = { detailNavController.popBackStack() },
                onPostClick = { postId ->
                    detailNavController.navigate(DetailRoutes.postDetail(postId))
                }
            )
        }

        // 关于
        composable(DetailRoutes.ABOUT) {
            AboutScreen(
                onBackClick = { detailNavController.popBackStack() }
            )
        }

        // 主题设置
        composable(DetailRoutes.THEME_SETTINGS) {
            ThemeSettingsScreen(
                onBackClick = { detailNavController.popBackStack() }
            )
        }

        // 设计系统
        composable(DetailRoutes.DESIGN_SYSTEM) {
            DesignSystemScreen(
                onBackClick = { detailNavController.popBackStack() }
            )
        }
    }
}

/**
 * 二级列表页面的自适应布局包装器
 *
 * 宽屏时左侧显示列表内容，右侧显示详情面板；
 * 窄屏时直接显示列表，导航回调由调用者控制。
 */
@Composable
fun AdaptiveSecondaryLayout(
    isDualPane: Boolean,
    listContent: @Composable (
        onPostClick: (Int) -> Unit,
        onAuthorClick: (Int) -> Unit,
        onImageClick: (Int, Int) -> Unit,
        onCategoryClick: (Int) -> Unit
    ) -> Unit,
    // 窄屏模式下的导航回调
    singlePanePostClick: (Int) -> Unit = {},
    singlePaneAuthorClick: (Int) -> Unit = {},
    singlePaneImageClick: (Int, Int) -> Unit = { _, _ -> },
    singlePaneCategoryClick: (Int) -> Unit = {}
) {
    if (isDualPane) {
        var detailRoute by rememberSaveable { mutableStateOf<String?>(null) }
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.38f)) {
                listContent(
                    { detailRoute = DetailRoutes.postDetail(it) },
                    { detailRoute = DetailRoutes.userDetail(it) },
                    { p, i -> detailRoute = DetailRoutes.imageDetail(p, i) },
                    { detailRoute = DetailRoutes.categoryDetail(it) }
                )
            }
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            Box(modifier = Modifier.weight(0.62f)) {
                DetailPaneNavHost(
                    detailRoute = detailRoute,
                    onNavigateFullScreen = { }
                )
            }
        }
    } else {
        listContent(
            singlePanePostClick,
            singlePaneAuthorClick,
            singlePaneImageClick,
            singlePaneCategoryClick
        )
    }
}

@Composable
private fun EmptyDetailPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "选择内容查看详情",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
