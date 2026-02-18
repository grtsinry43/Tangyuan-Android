package com.qingshuige.tangyuan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.qingshuige.tangyuan.ui.components.DownloadProgressDialog
import com.qingshuige.tangyuan.ui.components.InstallPermissionDialog
import com.qingshuige.tangyuan.ui.components.ReadyInstallDialog
import com.qingshuige.tangyuan.ui.components.UpdatePromptDialog
import com.qingshuige.tangyuan.ui.theme.AppThemeMode
import com.qingshuige.tangyuan.ui.theme.TangyuanTheme
import com.qingshuige.tangyuan.ui.theme.ThemePolicy
import com.qingshuige.tangyuan.update.UpdateCoordinator
import com.qingshuige.tangyuan.utils.PrefsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkCallback: ((Int) -> Unit)? = null
    private lateinit var updateCoordinator: UpdateCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateCoordinator = UpdateCoordinator(this)

        setContent {
            val themeModeValue by PrefsManager.getStringFlow(
                key = PrefsManager.Keys.APP_THEME_MODE,
                defaultValue = AppThemeMode.DEFAULT.value
            ).collectAsState(initial = AppThemeMode.DEFAULT.value)
            val themeUserOverridden by PrefsManager.getBooleanFlow(
                key = PrefsManager.Keys.APP_THEME_USER_OVERRIDDEN,
                defaultValue = false
            ).collectAsState(initial = false)

            val savedMode = AppThemeMode.fromValue(themeModeValue)
            val effectiveThemeMode = ThemePolicy.resolveThemeMode(
                savedMode = savedMode,
                userOverridden = themeUserOverridden
            )

            TangyuanTheme(themeMode = effectiveThemeMode) {
                App(
                    onDeepLinkCallbackSet = { callback ->
                        deepLinkCallback = callback
                        handleDeepLink(intent)
                    }
                )

                updateCoordinator.updateDialogInfo?.let { info ->
                    UpdatePromptDialog(
                        info = info,
                        onDismiss = { updateCoordinator.dismissUpdateDialog() },
                        onConfirm = { updateCoordinator.confirmUpdate(info) }
                    )
                }

                if (updateCoordinator.showInstallPermissionDialog) {
                    InstallPermissionDialog(
                        onDismiss = { updateCoordinator.dismissInstallPermissionDialog() },
                        onGoAuthorize = { updateCoordinator.goAuthorizeInstallPermission() }
                    )
                }

                if (updateCoordinator.showReadyInstallDialog && updateCoordinator.pendingInstallFile != null) {
                    ReadyInstallDialog(
                        onDismiss = { updateCoordinator.dismissReadyInstallDialog() },
                        onInstallNow = { updateCoordinator.installNowFromDialog() }
                    )
                }

                if (updateCoordinator.showDownloadProgressDialog) {
                    DownloadProgressDialog(
                        progress = updateCoordinator.downloadProgressValue,
                        progressText = updateCoordinator.downloadProgressText,
                        onHideToBackground = { updateCoordinator.hideDownloadToBackground() }
                    )
                }
            }
        }

        updateCoordinator.startCheckUpdates()
    }

    override fun onResume() {
        super.onResume()
        updateCoordinator.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateCoordinator.onDestroy()
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
            val pathSegments = data.pathSegments
            android.util.Log.d("MainActivity", "Path segments: $pathSegments")

            var postId: Int? = null
            when {
                pathSegments.size >= 2 && pathSegments[0] == "post" -> {
                    postId = pathSegments[1].toIntOrNull()
                    android.util.Log.d("MainActivity", "Format: tangyuan://post/{id}, extracted: $postId")
                }
                pathSegments.size >= 1 -> {
                    postId = pathSegments[0].toIntOrNull()
                    android.util.Log.d("MainActivity", "Format: tangyuan://{id}, extracted: $postId")
                }
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
