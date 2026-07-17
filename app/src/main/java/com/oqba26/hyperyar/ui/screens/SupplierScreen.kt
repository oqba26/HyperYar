package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.Supplier
import androidx.compose.ui.unit.Dp
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.AddSupplierDialog
import com.oqba26.hyperyar.ui.components.SupplierDetailDialog
import com.oqba26.hyperyar.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(
    viewModel: ProductViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateBack: () -> Unit = {}
) {
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSupplierForDetail by remember { mutableStateOf<Supplier?>(null) }

    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addSupplier(name, phone, address)
                showAddDialog = false
            }
        )
    }

    selectedSupplierForDetail?.let { supplier ->
        val transactions by viewModel.getTransactionsForPerson(supplier.id, "Supplier")
            .collectAsStateWithLifecycle(initialValue = emptyList())
        SupplierDetailDialog(
            supplier = supplier,
            transactions = transactions,
            onDismiss = { selectedSupplierForDetail = null },
            onAddPayment = { amount, desc ->
                viewModel.addDebtTransaction(supplier.id, "Supplier", amount, "Payment", desc)
                // Update supplier balance
                viewModel.updateSupplier(supplier.copy(balance = supplier.balance - amount))
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text("تامین‌کنندگان", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (suppliers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(bottom = bottomPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("هنوز تامین‌کننده‌ای ثبت نشده است")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()).padding(bottom = bottomPadding).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suppliers) { supplier ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedSupplierForDetail = supplier },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(supplier.name, style = MaterialTheme.typography.titleMedium)
                                if (supplier.phone.isNotBlank()) {
                                    Text(supplier.phone.toPersianDigits(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    viewModel.selectSupplierForCart(supplier.id)
                                    onNavigateBack()
                                }) {
                                    Icon(Icons.Default.AddShoppingCart, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { /* Add edit if needed */ }) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
