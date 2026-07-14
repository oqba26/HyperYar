package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.oqba26.hyperyar.data.DebtTransaction
import com.oqba26.hyperyar.data.Supplier
import com.oqba26.hyperyar.util.PersianNumberVisualTransformation
import com.oqba26.hyperyar.util.cleanNumber
import com.oqba26.hyperyar.util.toPersianDateTimeString
import com.oqba26.hyperyar.util.toPersianDigits
import com.oqba26.hyperyar.util.toPersianPrice

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عمومی") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("ثبت هزینه جدید", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
                    
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان هزینه") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = amount, 
                        onValueChange = { amount = it.cleanNumber() }, 
                        label = { Text("مبلغ (تومان)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PersianNumberVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("دسته بندی") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("توضیحات (اختیاری)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (title.isNotBlank() && amount.isNotBlank()) onConfirm(title, amount.toDoubleOrNull() ?: 0.0, category, description) }, modifier = Modifier.weight(1f)) {
                            Text("ثبت")
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("افزودن تامین‌کننده", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
                    
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام شرکت / شخص") }, leadingIcon = { Icon(Icons.Default.Business, null) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone.toPersianDigits(), onValueChange = { phone = it.cleanNumber() }, label = { Text("شماره تماس") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (name.isNotBlank()) onConfirm(name, phone, address) }, modifier = Modifier.weight(1f)) {
                            Text("ثبت")
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddChequeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Long, String, String) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("دریافتی") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("ثبت چک جدید", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
                    
                    OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("شماره چک") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("بانک صادرکننده") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = amount, 
                        onValueChange = { amount = it.cleanNumber() }, 
                        label = { Text("مبلغ چک (تومان)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                        visualTransformation = PersianNumberVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = person, onValueChange = { person = it }, label = { Text("در وجه / از طرف") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("نوع چک:", modifier = Modifier.padding(top = 16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "دریافتی", onClick = { type = "دریافتی" })
                        Text("دریافتی")
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = type == "پرداختی", onClick = { type = "پرداختی" })
                        Text("پرداختی")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (number.isNotBlank()) onConfirm(number, bank, amount.toDoubleOrNull() ?: 0.0, System.currentTimeMillis(), person, type) }, modifier = Modifier.weight(1f)) {
                            Text("ثبت")
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailDialog(
    supplier: Supplier,
    transactions: List<DebtTransaction>,
    onDismiss: () -> Unit,
    onAddPayment: (Double, String) -> Unit
) {
    var showAddPayment by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentDesc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(supplier.name, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "بدهی ما: ${supplier.balance.toPersianPrice()}",
                                color = if (supplier.balance > 0) Color.Red else Color(0xFF2E7D32),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    if (showAddPayment) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("ثبت پرداختی به تامین‌کننده", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = paymentAmount,
                                    onValueChange = { paymentAmount = it.cleanNumber() },
                                    label = { Text("مبلغ (تومان)") },
                                    visualTransformation = PersianNumberVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(value = paymentDesc, onValueChange = { paymentDesc = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { showAddPayment = false }) { Text("انصراف") }
                                    Button(onClick = {
                                        if (paymentAmount.isNotBlank()) {
                                            onAddPayment(paymentAmount.toDoubleOrNull() ?: 0.0, paymentDesc)
                                            showAddPayment = false
                                            paymentAmount = ""
                                            paymentDesc = ""
                                        }
                                    }) { Text("ثبت") }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Button(onClick = { showAddPayment = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Payment, null)
                            Spacer(Modifier.width(8.dp))
                            Text("ثبت پرداخت وجه")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("تاریخچه تراکنش‌ها", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                    
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("تراکنشی یافت نشد") }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(transactions) { transaction ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(if (transaction.type == "Payment") "پرداخت ما" else "بدهی جدید (خرید)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(transaction.timestamp.toPersianDateTimeString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(transaction.amount.toPersianPrice(), color = if (transaction.type == "Payment") Color(0xFF2E7D32) else Color.Red, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}
