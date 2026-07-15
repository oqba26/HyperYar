package com.oqba26.hyperyar

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import android.provider.Settings
import android.content.Intent
import android.os.Environment
import java.io.File
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import com.oqba26.hyperyar.ui.components.CartSheetContent
import com.oqba26.hyperyar.util.InvoicePdfHelper
import com.oqba26.hyperyar.util.PrintHelper
import com.oqba26.hyperyar.util.UpdateManager
import com.oqba26.hyperyar.util.UpdateInfo
import com.oqba26.hyperyar.ui.components.UpdateDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
        
        cleanupInstaller()

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
            val lifecycleOwner = LocalLifecycleOwner.current

            var showCleanupDialog by remember { mutableStateOf(value = false) }
            var showForcedExitDialog by remember { mutableStateOf(value = false) }
            var isCleanupChecked by remember { mutableStateOf(value = false) }
            var showSessionExpiredDialog by remember { mutableStateOf(false) }
            var showPermissionsDialog by remember { mutableStateOf(false) }

            val requiredPermissions = mutableListOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted) {
                    showPermissionsDialog = false
                }
            }

            val settingsManager = remember { SettingsManager(context) }
            @Suppress("unused", "UnusedVariable") val syncEnabled by settingsManager.isSyncEnabled.collectAsState(initial = true)
            
            val selectedFontName by settingsManager.selectedFont.collectAsState(initial = "Vazirmatn")
            val selectedThemeName by settingsManager.selectedTheme.collectAsState(initial = "Purple")
            
            LaunchedEffect(Unit) {
                settingsManager.fixOldUrls()
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (isCleanupChecked) {
                            cleanupInstaller()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
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
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                    }

                    if (showCleanupDialog) {
                        Dialog(
                            onDismissRequest = {
                                showCleanupDialog = false
                                showForcedExitDialog = true
                            },
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            text = "دسترسی مدیریت فایل‌ها",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "برای انجام عملیات پشتیبان‌گیری، بازگردانی اطلاعات و مدیریت فایل‌های نصب، برنامه به دسترسی «مدیریت تمامی فایل‌ها» نیاز دارد.\n\nبدون این دسترسی، امکان ذخیره اطلاعات شما وجود نخواهد داشت. لطفاً در صفحه بعد این دسترسی را فعال کنید.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    showCleanupDialog = false
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                                        intent.data = "package:${context.packageName}".toUri()
                                                        context.startActivity(intent)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("تنظیمات", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                            }
                                            Button(
                                                onClick = {
                                                    showCleanupDialog = false
                                                    showForcedExitDialog = true
                                                },
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Text("خروج", style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showForcedExitDialog) {
                        Dialog(onDismissRequest = { (context as? Activity)?.finish() }) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            text = "عدم دسترسی",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        @Suppress("SpellCheckingInspection")
                                        Text(
                                            text = "متاسفانه شما دسترسی لازم رو به برنامه نداده اید و برنامه بسته خواهد شد.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { (context as? Activity)?.finish() },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.small,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("تایید و خروج", color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
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
                                    viewModel.syncWithSupabase()
                                }
                            }
                        }
                        else -> {
                            LaunchedEffect(Unit) {
                                val client = SupabaseManager.getClient()
                                val syncEnabledVal = settingsManager.isSyncEnabled.first()
                                if (client != null && syncEnabledVal) {
                                    client.auth.sessionStatus.collect { status ->
                                        if (status is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated) {
                                            showSessionExpiredDialog = true
                                        }
                                    }
                                }
                            }

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

                            var hasCheckedPermissionsThisSession by remember { mutableStateOf(value = false) }

                            LaunchedEffect(currentScreen, showSessionExpiredDialog) {
                                val isMainAppScreen = currentScreen != "login"

                                if (isMainAppScreen && !showSessionExpiredDialog && !hasCheckedPermissionsThisSession) {
                                    val allGranted = requiredPermissions.all {
                                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                                    }
                                    if (!allGranted) {
                                        showPermissionsDialog = true
                                    }
                                    hasCheckedPermissionsThisSession = true
                                }

                                if ((currentScreen == "products") && !isCleanupChecked && !showSessionExpiredDialog) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        if (!Environment.isExternalStorageManager()) {
                                            showCleanupDialog = true
                                        } else {
                                            cleanupInstaller()
                                        }
                                    } else {
                                        cleanupInstaller()
                                    }
                                    isCleanupChecked = true
                                }
                            }

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

                            if (showPermissionsDialog) {
                                Dialog(onDismissRequest = { }) {
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                        Surface(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            tonalElevation = 6.dp,
                                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                                        ) {
                                            @Suppress("SpellCheckingInspection")
                                            Column(modifier = Modifier.padding(24.dp)) {
                                                Text(
                                                    text = "دسترسی‌های الزامی",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    modifier = Modifier.padding(bottom = 16.dp),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = "کاربر گرامی، برای استفاده از برنامه باید دسترسی‌های زیر را تایید کنید:\n\n" +
                                                            "۱. مخاطبین: برای پیدا کردن خودکار شماره مشتری\n" +
                                                            "۲. دوربین: برای اسکن بارکد کالاها\n" +
                                                            "۳. حافظه: برای ذخیره فاکتورها و فایل‌های پشتیبان\n\n" +
                                                            "توجه: در صورت عدم تایید، برنامه به هیچ عنوان کار نخواهد کرد و بسته خواهد شد.",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(24.dp))
                                                Button(
                                                    onClick = {
                                                        permissionLauncher.launch(requiredPermissions)
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = MaterialTheme.shapes.small,
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Text("متوجه شدم؛ اعطای دسترسی", color = MaterialTheme.colorScheme.onPrimary)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                @Suppress("SpellCheckingInspection")
                                                TextButton(
                                                    onClick = { (context as? Activity)?.finish() },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("خروج", color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showSessionExpiredDialog) {
                                Dialog(onDismissRequest = { }) {
                                    @Suppress("SpellCheckingInspection")
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                        Surface(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            tonalElevation = 6.dp,
                                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(24.dp)) {
                                                Text(
                                                    text = "کاربر گرامی",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    modifier = Modifier.padding(bottom = 16.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "زمان ورود قبلی شما به برنامه منقضی شده و باید مجدد وارد شوید. این کار فقط برای حفظ امنیت اطلاعات خودتان ضروری است.\n\nالان به صفحه ورود منتقل می‌شوید؛ لطفاً نام کاربری و کلمه عبوری که قبلاً با آن در برنامه ثبت‌نام کرده و وارد شده‌اید را مجدداً وارد نمایید. با تشکر",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(24.dp))
                                                Button(
                                                    onClick = {
                                                        showSessionExpiredDialog = false
                                                        scope.launch {
                                                            settingsManager.setLoggedIn(false)
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = MaterialTheme.shapes.small,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    )
                                                ) {
                                                    Text("متوجه شدم؛ ورود مجدد", color = MaterialTheme.colorScheme.onPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
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

    private fun cleanupInstaller() {
        try {
            val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            deleteApksInDir(publicDownloadDir)
            val privateDownloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            deleteApksInDir(privateDownloadDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteApksInDir(directory: File?) {
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.isFile && file.extension.equals("apk", ignoreCase = true)) {
                    val name = file.name.lowercase()
                    if (name.contains("hyper") || name.contains("yar") || name.contains("app-debug") || name.contains("app-release")) {
                        if (file.delete()) {
                            android.util.Log.d("Cleanup", "Deleted installer: ${file.name}")
                        }
                    }
                }
            }
        }
    }
}
