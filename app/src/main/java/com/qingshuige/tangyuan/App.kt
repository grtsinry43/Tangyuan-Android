package com.qingshuige.tangyuan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.qingshuige.tangyuan.ui.screens.PostDetailScreen
import com.qingshuige.tangyuan.ui.screens.ImageDetailScreen
import com.qingshuige.tangyuan.ui.screens.TalkScreen
import com.qingshuige.tangyuan.ui.screens.LoginScreen

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
                        navController.navigate(Screen.PostDetail.createRoute(postId, "image") + "?imageIndex=$imageIndex")
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }

            composable(
                route = Screen.Login.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 600,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 600,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            ) {
                LoginScreen(navController = navController)
            }
            
            // 帖子详情页 - 统一容器管理两种模式
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType },
                    navArgument("mode") { type = NavType.StringType; defaultValue = "text" }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                val initialMode = backStackEntry.arguments?.getString("mode") ?: "text"
                
                PostDetailContainer(
                    postId = postId,
                    initialMode = initialMode,
                    onBackClick = { navController.popBackStack() },
                    onAuthorClick = { authorId ->
                        // TODO: 导航到用户详情页
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
        }
    }
}

/**
 * 帖子详情容器 - 统一管理文字和图片两种模式
 * 保持与PostCard的共享元素动画，同时支持内部模式切换动画
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailContainer(
    postId: Int,
    initialMode: String,
    onBackClick: () -> Unit,
    onAuthorClick: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    // 本地状态管理模式切换
    var currentMode by remember { mutableStateOf(initialMode) }
    var imageIndex by remember { mutableIntStateOf(0) }
    
    // 为模式切换创建内部AnimatedContent
    AnimatedContent(
        targetState = currentMode,
        transitionSpec = {
            // 使用滑动动画让切换更自然
            when {
                targetState == "image" && initialState == "text" -> {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(400)
                    ) togetherWith slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(400)
                    )
                }
                targetState == "text" && initialState == "image" -> {
                    slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(400)
                    ) togetherWith slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(400)
                    )
                }
                else -> {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                }
            }
        },
        label = "detail_mode_switch"
    ) { mode ->
        when (mode) {
            "image" -> {
                ImageDetailScreen(
                    postId = postId,
                    initialImageIndex = imageIndex,
                    onBackClick = onBackClick,
                    onAuthorClick = onAuthorClick,
                    onSwitchToTextMode = {
                        currentMode = "text"
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = this@AnimatedContent
                )
            }
            else -> {
                PostDetailScreen(
                    postId = postId,
                    onBackClick = onBackClick,
                    onAuthorClick = onAuthorClick,
                    onImageClick = { _, selectedImageIndex ->
                        imageIndex = selectedImageIndex
                        currentMode = "image"
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = if (mode == initialMode) {
                        // 如果是初始模式，使用外部的animatedContentScope来保持与PostCard的共享动画
                        animatedContentScope
                    } else {
                        // 如果是切换后的模式，使用内部的AnimatedContent scope
                        this@AnimatedContent
                    }
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
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(Screen.Talk, Screen.Topic, Screen.Message, Screen.User)
    val currentScreen = bottomBarScreens.find { it.route == currentDestination?.route } ?: Screen.Talk

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TangyuanTopBar(
                currentScreen = currentScreen,
                avatarUrl = "https://dogeoss.grtsinry43.com/img/author.jpeg",
                pageLevel = PageLevel.PRIMARY,
                onAvatarClick = onLoginClick,
                onAnnouncementClick = { /* 公告点击事件 */ },
                onPostClick = { /* 发表点击事件 */ }
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Talk.route) { 
                TalkScreen(
                    onPostClick = onPostClick,
                    onAuthorClick = { authorId ->
                        // TODO: 导航到用户详情页
                    },
                    onImageClick = onImageClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
            composable(Screen.Topic.route) { Text(text = "侃一侃") }
            composable(Screen.Message.route) { Text(text = "消息") }
            composable(Screen.User.route) { Text(text = "我的") }
        }
    }
}
