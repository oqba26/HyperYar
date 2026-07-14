package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.SimpleBarChart
import com.oqba26.hyperyar.util.toPersianNumber
import com.oqba26.hyperyar.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ProductViewModel) {
    val totalSales by viewModel.todayTotalSales.collectAsStateWithLifecycle()
    val totalProfit by viewModel.totalProfit.collectAsStateWithLifecycle()
    val monthlyProfit by viewModel.monthlyProfit.collectAsStateWithLifecycle()
    val topProducts by viewModel.topSellingProducts.collectAsStateWithLifecycle()
    val salesByHour by viewModel.salesByHour.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val totalExpenses = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارشات و آمار") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportCard(
                    title = "سود ماهانه",
                    value = monthlyProfit.toPersianPrice(),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                ReportCard(
                    title = "کل هزینه‌ها",
                    value = totalExpenses.toPersianPrice(),
                    icon = Icons.Default.Wallet,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("تحلیل فروش محصولات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("نمودار حجم فروش (۵ کالای برتر)", style = MaterialTheme.typography.labelMedium)
                    SimpleBarChart(data = topProducts)
                }
            }

            Text("تحلیل ساعات شلوغی (امروز)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("نمودار توزیع فروش در ۲۴ ساعت", style = MaterialTheme.typography.labelMedium)
                    SimpleBarChart(data = salesByHour, barColor = Color(0xFF673AB7))
                }
            }

            Text("لیست پرفروش‌ترین‌ها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (topProducts.isEmpty()) {
                        Text("داده‌ای برای نمایش وجود ندارد")
                    } else {
                        topProducts.forEach { (name, qty) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name)
                                Text("${qty.toPersianNumber()} عدد", fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            Text("وضعیت نقدینگی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            // Future chart or breakdown could go here
            Text("در این بخش می‌توانید مجموع ورودی و خروجی صندوق را مدیریت کنید.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
