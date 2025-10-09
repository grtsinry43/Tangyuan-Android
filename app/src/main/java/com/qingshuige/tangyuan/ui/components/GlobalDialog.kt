package com.qingshuige.tangyuan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 确认对话框数据类
 */
data class ConfirmDialogData(
    val title: String,
    val message: String,
    val confirmText: String = "确认",
    val dismissText: String = "取消",
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit = {}
)

/**
 * 全局确认对话框组件
 * 符合Material Design 3设计规范
 */
@Composable
fun GlobalConfirmDialog(
    dialogData: ConfirmDialogData?,
    onDismissRequest: () -> Unit
) {
    if (dialogData != null) {
        AlertDialog(
            onDismissRequest = {
                dialogData.onDismiss()
                onDismissRequest()
            },
            title = {
                Text(
                    text = dialogData.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = dialogData.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        dialogData.onConfirm()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(dialogData.confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogData.onDismiss()
                        onDismissRequest()
                    }
                ) {
                    Text(
                        dialogData.dismissText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

/**
 * 自定义样式的确认对话框（更现代化的设计）
 */
@Composable
fun ModernConfirmDialog(
    dialogData: ConfirmDialogData?,
    onDismissRequest: () -> Unit
) {
    if (dialogData != null) {
        Dialog(
            onDismissRequest = {
                dialogData.onDismiss()
                onDismissRequest()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题
                    Text(
                        text = dialogData.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 消息内容
                    Text(
                        text = dialogData.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )

                    // 按钮行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = androidx.compose.ui.Alignment.End)
                    ) {
                        // 取消按钮
                        TextButton(
                            onClick = {
                                dialogData.onDismiss()
                                onDismissRequest()
                            }
                        ) {
                            Text(
                                dialogData.dismissText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 确认按钮
                        Button(
                            onClick = {
                                dialogData.onConfirm()
                                onDismissRequest()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                dialogData.confirmText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
