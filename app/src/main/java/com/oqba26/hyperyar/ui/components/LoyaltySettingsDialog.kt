package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oqba26.hyperyar.util.cleanNumber
import com.oqba26.hyperyar.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltySettingsDialog(
    enabled: Boolean,
    rate: String,
    value: String,
    onDismiss: () -> Unit,
    onSave: (Boolean, String, String) -> Unit
) {
    var localEnabled by remember { mutableStateOf(enabled) }
    var localRate by remember { mutableStateOf(rate) }
    var localValue by remember { mutableStateOf(value) }

    HyperDialog(
        onDismissRequest = onDismiss,
        title = "تنظیمات باشگاه مشتریان",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فعال‌سازی سیستم امتیازدهی", modifier = Modifier.weight(1f))
                    Switch(checked = localEnabled, onCheckedChange = { localEnabled = it })
                }

                if (localEnabled) {
                    OutlinedTextField(
                        value = localRate.toPersianDigits(),
                        onValueChange = { localRate = it.cleanNumber() },
                        label = { Text("به ازای هر چند تومان خرید (۱ امتیاز)؟") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = localValue.toPersianDigits(),
                        onValueChange = { localValue = it.cleanNumber() },
                        label = { Text("ارزش هر امتیاز (تومان تخفیف)؟") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(localEnabled, localRate, localValue) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("ذخیره", style = MaterialTheme.typography.labelLarge)
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
                Text("انصراف", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
