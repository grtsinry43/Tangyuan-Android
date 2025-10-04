package com.qingshuige.tangyuan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.qingshuige.tangyuan.navigation.Screen
import com.qingshuige.tangyuan.ui.components.PageLevel
import com.qingshuige.tangyuan.ui.components.TangyuanBottomAppBar
import com.qingshuige.tangyuan.ui.components.TangyuanTopBar

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Talk) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TangyuanTopBar(
                currentScreen = currentScreen,
                avatarUrl = "https://dogeoss.grtsinry43.com/img/author.jpeg",
                pageLevel = PageLevel.PRIMARY,
                onAvatarClick = {/* 头像点击事件 */ },
                onAnnouncementClick = {/* 公告点击事件 */ },
                onPostClick = {/* 发表点击事件 */ }
            )
        },
        bottomBar = {
            TangyuanBottomAppBar(currentScreen) { selectedScreen ->
                currentScreen = selectedScreen
            }
        }
    ) { innerPadding ->
        Text(
            text = "Android",
            modifier = Modifier.padding(innerPadding)
        )
    }
}