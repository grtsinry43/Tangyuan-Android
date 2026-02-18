package com.qingshuige.tangyuan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.update.UpdateInfo

@Composable
fun UpdatePromptDialog(
    info: UpdateInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val notes = info.releaseNotes.take(1200).ifBlank { "本次版本包含若干优化与问题修复。" }
    val headerColor = Color(0xFF273C75)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(124.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(headerColor)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "发现新版本",
                            fontFamily = LiteraryFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "你所喜爱的糖原，现在更好了",
                            fontFamily = LiteraryFontFamily,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "最新版本：v${info.versionName}",
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(96.dp)
                            .alpha(0.38f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("下面是 release 的更新内容：")
                    Text(
                        text = notes,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = "下载将优先 GitHub，失败会自动切换 ghfast / gh-proxy。",
                        modifier = Modifier.padding(top = 10.dp),
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("稍后再说")
                    }
                    Button(onClick = onConfirm) {
                        Text("立即更新")
                    }
                }
            }
        }
    }
}

@Composable
fun InstallPermissionDialog(
    onDismiss: () -> Unit,
    onGoAuthorize: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("安装权限提示") },
        text = { Text("为了安装新版本，请先授权“允许安装未知来源应用”。授权后会自动继续安装。") },
        confirmButton = {
            TextButton(onClick = onGoAuthorize) {
                Text("去授权")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ReadyInstallDialog(
    onDismiss: () -> Unit,
    onInstallNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("下载完成") },
        text = { Text("新版本已下载完成，是否现在安装？") },
        confirmButton = {
            TextButton(onClick = onInstallNow) {
                Text("立即安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后安装")
            }
        }
    )
}

@Composable
fun DownloadProgressDialog(
    progress: Float,
    progressText: String,
    onHideToBackground: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("正在下载更新") },
        text = {
            Column {
                Text(progressText)
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onHideToBackground) {
                Text("隐藏到后台")
            }
        },
        dismissButton = {}
    )
}
