package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.AddChequeDialog
import com.oqba26.hyperyar.util.toPersianDateString
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequeScreen(viewModel: ProductViewModel, onNavigateBack: () -> Unit) {
    val cheques by viewModel.allCheques.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddChequeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { number, bank, amount, dueDate, person, type ->
                viewModel.addCheque(number, bank, amount, dueDate, person, type)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت چک‌ها") },
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
        if (cheques.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("هنوز چکی ثبت نشده است")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cheques) { cheque ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, null, tint = if (cheque.type == "پرداختی") Color.Red else Color(0xFF2E7D32))
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${cheque.bankName} (${cheque.status})", style = MaterialTheme.typography.titleMedium)
                                Text("سررسید: ${cheque.dueDate.toPersianDateString()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("طرف حساب: ${cheque.personName}", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(cheque.amount.toPersianPrice(), style = MaterialTheme.typography.titleMedium)
                                Row {
                                    if (cheque.status == "Pending") {
                                        IconButton(onClick = { viewModel.updateCheque(cheque.copy(status = "Cashed")) }) {
                                            Icon(Icons.Default.CheckCircle, "پاس شد", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { viewModel.updateCheque(cheque.copy(status = "Bounced")) }) {
                                            Icon(Icons.Default.Warning, "برگشت خورد", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteCheque(cheque) }) {
                                        Icon(Icons.Default.Delete, "حذف", tint = Color.Gray, modifier = Modifier.size(20.dp))
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
