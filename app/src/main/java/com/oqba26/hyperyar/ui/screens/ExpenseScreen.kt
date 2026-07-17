package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.AddExpenseDialog
import com.oqba26.hyperyar.util.toPersianDateString
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ProductViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, category, desc ->
                viewModel.addExpense(title, amount, category, desc)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text("لیست هزینه‌ها", style = MaterialTheme.typography.titleLarge) },
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
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()).padding(bottom = bottomPadding), contentAlignment = Alignment.Center) {
                Text("هنوز هزینه‌ای ثبت نشده است")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()).padding(bottom = bottomPadding).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, style = MaterialTheme.typography.titleMedium)
                                Text(expense.timestamp.toPersianDateString(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text(expense.amount.toPersianPrice(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
