package com.oqba26.hyperyar.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.Product
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.ui.components.*
import com.oqba26.hyperyar.util.toPersianPrice
import com.oqba26.hyperyar.util.toPersianDigits
import com.oqba26.hyperyar.util.toPersianNumber
import com.oqba26.hyperyar.util.BackupHelper
import com.oqba26.hyperyar.util.FileHelper
import android.content.Intent
import androidx.core.content.FileProvider
import com.oqba26.hyperyar.data.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    bottomPadding: Dp = 0.dp,
    isAdmin: Boolean = true
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    val isPurchaseMode by viewModel.isPurchaseMode.collectAsStateWithLifecycle()
    
    var selectedCategory by remember { mutableStateOf("همه") }
    
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) || 
                              product.barcode.contains(searchQuery)
            val matchesCategory = selectedCategory == "همه" || product.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }
    
    val categories = remember(products) {
        listOf("همه") + products.map { it.category }.distinct().sorted()
    }

    val todaySales by viewModel.todayTotalSales.collectAsStateWithLifecycle()
    val monthlyProfit by viewModel.monthlyProfit.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockItemsCount.collectAsStateWithLifecycle()
    val dueInstallmentsCount by viewModel.todayDueInstallmentsCount.collectAsStateWithLifecycle()
    val expiringProducts by viewModel.expiringProducts.collectAsStateWithLifecycle()
    
    val lowStockProducts = remember(products) { products.filter { it.stock <= 5 } }
    var showLowStockDialog by remember { mutableStateOf(false) }
    var showExpiringDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val settingsManager = remember { com.oqba26.hyperyar.data.SettingsManager(context) }
    val shopName by settingsManager.shopName.collectAsState(initial = "")
    val displayTitle = remember(shopName) {
        if (shopName.isBlank() || shopName == "هایپر من") "هایپرمارکت" 
        else "هایپرمارکت $shopName"
    }
    
    var showAddDialog by remember { mutableStateOf(value = false) }
    var showScanner by remember { mutableStateOf(value = false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForWaste by remember { mutableStateOf<Product?>(null) }
    var selectedProductForHistory by remember { mutableStateOf<Product?>(null) }
    var showFileMenu by remember { mutableStateOf(value = false) }
    
    val scope = rememberCoroutineScope()

    val csvImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val importedProducts = FileHelper.importProductsFromCsv(context, it)
            if (importedProducts.isNotEmpty()) {
                importedProducts.forEach { product -> viewModel.insert(product) }
                Toast.makeText(context, "${importedProducts.size} کالا وارد شد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.insert(it)
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { 
                viewModel.update(it)
                productToEdit = null
            }
        )
    }

    productForWaste?.let { product ->
        WasteDialog(
            product = product,
            onDismiss = { productForWaste = null },
            onConfirm = { amount, reason ->
                viewModel.registerWaste(product, amount, reason)
                productForWaste = null
            }
        )
    }

    selectedProductForHistory?.let { product ->
        val logs by viewModel.getInventoryLogs(product.id).collectAsStateWithLifecycle(initialValue = emptyList())
        InventoryHistoryDialog(
            product = product,
            logs = logs,
            onDismiss = { selectedProductForHistory = null }
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false },
            onBarcodeScanned = { barcode ->
                viewModel.onSearchQueryChange(barcode)
                showScanner = false
            }
        )
    }

    if (showLowStockDialog) {
        AlertDialog(
            onDismissRequest = { showLowStockDialog = false },
            title = { Text("کالاهای کم موجودی") },
            text = {
                LazyColumn {
                    items(lowStockProducts) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text("موجودی: ${product.stock.toPersianNumber()} ${product.unit}") },
                            trailingContent = { Text(product.sellPrice.toPersianPrice(), color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLowStockDialog = false }) { Text("بستن") } }
        )
    }

    if (showExpiringDialog) {
        AlertDialog(
            onDismissRequest = { showExpiringDialog = false },
            title = { Text("کالاهای نزدیک به انقضا (۷ روز آینده)") },
            text = {
                LazyColumn {
                    items(expiringProducts) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text("تاریخ انقضا: ${product.expiryDate ?: "-"}") },
                            trailingContent = { Text("${product.stock.toPersianNumber()} ${product.unit}", color = Color.Red) }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showExpiringDialog = false }) { Text("بستن") } }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    Box {
                        Surface(
                            onClick = { showFileMenu = true },
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImportExport,
                                    contentDescription = "فایل‌ها",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "فایل‌ها",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showFileMenu,
                            onDismissRequest = { showFileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("خروجی پشتیبان (JSON)") },
                                onClick = {
                                    showFileMenu = false
                                    scope.launch {
                                        val database = AppDatabase.getDatabase(context)
                                        val file = BackupHelper.exportBackup(context, database)
                                        
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "ذخیره فایل پشتیبان"))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ورود از CSV") },
                                onClick = { 
                                    showFileMenu = false
                                    csvImportLauncher.launch("text/comma-separated-values") 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("خروجی CSV") },
                                onClick = {
                                    showFileMenu = false
                                    val file = FileHelper.exportProductsToCsv(context, products)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/comma-separated-values"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "خروجی محصولات"))
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

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
                                contentDescription = "Add Product",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "افزودن کالا",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(8.dp))

                    Spacer(Modifier.width(4.dp))
                },
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
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = bottomPadding)
        ) {
            DashboardSection(
                todaySales = todaySales,
                monthlyProfit = monthlyProfit,
                lowStockCount = lowStockCount,
                dueInstallmentsCount = dueInstallmentsCount,
                expiringCount = expiringProducts.size,
                isPurchaseMode = isPurchaseMode,
                isAdmin = isAdmin,
                onLowStockClick = { showLowStockDialog = true },
                onExpiringClick = { showExpiringDialog = true },
                onTogglePurchaseMode = { viewModel.togglePurchaseMode() }
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onScanClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        showScanner = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )

            CategoryFilterSection(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            val favoriteProducts = products.filter { it.isFavorite }
            if (favoriteProducts.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "همه") {
                Text(
                    text = "دسترسی سریع (برگزیده‌ها)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteProducts, key = { "fav_${it.id}" }) { product ->
                        FavoriteProductChip(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 0.5.dp)
            }

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp), 
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("هنوز کالایی ثبت نشده است", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredProducts, key = { it.id }) { product ->
                    ProductItem(
                        product = product,
                        isPurchaseMode = isPurchaseMode,
                        isAdmin = isAdmin,
                        shopName = shopName,
                        onDelete = { viewModel.delete(product) },
                        onEdit = { productToEdit = product },
                        onWaste = { productForWaste = product },
                        onAddToCart = { viewModel.addToCart(product) },
                        onAddBulkToCart = { viewModel.addToCartBulk(product) },
                        onPrintLabel = { 
                            com.oqba26.hyperyar.util.LabelPdfHelper.generateAndShareLabel(context, product, shopName)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(product) },
                        onClick = { selectedProductForHistory = product }
                    )
                }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun DashboardSection(
    todaySales: Double,
    monthlyProfit: Double,
    lowStockCount: Int,
    dueInstallmentsCount: Int,
    expiringCount: Int,
    isPurchaseMode: Boolean,
    isAdmin: Boolean = true,
    onLowStockClick: () -> Unit,
    onExpiringClick: () -> Unit,
    onTogglePurchaseMode: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                label = if (isPurchaseMode) "حالت خرید فعال" else "فروش امروز",
                value = if (isPurchaseMode) "افزایش انبار" else todaySales.toPersianPrice(),
                icon = if (isPurchaseMode) Icons.Default.AddBusiness else Icons.AutoMirrored.Filled.TrendingUp,
                color = if (isPurchaseMode) Color(0xFFE91E63) else Color(0xFF2E7D32),
                modifier = Modifier.weight(1.2f),
                onClick = onTogglePurchaseMode
            )
            if (isAdmin) {
                DashboardCard(
                    label = "سود ماهانه",
                    value = monthlyProfit.toPersianPrice(),
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                label = "کمبود موجودی",
                value = "${lowStockCount.toString().toPersianDigits()} کالا",
                icon = Icons.Default.Warning,
                color = if (lowStockCount > 0) Color(0xFFE65100) else Color.Gray,
                modifier = Modifier.weight(1f),
                onClick = { if (lowStockCount > 0) onLowStockClick() }
            )
            DashboardCard(
                label = "انقضا نزدیک",
                value = "${expiringCount.toString().toPersianDigits()} کالا",
                icon = Icons.Default.Timer,
                color = if (expiringCount > 0) Color.Red else Color.Gray,
                modifier = Modifier.weight(1f),
                onClick = { if (expiringCount > 0) onExpiringClick() }
            )
            DashboardCard(
                label = "اقساط امروز",
                value = "${dueInstallmentsCount.toString().toPersianDigits()} مورد",
                icon = Icons.Default.NotificationsActive,
                color = if (dueInstallmentsCount > 0) Color.Red else Color.Gray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DashboardCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun FavoriteProductChip(
    product: Product,
    onAddToCart: () -> Unit
) {
    Surface(
        onClick = onAddToCart,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(product.sellPrice.toPersianPrice(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
