package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.Customer
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.AddCustomerDialog
import com.oqba26.hyperyar.ui.components.EditCustomerDialog
import com.oqba26.hyperyar.ui.components.CustomerDetailDialog
import com.oqba26.hyperyar.ui.components.ConfirmDialog
import androidx.compose.ui.unit.Dp
import com.oqba26.hyperyar.util.toPersianDigits
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: ProductViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateBack: () -> Unit = {}
) {
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var selectedCustomerForDetail by remember { mutableStateOf<Customer?>(null) }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { customer ->
                viewModel.insertCustomer(customer)
                showAddDialog = false
            }
        )
    }

    customerToEdit?.let { customer ->
        EditCustomerDialog(
            customer = customer,
            onDismiss = { customerToEdit = null },
            onConfirm = { updatedCustomer ->
                viewModel.updateCustomer(updatedCustomer)
                customerToEdit = null
            }
        )
    }

    customerToDelete?.let { customer ->
        ConfirmDialog(
            title = "حذف مشتری",
            message = "آیا از حذف مشتری «${customer.name}» اطمینان دارید؟",
            confirmText = "حذف",
            cancelText = "انصراف",
            onConfirm = {
                viewModel.deleteCustomer(customer)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null },
            isDanger = true
        )
    }

    selectedCustomerForDetail?.let { customer ->
        val transactions by viewModel.getTransactionsForPerson(customer.id, "Customer")
            .collectAsStateWithLifecycle(initialValue = emptyList())
        val allInvoices by viewModel.allInvoicesWithItems.collectAsStateWithLifecycle()
            
        CustomerDetailDialog(
            customer = customer,
            transactions = transactions,
            allInvoices = allInvoices,
            onDismiss = { selectedCustomerForDetail = null },
            onAddPayment = { amount, desc ->
                viewModel.addDebtTransaction(customer.id, "Customer", amount, "Payment", desc)
            },
            onPayInstallment = { viewModel.payInstallment(it) },
            onDeleteInstallment = { viewModel.deleteDebtTransaction(it) }
        )
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text("مشتریان", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Surface(
                        onClick = { showAddDialog = true },
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Customer",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "افزودن مشتری",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(bottom = bottomPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("هنوز مشتری ثبت نشده است")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(bottom = bottomPadding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers, key = { it.id }) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedCustomerForDetail = customer },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (customer.phone.isNotBlank()) {
                                    Text(customer.phone.toPersianDigits(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    customer.balance.toPersianPrice(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (customer.balance < 0) Color.Red else Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        viewModel.selectCustomerForCart(customer.id)
                                        onNavigateBack()
                                    }) {
                                        Icon(Icons.Default.AddShoppingCart, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { customerToEdit = customer }) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { customerToDelete = customer }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
