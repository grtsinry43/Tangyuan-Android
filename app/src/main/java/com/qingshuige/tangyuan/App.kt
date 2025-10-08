package com.qingshuige.tangyuan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qingshuige.tangyuan.navigation.Screen
import com.qingshuige.tangyuan.ui.components.PageLevel
import com.qingshuige.tangyuan.ui.components.TangyuanBottomAppBar
import com.qingshuige.tangyuan.ui.components.TangyuanTopBar
import com.qingshuige.tangyuan.ui.screens.AboutScreen
import com.qingshuige.tangyuan.ui.screens.DesignSystemScreen
import com.qingshuige.tangyuan.ui.screens.PostDetailScreen
import com.qingshuige.tangyuan.ui.screens.ImageDetailScreen
import com.qingshuige.tangyuan.ui.screens.TalkScreen
import com.qingshuige.tangyuan.ui.screens.LoginScreen
import com.qingshuige.tangyuan.ui.screens.UserDetailScreen
import com.qingshuige.tangyuan.ui.screens.UserScreen
import com.qingshuige.tangyuan.viewmodel.UserViewModel

// 自定义带回弹效果的easing - 快速流畅
private val QuickSpringEasing = CubicBezierEasing(0.34f, 1.3f, 0.64f, 1.0f)
private val QuickEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainFlow(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onImageClick = { postId, imageIndex ->
                        navController.navigate(Screen.ImageDetail.createRoute(postId, imageIndex))
                    },
                    onAuthorClick = { authorId ->
                        navController.navigate(Screen.UserDetail.createRoute(authorId))
                    },
                    onAboutClick = { navController.navigate(Screen.About.route) },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable,
                    onDesignSystemClick = { navController.navigate(Screen.DesignSystem.route) }
                )
            }

            composable(
                route = Screen.Login.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = QuickSpringEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = QuickEasing
                        )
                    )
                }
            ) {
                LoginScreen(navController = navController)
            }

            // 帖子详情页 - 使用淡入淡出避免与共享元素冲突
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType }
                ),
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popEnterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                }
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0

                PostDetailScreen(
                    postId = postId,
                    onBackClick = { navController.popBackStack() },
                    onAuthorClick = { authorId ->
                        navController.navigate(Screen.UserDetail.createRoute(authorId))
                    },
                    onImageClick = { postId, imageIndex ->
                        navController.navigate(Screen.ImageDetail.createRoute(postId, imageIndex)) {
                            popUpTo(Screen.PostDetail.createRoute(postId)) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }

            // 图片详情页 - 使用淡入淡出避免与共享元素冲突
            composable(
                route = Screen.ImageDetail.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType },
                    navArgument("imageIndex") { type = NavType.IntType }
                ),
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popEnterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                }
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0

                ImageDetailScreen(
                    postId = postId,
                    initialImageIndex = imageIndex,
                    onBackClick = { navController.popBackStack() },
                    onAuthorClick = { authorId ->
                        navController.navigate(Screen.UserDetail.createRoute(authorId))
                    },
                    onSwitchToTextMode = {
                        navController.navigate(Screen.PostDetail.createRoute(postId)) {
                            popUpTo(Screen.ImageDetail.createRoute(postId, imageIndex)) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }

            // 用户详情页 - 使用淡入淡出避免与共享元素冲突
            composable(
                route = Screen.UserDetail.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType }
                ),
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popEnterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                }
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0

                UserDetailScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onImageClick = { postId, imageIndex ->
                        navController.navigate(Screen.ImageDetail.createRoute(postId, imageIndex)) {
                            popUpTo(Screen.PostDetail.createRoute(postId)) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onFollowClick = {
                        // TODO: 实现关注功能
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }

            composable(
                route = Screen.About.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickSpringEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = QuickEasing
                        )
                    )
                }
            ) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DesignSystem.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickSpringEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = QuickEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = QuickEasing
                        )
                    )
                }
            ) {
                DesignSystemScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainFlow(
    onLoginClick: () -> Unit,
    onPostClick: (Int) -> Unit,
    onImageClick: (Int, Int) -> Unit = { _, _ -> },
    onAuthorClick: (Int) -> Unit = {},
    onAboutClick: () -> Unit,
    onDesignSystemClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(Screen.Talk, Screen.Topic, Screen.Message, Screen.User)
    val currentScreen =
        bottomBarScreens.find { it.route == currentDestination?.route } ?: Screen.Talk

    // 观察登录状态和用户信息
    val loginState by userViewModel.loginState.collectAsState()
    val userUiState by userViewModel.userUiState.collectAsState()

    // 获取头像URL - 当用户状态变化时重新计算
    val avatarUrl = remember(loginState.user, userUiState.currentUser) {
        userViewModel.getCurrentUserAvatarUrl()
    }

    // 头像点击处理逻辑
    val onAvatarClick = {
        if (userViewModel.isLoggedIn()) {
            // 已登录，跳转到"我的"页面
            mainNavController.navigate(Screen.User.route) {
                popUpTo(mainNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            // 未登录，跳转到登录页面
            onLoginClick()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TangyuanTopBar(
                currentScreen = currentScreen,
                avatarUrl = avatarUrl,
                pageLevel = PageLevel.PRIMARY,
                onAvatarClick = onAvatarClick,
                onAnnouncementClick = {/* 公告点击事件 */ },
                onPostClick = {/* 发表点击事件 */ }
            )
        },
        bottomBar = {
            TangyuanBottomAppBar(currentScreen) { selectedScreen ->
                mainNavController.navigate(selectedScreen.route) {
                    popUpTo(mainNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Talk.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = QuickEasing
                    )
                )
            }
        ) {
            composable(Screen.Talk.route) {
                TalkScreen(
                    onPostClick = onPostClick,
                    onAuthorClick = onAuthorClick,
                    onImageClick = onImageClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
            composable(Screen.Topic.route) { Text(text = "侃一侃") }
            composable(Screen.Message.route) { Text(text = "消息") }
            composable(Screen.User.route) {
                UserScreen(
                    onEditProfile = {
                        // TODO: 导航到编辑个人资料页面
                    },
                    onPostManagement = {
                        // TODO: 导航到帖子管理页面
                    },
                    onSettings = {
                        // TODO: 导航到设置页面
                    },
                    onAbout = onAboutClick,
                    onDesignSystem = onDesignSystemClick,
                )
            }
        }
    }
}
