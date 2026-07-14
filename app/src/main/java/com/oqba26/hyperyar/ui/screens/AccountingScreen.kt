package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.util.toPersianDigits
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: ProductViewModel,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToCheques: () -> Unit,
    onNavigateToExpenses: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val cheques by viewModel.allCheques.collectAsStateWithLifecycle()

    //val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.deepAnalysisResult.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(analysisResult) {
        if (analysisResult != null) showSheet = true
    }

    val totalIncome = remember(invoices) { invoices.sumOf { it.amountPaid } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val balance = totalIncome - totalExpenses

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("حسابداری و مالی", style = MaterialTheme.typography.titleMedium) },
                /*
                actions = {
                    IconButton(
                        onClick = { viewModel.performDeepAnalysis() },
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Analysis", tint = Color.Yellow)
                        }
                    }
                },
                */
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("خلاصه وضعیت مالی", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryItem("درآمد (دریافتی)", totalIncome.toPersianPrice(), Color(0xFF2E7D32))
                        SummaryItem("هزینه‌های جاری", totalExpenses.toPersianPrice(), MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("مانده صندوق (سود):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            balance.toPersianPrice(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (balance >= 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Navigation Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountingNavCard(
                    title = "تامین‌کنندگان",
                    count = suppliers.size,
                    icon = Icons.Default.Store,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onNavigateToSuppliers,
                    modifier = Modifier.weight(1f)
                )
                AccountingNavCard(
                    title = "دفتر چک",
                    count = cheques.size,
                    icon = Icons.Default.Payments,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = onNavigateToCheques,
                    modifier = Modifier.weight(1f)
                )
                AccountingNavCard(
                    title = "هزینه‌ها",
                    count = expenses.size,
                    icon = Icons.Default.Receipt,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            
            // Text("گزارشات و ابزارها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            /*
            OutlinedButton(
                onClick = { viewModel.performDeepAnalysis() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text("تحلیل هوشمند وضعیت فروشگاه")
            }
            */
        }
    }

    if (showSheet && analysisResult != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false
                viewModel.clearAnalysisResult()
            },
            sheetState = sheetState
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "تحلیل هوشمند مدیریت (AI)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = analysisResult ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            showSheet = false
                            viewModel.clearAnalysisResult()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("متوجه شدم")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AccountingNavCard(
    title: String, 
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    color: Color, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${count.toString().toPersianDigits()} مورد",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
