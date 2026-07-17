package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oqba26.hyperyar.util.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    HyperDialog(
        onDismissRequest = { if (!updateInfo.isForceUpdate) onDismiss() },
        title = "به‌روزرسانی جدید موجود است",
        content = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "نسخه ${updateInfo.versionName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "تغییرات این نسخه:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = updateInfo.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (updateInfo.isForceUpdate) {
                    Text(
                        text = "نصب این به‌روزرسانی برای ادامه استفاده از برنامه الزامی است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("تایید و بروزرسانی", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = if (!updateInfo.isForceUpdate) {
            {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("بعداً", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else null
    )
}
