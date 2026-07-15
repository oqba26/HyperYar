package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.hyperyar.data.CartItem
import com.oqba26.hyperyar.data.Customer
import com.oqba26.hyperyar.data.Supplier
import com.oqba26.hyperyar.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheetContent(
    cartItems: List<CartItem>,
    isPurchaseMode: Boolean = false,
    customers: List<Customer> = emptyList(),
    suppliers: List<Supplier> = emptyList(),
    onPersonSelected: (Int?) -> Unit,
    onRemove: (CartItem) -> Unit,
    onRedeemPoints: (Int) -> Double,
    onCheckout: (Double, Double, String?, String?, Long?, List<Pair<Double, Long?>>?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var manualCustomerName by remember { mutableStateOf("") }
    var manualCustomerPhone by remember { mutableStateOf("") }
    var pointsDiscount by remember { mutableDoubleStateOf(0.0) }
    
    var amountPaidText by remember { mutableStateOf("") }
    var totalDiscountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var installments by remember { mutableStateOf<List<Pair<Double, Long?>>>(emptyList()) }

    val totalAmount = cartItems.sumOf { it.totalPrice }
    val discountValue = (totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0) + pointsDiscount
    val finalAmount = (totalAmount - discountValue).coerceAtLeast(0.0)
    val remainingDebt = finalAmount - (amountPaidText.cleanNumber().toDoubleOrNull() ?: 0.0)
    val isDebt = remainingDebt > 0 && (selectedPersonId != null || manualCustomerName.isNotBlank())

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPurchaseMode) "فاکتور خرید (ورود کالا)" else "سبد خرید (فروش)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isPurchaseMode) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary
                )
                
                // Person Selector (Smart Selection)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    OutlinedTextField(
                        value = manualCustomerName,
                        onValueChange = { input ->
                            manualCustomerName = input
                            val matchId: Int?
                            val matchPhone: String?
                            if (isPurchaseMode) {
                                val match = suppliers.find { it.name == input }
                                matchId = match?.id
                                matchPhone = match?.phone
                            } else {
                                val match = customers.find { it.name == input }
                                matchId = match?.id
                                matchPhone = match?.phone
                            }
                            selectedPersonId = matchId
                            if (matchPhone != null) manualCustomerPhone = matchPhone
                            onPersonSelected(matchId)
                            expanded = true
                        },
                        label = { Text(if (isPurchaseMode) "نام تامین‌کننده" else "نام مشتری / فروش آزاد") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    val filteredItems = if (isPurchaseMode) {
                        suppliers.filter { it.name.contains(manualCustomerName, ignoreCase = true) }
                    } else {
                        customers.filter { it.name.contains(manualCustomerName, ignoreCase = true) }
                    }

                    if (filteredItems.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredItems.forEach { item ->
                                val name = if (item is Customer) item.name else (item as Supplier).name
                                val phoneStr = if (item is Customer) item.phone else (item as Supplier).phone
                                val id = if (item is Customer) item.id else (item as Supplier).id

                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(name)
                                            if (phoneStr.isNotBlank()) {
                                                Text(phoneStr.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        manualCustomerName = name
                                        manualCustomerPhone = phoneStr
                                        selectedPersonId = id
                                        onPersonSelected(id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (manualCustomerName.isNotBlank() || selectedPersonId != null) {
                OutlinedTextField(
                    value = manualCustomerPhone.toPersianDigits(),
                    onValueChange = { manualCustomerPhone = it.cleanNumber() },
                    label = { Text("شماره تماس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            if (!isPurchaseMode && selectedPersonId != null) {
                val customer = customers.find { it.id == selectedPersonId }
                if (customer != null && customer.loyaltyPoints >= 10) {
                    Button(
                        onClick = { 
                            pointsDiscount = onRedeemPoints(customer.id)
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("استفاده از ${customer.loyaltyPoints} امتیاز (${(customer.loyaltyPoints * 1000).toDouble().toPersianPrice()} تخفیف)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("سبد خرید شما خالی است")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${item.quantity.toPersianNumber()} ${item.product.unit} × ${item.sellPrice.toPersianPrice()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                item.totalPrice.toPersianPrice(),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                    
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            OutlinedTextField(
                                value = amountPaidText,
                                onValueChange = { amountPaidText = it.cleanNumber() },
                                label = { Text("مبلغ پرداختی (تومان)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = totalDiscountText,
                                onValueChange = { totalDiscountText = it.cleanNumber() },
                                label = { Text("تخفیف دستی (تومان)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberVisualTransformation()
                            )

                            if (isDebt) {
                                InstallmentSection(
                                    remainingDebt = remainingDebt,
                                    dueDate = dueDate,
                                    onDueDateChange = { dueDate = it },
                                    installments = installments,
                                    onInstallmentsChange = { installments = it }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (pointsDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تخفیف امتیاز:")
                            Text("- ${pointsDiscount.toPersianPrice()}", color = Color(0xFF2E7D32))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("جمع کل:", fontWeight = FontWeight.Bold)
                        Text(finalAmount.toPersianPrice(), color = if (isPurchaseMode) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { 
                            val manualDiscount = totalDiscountText.cleanNumber().toDoubleOrNull() ?: 0.0
                            val paid = amountPaidText.cleanNumber().toDoubleOrNull() ?: finalAmount
                            onCheckout(paid, manualDiscount + pointsDiscount, manualCustomerName, manualCustomerPhone, dueDate, installments.ifEmpty { null }) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPurchaseMode) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isPurchaseMode) "ثبت فاکتور خرید" else "ثبت نهایی و چاپ فاکتور")
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentSection(
    remainingDebt: Double,
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    installments: List<Pair<Double, Long?>>,
    onInstallmentsChange: (List<Pair<Double, Long?>>) -> Unit
) {
    var showInstallmentDialog by remember { mutableStateOf(false) }
    var showDatePickerForIndex by remember { mutableStateOf<Int?>(null) } // -1 for main dueDate, >=0 for installments

    // هوشمندسازی موعد نهایی تسویه بر اساس آخرین قسط
    LaunchedEffect(installments) {
        if (installments.isNotEmpty()) {
            val lastDate = installments.mapNotNull { it.second }.maxOrNull()
            if (lastDate != null) {
                onDueDateChange(lastDate)
            }
        }
    }

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = "مدیریت پرداخت و اقساط",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { showInstallmentDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("افزودن قسط")
        }

        // فقط اگر قسطی نباشد، انتخاب دستی موعد نهایی نمایش داده می‌شود
        if (installments.isEmpty()) {
            Button(
                onClick = { showDatePickerForIndex = -1 },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("موعد نهایی تسویه", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = dueDate?.toPersianDateString() ?: "انتخاب تاریخ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Installments List
        installments.forEachIndexed { index, pair ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قسط ${index + 1}", style = MaterialTheme.typography.labelSmall)
                        Text(pair.first.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تاریخ سررسید", style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = { showDatePickerForIndex = index }) {
                            Text(pair.second?.toPersianDateString() ?: "انتخاب")
                        }
                    }
                    IconButton(onClick = { 
                        val newList = installments.toMutableList()
                        newList.removeAt(index)
                        onInstallmentsChange(newList)
                    }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (showInstallmentDialog) {
            AddInstallmentDialog(
                remainingDebt = remainingDebt - installments.sumOf { it.first },
                onDismiss = { showInstallmentDialog = false },
                onConfirm = { amount, date ->
                    onInstallmentsChange(installments + (amount to date))
                    showInstallmentDialog = false
                }
            )
        }

        showDatePickerForIndex?.let { index ->
            ShamsiDatePicker(
                initialTimestamp = if (index == -1) dueDate else installments.getOrNull(index)?.second,
                onDismiss = { showDatePickerForIndex = null },
                onDateSelected = { timestamp ->
                    if (index == -1) {
                        onDueDateChange(timestamp)
                    } else {
                        val newList = installments.toMutableList()
                        newList[index] = newList[index].first to timestamp
                        onInstallmentsChange(newList)
                    }
                    showDatePickerForIndex = null
                }
            )
        }
    }
}

@Composable
fun AddInstallmentDialog(
    remainingDebt: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long?) -> Unit
) {
    var amountText by remember { mutableStateOf(if (remainingDebt > 0) remainingDebt.toLong().toString() else "") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("افزودن قسط جدید", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.cleanNumber() },
                    label = { Text("مبلغ قسط (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianNumberVisualTransformation()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dueDate?.toPersianDateString() ?: "",
                    onValueChange = { },
                    label = { Text("تاریخ سررسید") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val amt = amountText.cleanNumber().toDoubleOrNull() ?: 0.0
                            if (amt > 0) onConfirm(amt, dueDate) 
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("تایید") }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("انصراف")
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        ShamsiDatePicker(
            initialTimestamp = dueDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { 
                dueDate = it
                showDatePicker = false
            }
        )
    }
}
