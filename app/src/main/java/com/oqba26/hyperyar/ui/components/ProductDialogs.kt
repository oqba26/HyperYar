package com.oqba26.hyperyar.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.oqba26.hyperyar.data.InventoryLog
import com.oqba26.hyperyar.data.Product
import com.oqba26.hyperyar.util.CategorizationHelper
import com.oqba26.hyperyar.util.PersianNumberVisualTransformation
import com.oqba26.hyperyar.util.cleanNumber
import com.oqba26.hyperyar.util.toPersianDateTimeString
import com.oqba26.hyperyar.util.toPersianNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("عدد") }
    var category by remember { mutableStateOf("عمومی") }
    var expanded by remember { mutableStateOf(false) }
    
    // پرچم‌هایی برای تشخیص تغییر دستی توسط کاربر
    var isUnitManuallySet by remember { mutableStateOf(false) }
    var isCategoryManuallySet by remember { mutableStateOf(false) }

    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(name) {
        if (name.length > 1) {
            val suggestedCat = CategorizationHelper.suggestCategory(name)
            if (!isCategoryManuallySet) {
                category = suggestedCat
            }
            
            val suggestedUnit = CategorizationHelper.suggestUnit(name)
            if (!isUnitManuallySet) {
                unit = suggestedUnit
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
        else Toast.makeText(context, "دسترسی به دوربین داده نشد", Toast.LENGTH_SHORT).show()
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false },
            onBarcodeScanned = {
                barcode = it
                showScanner = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "افزودن کالای جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام کالا") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("بارکد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        showScanner = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "اسکن بارکد")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = sellPrice,
                            onValueChange = { sellPrice = it },
                            label = { Text("قیمت فروش") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = buyPrice,
                            onValueChange = { buyPrice = it },
                            label = { Text("قیمت خرید") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("موجودی اولیه") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { 
                                unit = it
                                isUnitManuallySet = true
                            },
                            label = { Text("واحد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { 
                                    category = it
                                    isCategoryManuallySet = true
                                },
                                label = { Text("دسته‌بندی") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                CategorizationHelper.defaultCategories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            category = selectionOption
                                            isCategoryManuallySet = true
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onConfirm(
                                        Product(
                                            name = name,
                                            barcode = barcode,
                                            sellPrice = sellPrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            buyPrice = buyPrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            stock = stock.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            unit = unit,
                                            category = category
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("ثبت کالا", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("انصراف", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var sellPrice by remember { mutableStateOf(product.sellPrice.toString()) }
    var buyPrice by remember { mutableStateOf(product.buyPrice.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var unit by remember { mutableStateOf(product.unit) }
    var category by remember { mutableStateOf(product.category) }
    var expanded by remember { mutableStateOf(false) }
    
    var isUnitManuallySet by remember { mutableStateOf(false) }
    var isCategoryManuallySet by remember { mutableStateOf(false) }

    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(name) {
        if (name.length > 1 && name != product.name) {
            val suggestedCat = CategorizationHelper.suggestCategory(name)
            if (!isCategoryManuallySet) {
                category = suggestedCat
            }
            
            val suggestedUnit = CategorizationHelper.suggestUnit(name)
            if (!isUnitManuallySet) {
                unit = suggestedUnit
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
        else Toast.makeText(context, "دسترسی به دوربین داده نشد", Toast.LENGTH_SHORT).show()
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false },
            onBarcodeScanned = {
                barcode = it
                showScanner = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "ویرایش کالا",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام کالا") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("بارکد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        showScanner = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "اسکن بارکد")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = sellPrice,
                            onValueChange = { sellPrice = it },
                            label = { Text("قیمت فروش") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = buyPrice,
                            onValueChange = { buyPrice = it },
                            label = { Text("قیمت خرید") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("موجودی") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { 
                                unit = it
                                isUnitManuallySet = true
                            },
                            label = { Text("واحد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { 
                                    category = it
                                    isCategoryManuallySet = true
                                },
                                label = { Text("دسته‌بندی") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                CategorizationHelper.defaultCategories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            category = selectionOption
                                            isCategoryManuallySet = true
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onConfirm(
                                        product.copy(
                                            name = name,
                                            barcode = barcode,
                                            sellPrice = sellPrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            buyPrice = buyPrice.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            stock = stock.cleanNumber().toDoubleOrNull() ?: 0.0,
                                            unit = unit,
                                            category = category,
                                            isSynced = false // Reset sync status on update
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("بروزرسانی", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("انصراف", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WasteDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("شکستگی / خرابی") }
    var expanded by remember { mutableStateOf(false) }
    val reasons = listOf("شکستگی / خرابی", "تاریخ گذشته", "مفقودی", "سایر")

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("ثبت ضایعات", style = MaterialTheme.typography.headlineSmall)
                    Text(product.name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.cleanNumber() },
                        label = { Text("مقدار ضایعات (${product.unit})") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("علت") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            reasons.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        reason = r
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val amt = amount.toDoubleOrNull() ?: 0.0
                                if (amt > 0) onConfirm(amt, reason)
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("تایید") }
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryHistoryDialog(
    product: Product,
    logs: List<InventoryLog>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("تاریخچه موجودی", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text(product.name, style = MaterialTheme.typography.headlineSmall)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    if (logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("هیچ تغییری ثبت نشده است")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(logs) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = when (log.type) {
                                                "Sale" -> "فروش"
                                                "Purchase" -> "خرید / ورود کالا"
                                                else -> "تغییر دستی"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(log.timestamp.toPersianDateTimeString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        if (log.note.isNotBlank()) {
                                            Text(log.note, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text(
                                        text = (if (log.changeAmount > 0) "+" else "") + log.changeAmount.toPersianNumber(),
                                        color = if (log.changeAmount > 0) Color(0xFF2E7D32) else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}
