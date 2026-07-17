package com.oqba26.hyperyar.ui.components

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsDialog(
    currentType: String,
    currentAddress: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(currentType) }
    var selectedAddress by remember { mutableStateOf(currentAddress) }

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var usbDevices by remember { mutableStateOf<List<UsbDevice>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Refresh logic
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedType == "BT") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
                }
            }
            try {
                pairedDevices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            } catch (e: SecurityException) {
                Toast.makeText(context, "عدم دسترسی به بلوتوث", Toast.LENGTH_SHORT).show()
            }
        } else if (selectedType == "USB") {
            usbDevices = usbManager.deviceList.values.toList()
        }
    }

    HyperDialog(
        onDismissRequest = onDismiss,
        title = "تنظیمات چاپگر فاکتور",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("روش چاپ را انتخاب کنید:", style = MaterialTheme.typography.titleMedium)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == "SHARE", onClick = { selectedType = "SHARE"; selectedAddress = "" })
                        Icon(Icons.Default.Print, null, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("اشتراک‌گذاری متن (پیش‌فرض)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == "BT", onClick = { selectedType = "BT" })
                        Icon(Icons.Default.Bluetooth, null, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("اتصال بلوتوث (Bluetooth)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == "USB", onClick = { selectedType = "USB" })
                        Icon(Icons.Default.Usb, null, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("اتصال کابل (USB OTG)")
                    }
                }

                HorizontalDivider()

                if (selectedType == "BT") {
                    Text("لیست دستگاه‌های جفت شده:", style = MaterialTheme.typography.labelMedium)
                    if (pairedDevices.isEmpty()) {
                        Text("دستگاهی یافت نشد. ابتدا در تنظیمات گوشی جفت کنید.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(pairedDevices) { device ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                @Suppress("MissingPermission")
                                val deviceName = remember(device) {
                                    try {
                                        device.name ?: "Unknown"
                                    } catch (e: SecurityException) {
                                        "Unknown"
                                    }
                                }
                                RadioButton(selected = selectedAddress == device.address, onClick = { selectedAddress = device.address })
                                Text(text = "$deviceName (${device.address})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else if (selectedType == "USB") {
                    Text("دستگاه‌های USB متصل:", style = MaterialTheme.typography.labelMedium)
                    if (usbDevices.isEmpty()) {
                        Text("چاپگری متصل نیست. کابل OTG را بررسی کنید.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(usbDevices) { device ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val address = "${device.vendorId}:${device.productId}"
                                RadioButton(selected = selectedAddress == address, onClick = { selectedAddress = address })
                                Text(text = "${device.deviceName} ($address)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedType, selectedAddress) },
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
