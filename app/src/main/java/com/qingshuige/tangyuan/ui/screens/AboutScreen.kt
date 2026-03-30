package com.qingshuige.tangyuan.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingshuige.tangyuan.BuildConfig
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.ui.components.AnimatedSecondaryTopBar
import com.qingshuige.tangyuan.ui.components.AuroraBackground
import com.qingshuige.tangyuan.ui.components.GlobalMessageHost
import com.qingshuige.tangyuan.ui.theme.LiteraryFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanGeneralFontFamily
import com.qingshuige.tangyuan.ui.theme.TangyuanTheme
import com.qingshuige.tangyuan.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {},
    messageViewModel: MessageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentMessage by messageViewModel.currentMessage.collectAsState()

    // 彩蛋：记录点击次数和时间
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var shouldOpenGithub by remember { mutableIntStateOf(0) }

    // Logo点击处理
    val onLogoClick = {
        val currentTime = System.currentTimeMillis()
        // 如果两次点击间隔超过500ms，重置计数
        if (currentTime - lastClickTime > 500) {
            clickCount = 1
        } else {
            clickCount++
        }
        lastClickTime = currentTime

        // 连续点击5次触发彩蛋
        if (clickCount >= 5) {
            clickCount = 0
            // 显示全局消息
            messageViewModel.showSuccess("看看你的嵴👀")
            // 触发延迟跳转
            shouldOpenGithub++
        }
    }

    // 延迟跳转到GitHub
    LaunchedEffect(shouldOpenGithub) {
        if (shouldOpenGithub > 0) {
            delay(1500) // 延迟1.5秒，让用户看到提示
            try {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/MitochondriaCN".toUri())
                context.startActivity(intent)
            } catch (_: Exception) {
                messageViewModel.showError("无法打开浏览器")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AnimatedSecondaryTopBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    title = {
                        Text(
                            text = "关于",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "其他",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { contentPadding ->
        AuroraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo and Name
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLogoClick
                        ),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "糖原",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = LiteraryFontFamily
                    )
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(48.dp))

                // Info Sections
                InfoSection(title = "开发团队") {
                    InfoItem(label = "Lead Developer", value = "线粒体 XianliticCN")
                    InfoItem(label = "Algorithm", value = "南木, 浩瀚之渺, Legend")
                    InfoItem(label = "Business Manager", value = "なかのみく")
                    InfoItem(label = "Test", value = "NukeCirno, 嘉木, 猕猴桃教教主")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "所有成员皆参与了开发过程的全部工作。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                InfoSection(title = "Logo 设计") {
                    InfoItem(value = "南木")
                }

                InfoSection(title = "代号设计") {
                    InfoItem(value = "NukeCirno")
                }

                InfoSection(title = "特别纪念") {
                    Text(
                        text = "糖原在一台序列号为 6a34145e 的小米5上完成调试。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

        // 页面内的全局消息提示
        GlobalMessageHost(
            message = currentMessage,
            onDismiss = { messageViewModel.dismiss() }
        )
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = TangyuanGeneralFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun InfoItem(label: String? = null, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    TangyuanTheme {
        Surface {
            AboutScreen()
        }
    }
}
