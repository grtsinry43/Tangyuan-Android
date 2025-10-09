package com.qingshuige.tangyuan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qingshuige.tangyuan.ui.theme.TangyuanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 使用回调方式处理深层链接
    private var deepLinkCallback: ((Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TangyuanTheme {
                App(
                    onDeepLinkCallbackSet = { callback ->
                        deepLinkCallback = callback
                        // 如果onCreate时已经有deep link，立即处理
                        handleDeepLink(intent)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * 处理Deep Link
     * 支持的格式:
     * - tangyuan://post/{postId}
     * - tangyuan://{postId}
     * - https://tangyuan.app/post/{postId}
     * - http://tangyuan.app/post/{postId}
     */
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data != null) {
            android.util.Log.d("MainActivity", "Deep Link URI: $data")
            android.util.Log.d("MainActivity", "Scheme: ${data.scheme}, Host: ${data.host}, Path: ${data.path}")

            // 解析路径
            val pathSegments = data.pathSegments
            android.util.Log.d("MainActivity", "Path segments: $pathSegments")

            var postId: Int? = null

            when {
                // tangyuan://post/123
                pathSegments.size >= 2 && pathSegments[0] == "post" -> {
                    postId = pathSegments[1].toIntOrNull()
                    android.util.Log.d("MainActivity", "Format: tangyuan://post/{id}, extracted: $postId")
                }
                // tangyuan://123 (直接跳过host，只有路径)
                pathSegments.size >= 1 -> {
                    postId = pathSegments[0].toIntOrNull()
                    android.util.Log.d("MainActivity", "Format: tangyuan://{id}, extracted: $postId")
                }
                // tangyuan://post (host是post，path是/123)
                data.host == "post" && data.path?.isNotEmpty() == true -> {
                    postId = data.path?.substring(1)?.toIntOrNull()
                    android.util.Log.d("MainActivity", "Format: tangyuan://post/{id} (as host), extracted: $postId")
                }
            }

            if (postId != null) {
                android.util.Log.d("MainActivity", "Calling deep link callback with postId: $postId")
                deepLinkCallback?.invoke(postId)
            } else {
                android.util.Log.w("MainActivity", "Could not extract postId from deep link")
            }
        } else {
            android.util.Log.d("MainActivity", "No deep link data in intent")
        }
    }
}