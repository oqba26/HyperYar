package com.oqba26.hyperyar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.oqba26.hyperyar.data.*
import com.oqba26.hyperyar.ui.components.AppBottomBar
import com.oqba26.hyperyar.ui.components.ConfirmDialog
import com.oqba26.hyperyar.ui.screens.*
import com.oqba26.hyperyar.ui.screens.HistoryScreen
import com.oqba26.hyperyar.ui.theme.*
import com.oqba26.hyperyar.util.SupabaseManager
import com.oqba26.hyperyar.ui.components.CartSheetContent
import com.oqba26.hyperyar.util.InvoicePdfHelper
import com.oqba26.hyperyar.util.PrintHelper
import com.oqba26.hyperyar.util.UpdateManager
import com.oqba26.hyperyar.util.UpdateInfo
import com.oqba26.hyperyar.ui.components.UpdateDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { 
        ProductRepository(
            database.productDao(),
            database.customerDao(),
            database.invoiceDao(),
            database.supplierDao(),
            database.chequeDao(),
            database.expenseDao(),
            database.debtTransactionDao(),
            database.inventoryLogDao(),
            database.userDao(),
            database.wasteLogDao(),
        ) 
    }
    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tempSettings = SettingsManager(this)
        runBlocking {
            val url = tempSettings.supabaseUrl.first()
            val key = tempSettings.supabaseKey.first()
            val isEnabled = tempSettings.isSyncEnabled.first()
            if (isEnabled && url.isNotBlank() && key.isNotBlank()) {
                try {
                    SupabaseManager.init(url, key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val settingsManager = remember { SettingsManager(context) }
            @Suppress("unused", "UnusedVariable") val syncEnabled by settingsManager.isSyncEnabled.collectAsState(initial = true)
            
            val selectedFontName by settingsManager.selectedFont.collectAsState(initial = "Vazirmatn")
            val selectedThemeName by settingsManager.selectedTheme.collectAsState(initial = "Purple")
            
            LaunchedEffect(Unit) {
                settingsManager.fixOldUrls()
            }

            val fontFamily = when (selectedFontName) {
                "Sahel" -> Sahel
                "Estedad" -> Estedad
                "BYekan" -> BYekan
                "IranianSans" -> IranianSans
                else -> Vazirmatn
            }

            HyperYarTheme(
                themeName = selectedThemeName,
                fontFamily = fontFamily,
            ) {
                val updateManager = remember { UpdateManager(context) }
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var downloadProgress by remember { mutableFloatStateOf(0f) }
                var isDownloading by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(2000.milliseconds)
                    updateInfo = updateManager.checkForUpdate()
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val isLoggedIn by settingsManager.isLoggedIn.collectAsState(initial = null)
                    
                    updateInfo?.let { info ->
                        UpdateDialog(
                            updateInfo = info,
                            onDismiss = { updateInfo = null },
                            onConfirm = {
                                val id = updateManager.downloadAndInstall(info.url, "HyperYar_v${info.versionName}.apk")
                                if (id != -1L) {
                                    isDownloading = true
                                    scope.launch {
                                        updateManager.getDownloadProgress(id).collect { progress ->
                                            downloadProgress = progress
                                            if (progress >= 1f) {
                                                isDownloading = false
                                            }
                                        }
                                    }
                                    updateInfo = null
                                }
                            }
                        )
                    }

                    if (isDownloading) {
                        Dialog(onDismissRequest = { }) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                tonalElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "در حال دریافت به‌روزرسانی",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${(downloadProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    when (isLoggedIn) {
                        null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                        false -> {
                            LoginScreen {
                                scope.launch {
                                    settingsManager.setLoggedIn(loggedIn = true)
                                }
                            }
                        }
                        else -> {
                            val isSyncing by viewModel.isSyncing.collectAsState()
                            val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                            val isPurchaseMode by viewModel.isPurchaseMode.collectAsStateWithLifecycle()
                            val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
                            val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
                            val userRole by settingsManager.userRole.collectAsState(initial = "ADMIN")
                            val isAdmin = userRole == "ADMIN"

                            var selectedPersonId by remember { mutableStateOf<Int?>(null) }
                            var currentScreen by remember { mutableStateOf("products") }
                            var showCartSheet by remember { mutableStateOf(value = false) }
                            var showExitDialog by remember { mutableStateOf(value = false) }
                            val sheetState = rememberModalBottomSheetState()
                            val shopName by settingsManager.shopName.collectAsState(initial = "")
                            val displayTitle = remember(shopName) {
                                if (shopName.isBlank() || shopName == "هایپر من") "هایپرمارکت" 
                                else "هایپرمارکت $shopName"
                            }

                            BackHandler {
                                if (currentScreen != "products") {
                                    currentScreen = "products"
                                } else {
                                    showExitDialog = true
                                }
                            }

                            if (showExitDialog) {
                                ConfirmDialog(
                                    title = "خروج از برنامه",
                                    message = "آیا می‌خواهید از اپلیکیشن خارج شوید؟",
                                    confirmText = "تایید",
                                    cancelText = "لغو",
                                    onConfirm = {
                                        (context as? Activity)?.finish()
                                    },
                                    onDismiss = { showExitDialog = false },
                                    isDanger = true
                                )
                            }

                            if (showCartSheet) {
                                ModalBottomSheet(
                                    onDismissRequest = { showCartSheet = false },
                                    sheetState = sheetState
                                ) {
                                    CartSheetContent(
                                        cartItems = cartItems,
                                        isPurchaseMode = isPurchaseMode,
                                        customers = customers,
                                        suppliers = suppliers,
                                        onPersonSelected = { selectedPersonId = it },
                                        onRemove = { viewModel.removeFromCart(it) },
                                        onRedeemPoints = { viewModel.redeemPoints(it) }
                                    ) { paid, discount, custName, custPhone, dueDate, installments ->
                                        val total = cartItems.sumOf { it.totalPrice }
                                        val type = if (isPurchaseMode) InvoiceType.PURCHASE else InvoiceType.SALE
                                        
                                        val invoice = Invoice(
                                            totalAmount = total,
                                            totalDiscount = discount,
                                            type = type,
                                            customerId = if (type == InvoiceType.SALE) selectedPersonId else null,
                                            supplierId = if (type == InvoiceType.PURCHASE) selectedPersonId else null,
                                            amountPaid = paid,
                                            dueDate = dueDate
                                        )
                                        val items = cartItems.map { 
                                            InvoiceItem(
                                                productId = it.product.id,
                                                productName = it.product.name,
                                                quantity = it.quantity,
                                                unit = it.product.unit,
                                                priceAtSale = it.sellPrice,
                                                buyPriceAtSale = it.product.buyPrice,
                                                invoiceId = 0 
                                            )
                                        }
                                        
                                        val invoiceWithItems = InvoiceWithItems(invoice, items)
                                        
                                        if (type == InvoiceType.SALE) {
                                            InvoicePdfHelper.generateAndShareInvoice(
                                                context = context, 
                                                invoiceWithItems = invoiceWithItems, 
                                                shopName = displayTitle,
                                                customerName = custName,
                                                customerPhone = custPhone
                                            )
                                            val receipt = PrintHelper.generateReceiptText(invoiceWithItems, displayTitle, custName)
                                            PrintHelper.sendToPrinter(context, receipt)
                                        }

                                        viewModel.checkout(
                                            customerId = if (type == InvoiceType.SALE) selectedPersonId else null,
                                            supplierId = if (type == InvoiceType.PURCHASE) selectedPersonId else null,
                                            amountPaid = paid,
                                            totalDiscount = discount,
                                            dueDate = dueDate,
                                            installments = installments
                                        )
                                        showCartSheet = false
                                        selectedPersonId = null
                                    }
                                }
                            }
                            
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                bottomBar = {
                                    AppBottomBar(
                                        currentScreen = currentScreen,
                                        cartItemCount = cartItems.size,
                                        isAdmin = isAdmin,
                                        onNavigate = { currentScreen = it },
                                        onShowCart = { showCartSheet = true }
                                    )
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = innerPadding.calculateBottomPadding())
                                ) {
                                    when (currentScreen) {
                                        "products" -> ProductScreen(viewModel = viewModel, isAdmin = isAdmin)
                                        "customers" -> CustomerScreen(viewModel = viewModel)
                                            "accounting" -> AccountingScreen(
                                                viewModel = viewModel,
                                                isAdmin = isAdmin,
                                                onNavigateToSuppliers = { currentScreen = "suppliers" },
                                                onNavigateToCheques = { currentScreen = "cheques" },
                                                onNavigateToExpenses = { currentScreen = "expenses" },
                                                onNavigateToHistory = { currentScreen = "history" }
                                            )
                                        "reports" -> ReportScreen(viewModel = viewModel)
                                        "settings" -> SettingsScreen(
                                            viewModel = viewModel,
                                            settingsManager = settingsManager,
                                            onNavigateBack = { currentScreen = "products" },
                                            onLogout = { 
                                                scope.launch {
                                                    settingsManager.setLoggedIn(loggedIn = false)
                                                }
                                            }
                                        )
                                        "suppliers" -> SupplierScreen(viewModel = viewModel, onNavigateBack = { currentScreen = "accounting" })
                                        "cheques" -> ChequeScreen(viewModel = viewModel, onNavigateBack = { currentScreen = "accounting" })
                                        "expenses" -> ExpenseScreen(viewModel = viewModel, onNavigateBack = { currentScreen = "accounting" })
                                        "history" -> HistoryScreen(viewModel = viewModel, onNavigateBack = { currentScreen = "accounting" })
                                    }
                                    
                                    if (isSyncing) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(androidx.compose.ui.Alignment.TopCenter)
                                                .height(2.dp),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
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
