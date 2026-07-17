package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun HyperDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    confirmButton: (@Composable RowScope.() -> Unit)? = null,
    dismissButton: (@Composable RowScope.() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    content()

                    if (confirmButton != null || dismissButton != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // دکمه تایید در سمت راست (در RTL اولین عنصر Row است)
                            confirmButton?.invoke(this)
                            // دکمه انصراف در سمت چپ (در RTL دومین عنصر Row است)
                            dismissButton?.invoke(this)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "تایید",
    cancelText: String = "انصراف",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    isDanger: Boolean = false
) {
    HyperDialog(
        onDismissRequest = onDismiss,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDanger) MaterialTheme.colorScheme.error else confirmColor
                )
            ) {
                Text(confirmText, color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(cancelText, style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
