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
import com.qingshuige.tangyuan.ui.screens.UserDetailScreen

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

            // 帖子详情页
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType }
                )
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

            // 图片详情页
            composable(
                route = Screen.ImageDetail.route,
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

            // 用户详情页
            composable(
                route = Screen.UserDetail.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0

                UserDetailScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onFollowClick = {
                        // TODO: 实现关注功能
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
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
    val currentScreen =
        bottomBarScreens.find { it.route == currentDestination?.route } ?: Screen.Talk

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TangyuanTopBar(
                currentScreen = currentScreen,
                avatarUrl = "https://dogeoss.grtsinry43.com/img/author.jpeg",
                pageLevel = PageLevel.PRIMARY,
                onAvatarClick = onLoginClick,
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
