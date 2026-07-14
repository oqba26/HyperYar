package com.oqba26.hyperyar.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.oqba26.hyperyar.data.Customer
import com.oqba26.hyperyar.data.DebtTransaction
import com.oqba26.hyperyar.data.InvoiceWithItems
import com.oqba26.hyperyar.util.*

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val context = LocalContext.current
    var suggestedContacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }

    LaunchedEffect(name) {
        suggestedContacts = if (name.length > 1 && phone.isBlank()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                ContactHelper.getContactsByName(context, name)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
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
                        text = "افزودن مشتری جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام و نام خانوادگی") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (suggestedContacts.isNotEmpty()) {
                            Text(
                                text = "یافت شده در مخاطبین گوشی:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(suggestedContacts) { contact ->
                                    SuggestionChip(
                                        onClick = {
                                            name = contact.name
                                            phone = contact.phoneNumber
                                            suggestedContacts = emptyList()
                                        },
                                        label = {
                                            Column {
                                                Text(contact.name, style = MaterialTheme.typography.labelSmall)
                                                Text(contact.phoneNumber.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره تماس") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("آدرس") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
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
                                        Customer(
                                            name = name,
                                            phone = phone,
                                            address = address,
                                            balance = 0.0
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
                            Text("ثبت", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
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
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }
    var balance by remember { mutableStateOf(customer.balance.toString()) }

    val context = LocalContext.current
    var suggestedContacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }

    LaunchedEffect(name) {
        suggestedContacts = if (name.length > 1 && phone.isBlank()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                ContactHelper.getContactsByName(context, name)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
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
                        text = "ویرایش اطلاعات مشتری",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام و نام خانوادگی") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (suggestedContacts.isNotEmpty()) {
                            Text(
                                text = "یافت شده در مخاطبین گوشی:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(suggestedContacts) { contact ->
                                    SuggestionChip(
                                        onClick = {
                                            name = contact.name
                                            phone = contact.phoneNumber
                                            suggestedContacts = emptyList()
                                        },
                                        label = {
                                            Column {
                                                Text(contact.name, style = MaterialTheme.typography.labelSmall)
                                                Text(contact.phoneNumber.toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phone.toPersianDigits(),
                            onValueChange = { phone = it.cleanNumber() },
                            label = { Text("شماره تماس") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("آدرس") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        OutlinedTextField(
                            value = balance,
                            onValueChange = { balance = it.cleanNumber() },
                            label = { Text("مانده حساب") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PersianNumberVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
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
                                        customer.copy(
                                            name = name,
                                            phone = phone,
                                            address = address,
                                            balance = balance.toDoubleOrNull() ?: 0.0
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
fun CustomerDetailDialog(
    customer: Customer,
    transactions: List<DebtTransaction>,
    allInvoices: List<InvoiceWithItems>,
    onDismiss: () -> Unit,
    onAddPayment: (Double, String) -> Unit,
    onPayInstallment: (DebtTransaction) -> Unit,
    onDeleteInstallment: (DebtTransaction) -> Unit
) {
    val customerInvoices = remember(allInvoices, customer.id) {
        allInvoices.filter { it.invoice.customerId == customer.id }.sortedByDescending { it.invoice.timestamp }
    }

    val installments = remember(transactions) {
        transactions.filter { 
            (it.type == "Debt") && 
            (it.dueDate != null) && 
            (it.description.startsWith("قسط")) 
        }.sortedBy { it.dueDate!! }
    }

    val totalPurchased = customerInvoices.sumOf { it.invoice.totalAmount }
    val totalPaid = customerInvoices.sumOf { it.invoice.amountPaid } + transactions.filter { it.type == "Payment" }.sumOf { it.amount }

    var showAddPayment by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentDesc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(4.dp)
            ) {
                Column {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(
                            customer.name, 
                            style = MaterialTheme.typography.titleLarge, 
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Financial Summary
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FinancialRow("کل خریدها:", totalPurchased.toPersianPrice(), MaterialTheme.colorScheme.onSurface)
                                    FinancialRow("مجموع پرداختی:", totalPaid.toPersianPrice(), Color(0xFF2E7D32))
                                    HorizontalDivider()
                                    FinancialRow("مانده بدهی کل:", customer.balance.toPersianPrice(), if (customer.balance < 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32), isBold = true)
                                }
                            }
                        }

                        item {
                            if (showAddPayment) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("ثبت پرداختی جدید", fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = paymentAmount,
                                            onValueChange = { paymentAmount = it.cleanNumber() },
                                            label = { Text("مبلغ (تومان)") },
                                            visualTransformation = PersianNumberVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = paymentDesc,
                                            onValueChange = { paymentDesc = it },
                                            label = { Text("توضیحات") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (paymentAmount.isNotBlank()) {
                                                        onAddPayment(paymentAmount.toDoubleOrNull() ?: 0.0, paymentDesc)
                                                        showAddPayment = false
                                                        paymentAmount = ""
                                                        paymentDesc = ""
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("ثبت", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                            }
                                            Button(
                                                onClick = { showAddPayment = false },
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
                            } else {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Button(
                                        onClick = { showAddPayment = true },
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("ثبت دریافت وجه")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "تاریخچه فاکتورها و اقساط",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (customerInvoices.isEmpty() && transactions.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("سابقه ای یافت نشد.")
                                }
                            }
                        } else {
                            items(customerInvoices) { invoiceWithItems ->
                                val invoiceInstallments = installments.filter { it.invoiceId == invoiceWithItems.invoice.id }
                                InvoiceAndInstallmentGroup(
                                    invoiceWithItems = invoiceWithItems,
                                    installments = invoiceInstallments,
                                    onPayInstallment = onPayInstallment,
                                    onDeleteInstallment = onDeleteInstallment
                                )
                            }
                            
                            // Also show other transactions not tied to invoices if any
                            val otherTransactions = transactions.filter { it.invoiceId == null }
                            if (otherTransactions.isNotEmpty()) {
                                item {
                                    Text("سایر تراکنش‌ها", style = MaterialTheme.typography.titleSmall)
                                }
                                items(otherTransactions) { transaction ->
                                    TransactionItem(transaction)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceAndInstallmentGroup(
    invoiceWithItems: InvoiceWithItems,
    installments: List<DebtTransaction>,
    onPayInstallment: (DebtTransaction) -> Unit,
    onDeleteInstallment: (DebtTransaction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            InvoiceHistoryCard(invoiceWithItems, showDueDate = installments.isEmpty())
            
            if (installments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "اقساط این فاکتور:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    installments.forEach { installment ->
                        InstallmentRow(
                            transaction = installment,
                            onPayClick = { onPayInstallment(installment) },
                            onDeleteClick = { onDeleteInstallment(installment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentRow(
    transaction: DebtTransaction,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isPaid) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, if (transaction.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.isPaid) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = transaction.dueDate?.toPersianDateString() ?: "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (transaction.isPaid) Color(0xFF2E7D32).copy(alpha = 0.7f) else Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount.toPersianPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
                
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!transaction.isPaid) {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                        Button(
                            onClick = onPayClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("تسویه", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun InvoiceHistoryCard(invoiceWithItems: InvoiceWithItems, showDueDate: Boolean) {
    val inv = invoiceWithItems.invoice
    val isSettled = inv.amountPaid >= inv.totalAmount
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "فاکتور #${inv.id}", fontWeight = FontWeight.Bold)
                Text(
                    text = if (isSettled) "تسویه شده" else "نسیه/مانده دار",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
            
            Text(
                text = inv.timestamp.toPersianDateString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("مبلغ فاکتور", style = MaterialTheme.typography.labelSmall)
                    Text(inv.totalAmount.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("پرداختی", style = MaterialTheme.typography.labelSmall)
                    Text(inv.amountPaid.toPersianPrice(), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                }
            }

            if (!isSettled && inv.dueDate != null && showDueDate) {
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "سررسید بدهی: ${inv.dueDate.toPersianDateString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: DebtTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                if (transaction.type == "Payment") "دریافت وجه" else "بدهی جدید (فاکتور)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(transaction.timestamp.toPersianDateTimeString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (transaction.description.isNotBlank()) {
                Text(transaction.description, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(
            transaction.amount.toPersianPrice(),
            color = if (transaction.type == "Payment") Color(0xFF2E7D32) else Color.Red,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
}
