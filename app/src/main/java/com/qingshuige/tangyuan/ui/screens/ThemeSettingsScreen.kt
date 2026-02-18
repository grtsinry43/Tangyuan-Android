package com.qingshuige.tangyuan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qingshuige.tangyuan.ui.theme.AppThemeMode
import com.qingshuige.tangyuan.ui.theme.ThemePolicy
import com.qingshuige.tangyuan.utils.PrefsManager
import com.qingshuige.tangyuan.utils.UIUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val themeModeValue by PrefsManager.getStringFlow(
        key = PrefsManager.Keys.APP_THEME_MODE,
        defaultValue = AppThemeMode.DEFAULT.value
    ).collectAsState(initial = AppThemeMode.DEFAULT.value)
    val userOverridden by PrefsManager.getBooleanFlow(
        key = PrefsManager.Keys.APP_THEME_USER_OVERRIDDEN,
        defaultValue = false
    ).collectAsState(initial = false)
    val savedMode = AppThemeMode.fromValue(themeModeValue)
    val effectiveMode = ThemePolicy.resolveThemeMode(savedMode, userOverridden)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "主题选择",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "当前生效：${themeLabel(effectiveMode)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "自动节日区间：2026-02-16 至 2026-03-03。手动选择后不再自动覆盖。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (userOverridden) "当前来源：手动选择" else "当前来源：自动节日策略",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ThemeCard(
                title = "默认主题",
                subtitle = "蓝白清爽风格",
                selected = effectiveMode == AppThemeMode.DEFAULT,
                onClick = {
                    scope.launch {
                        PrefsManager.putString(PrefsManager.Keys.APP_THEME_MODE, AppThemeMode.DEFAULT.value)
                        PrefsManager.putBoolean(PrefsManager.Keys.APP_THEME_USER_OVERRIDDEN, true)
                        UIUtils.showSuccess("已应用主题：默认主题")
                    }
                }
            )

            ThemeCard(
                title = "春节主题",
                subtitle = "红金节庆风格",
                selected = effectiveMode == AppThemeMode.SPRING_FESTIVAL,
                onClick = {
                    scope.launch {
                        PrefsManager.putString(PrefsManager.Keys.APP_THEME_MODE, AppThemeMode.SPRING_FESTIVAL.value)
                        PrefsManager.putBoolean(PrefsManager.Keys.APP_THEME_USER_OVERRIDDEN, true)
                        UIUtils.showSuccess("已应用主题：春节主题")
                    }
                }
            )

            if (userOverridden) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        scope.launch {
                            PrefsManager.putBoolean(PrefsManager.Keys.APP_THEME_USER_OVERRIDDEN, false)
                            UIUtils.showSuccess("已恢复自动节日策略")
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("恢复自动节日策略")
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun themeLabel(mode: AppThemeMode): String {
    return when (mode) {
        AppThemeMode.DEFAULT -> "默认主题"
        AppThemeMode.SPRING_FESTIVAL -> "春节主题"
    }
}
