package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.InvoiceType
import com.oqba26.hyperyar.data.InvoiceWithItems
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.ConfirmDialog
import com.oqba26.hyperyar.util.InvoicePdfHelper
import com.oqba26.hyperyar.util.toPersianDateString
import com.oqba26.hyperyar.util.toPersianDigits
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ProductViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoicesWithItems.collectAsStateWithLifecycle()
    val settingsManager = remember { com.oqba26.hyperyar.data.SettingsManager(context) }
    val shopName by settingsManager.shopName.collectAsState(initial = "")
    val displayTitle = remember(shopName) {
        if (shopName.isBlank() || shopName == "هایپر من") "هایپرمارکت" 
        else "هایپرمارکت $shopName"
    }

    var selectedInvoiceForAction by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var actionType by remember { mutableStateOf("") } // "delete" or "refund"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text("تاریخچه فاکتورها", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()).padding(bottom = bottomPadding).fillMaxSize()) {
            if (invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هنوز هیچ فاکتوری ثبت نشده است.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoices, key = { it.invoice.id }) { invoiceWithItems ->
                        InvoiceHistoryItem(
                            invoiceWithItems = invoiceWithItems,
                            onShare = {
                                InvoicePdfHelper.generateAndShareInvoice(context, invoiceWithItems, displayTitle)
                            },
                            onDelete = {
                                selectedInvoiceForAction = invoiceWithItems
                                actionType = "delete"
                            },
                            onRefund = {
                                selectedInvoiceForAction = invoiceWithItems
                                actionType = "refund"
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedInvoiceForAction != null) {
        val invoice = selectedInvoiceForAction!!
        ConfirmDialog(
            title = if (actionType == "delete") "حذف فاکتور" else "مرجوع کردن فاکتور",
            message = if (actionType == "delete")
                "آیا از حذف فاکتور #${invoice.invoice.id.toString().toPersianDigits()} اطمینان دارید؟ این عمل قابل بازگشت نیست."
            else
                "آیا می‌خواهید فاکتور #${invoice.invoice.id.toString().toPersianDigits()} را مرجوع کنید؟ با این کار یک فاکتور معکوس ثبت شده و موجودی انبار اصلاح می‌شود.",
            confirmText = "تایید",
            cancelText = "انصراف",
            onConfirm = {
                if (actionType == "delete") {
                    viewModel.deleteInvoice(invoice)
                } else {
                    viewModel.refundInvoice(invoice)
                }
                selectedInvoiceForAction = null
            },
            onDismiss = { selectedInvoiceForAction = null },
            isDanger = actionType == "delete"
        )
    }
}

@Composable
fun InvoiceHistoryItem(
    invoiceWithItems: InvoiceWithItems,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRefund: () -> Unit
) {
    val inv = invoiceWithItems.invoice
    val isReturn = inv.type == InvoiceType.RETURN_SALE || inv.type == InvoiceType.RETURN_PURCHASE

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReturn) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (inv.type) {
                            InvoiceType.SALE -> "فاکتور فروش #${inv.id}"
                            InvoiceType.PURCHASE -> "فاکتور خرید #${inv.id}"
                            InvoiceType.RETURN_SALE -> "مرجوعی فروش #${inv.id}"
                            InvoiceType.RETURN_PURCHASE -> "مرجوعی خرید #${inv.id}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isReturn) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = inv.timestamp.toPersianDateString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = inv.totalAmount.toPersianPrice(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (inv.type == InvoiceType.PURCHASE || inv.type == InvoiceType.RETURN_SALE) Color(0xFFE91E63) else Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "تعداد اقلام: ${invoiceWithItems.items.size.toString().toPersianDigits()}",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isReturn) {
                    IconButton(onClick = onRefund) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refund", tint = Color(0xFFE65100))
                    }
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
