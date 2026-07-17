package com.oqba26.hyperyar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqba26.hyperyar.data.ProductViewModel
import com.oqba26.hyperyar.data.SettingsViewModel
import com.oqba26.hyperyar.ui.components.UserManagementDialog
import com.oqba26.hyperyar.ui.components.PrinterSettingsDialog
import com.oqba26.hyperyar.ui.components.LoyaltySettingsDialog
import com.oqba26.hyperyar.util.SupabaseManager
import com.oqba26.hyperyar.util.cleanNumber
import com.oqba26.hyperyar.util.toPersianDigits
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProductViewModel,
    settingsViewModel: SettingsViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val selectedFont by settingsViewModel.selectedFont.collectAsStateWithLifecycle()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsStateWithLifecycle()
    val isSyncEnabled by settingsViewModel.isSyncEnabled.collectAsStateWithLifecycle()
    val userRole by settingsViewModel.userRole.collectAsStateWithLifecycle()
    val isAdmin = userRole == "ADMIN"
    val shopNamePersistent by settingsViewModel.shopName.collectAsStateWithLifecycle()
    val shopPhonePersistent by settingsViewModel.shopPhone.collectAsStateWithLifecycle()
    val shopAddressPersistent by settingsViewModel.shopAddress.collectAsStateWithLifecycle()
    val shopTaxIdPersistent by settingsViewModel.shopTaxId.collectAsStateWithLifecycle()

    val loyaltyEnabled by settingsViewModel.loyaltyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val loyaltyRate by settingsViewModel.loyaltyRate.collectAsStateWithLifecycle(initialValue = "10000")
    val loyaltyValue by settingsViewModel.loyaltyValue.collectAsStateWithLifecycle(initialValue = "1000")
    val printerAddress by settingsViewModel.printerAddress.collectAsStateWithLifecycle(initialValue = "")
    val printerType by settingsViewModel.printerType.collectAsStateWithLifecycle(initialValue = "SHARE")
    val users by viewModel.allUsers.collectAsStateWithLifecycle()

    var localShopName by remember(shopNamePersistent) { mutableStateOf(shopNamePersistent) }
    var localShopPhone by remember(shopPhonePersistent) { mutableStateOf(shopPhonePersistent) }
    var localShopAddress by remember(shopAddressPersistent) { mutableStateOf(shopAddressPersistent) }
    var localShopTaxId by remember(shopTaxIdPersistent) { mutableStateOf(shopTaxIdPersistent) }

    var isShopExpanded by remember { mutableStateOf(false) }
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    var isAccessExpanded by remember { mutableStateOf(false) }
    var isAccountExpanded by remember { mutableStateOf(false) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var hasInitializedExpansion by remember { mutableStateOf(false) }

    var showUserManagement by remember { mutableStateOf(false) }
    var showPrinterSettings by remember { mutableStateOf(false) }
    var showLoyaltySettings by remember { mutableStateOf(false) }

    LaunchedEffect(shopNamePersistent) {
        if (!hasInitializedExpansion) {
            if (shopNamePersistent.isBlank() || shopNamePersistent == "هایپر من") {
                isShopExpanded = true
            }
            hasInitializedExpansion = true
        }
    }

    val scope = rememberCoroutineScope()
    var fontExpanded by remember { mutableStateOf(value = false) }
    var themeExpanded by remember { mutableStateOf(value = false) }
    
    val fonts = listOf("Vazirmatn", "Sahel", "Estedad", "BYekan", "IranianSans")
    val themes = listOf("Purple", "Blue", "Green")

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("تنظیمات", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = bottomPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 120.dp), // فضا برای دکمه خروج در پایین
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- تنظیمات فروشگاه ---
                ExpandableSettingsSection(
                    title = "تنظیمات فروشگاه",
                    subtitle = shopNamePersistent,
                    icon = Icons.Default.Store,
                    isExpanded = isShopExpanded,
                    onExpandClick = { isShopExpanded = !isShopExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localShopName,
                            onValueChange = { localShopName = it },
                            label = { Text("نام فروشگاه") },
                            placeholder = { Text("مثلاً: هایپر مارکت من") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = localShopPhone.toPersianDigits(),
                            onValueChange = { localShopPhone = it.cleanNumber() },
                            label = { Text("شماره تماس فروشگاه") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = localShopAddress,
                            onValueChange = { localShopAddress = it },
                            label = { Text("آدرس فروشگاه") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            maxLines = 2
                        )

                        OutlinedTextField(
                            value = localShopTaxId.toPersianDigits(),
                            onValueChange = { localShopTaxId = it.cleanNumber() },
                            label = { Text("شناسه اقتصادی / کد ملی") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Button(
                            onClick = {
                                settingsViewModel.saveShopInfo(
                                    localShopName,
                                    localShopPhone,
                                    localShopAddress,
                                    localShopTaxId
                                )
                                isShopExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("ذخیره مشخصات فروشگاه")
                        }
                    }
                }

                // --- تنظیمات ظاهری ---
                ExpandableSettingsSection(
                    title = "تنظیمات ظاهری",
                    subtitle = "فونت: $selectedFont",
                    icon = Icons.Default.Palette,
                    isExpanded = isAppearanceExpanded,
                    onExpandClick = { isAppearanceExpanded = !isAppearanceExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = "انتخاب فونت برنامه:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = fontExpanded,
                                onExpandedChange = { fontExpanded = !fontExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedFont,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                ExposedDropdownMenu(
                                    expanded = fontExpanded,
                                    onDismissRequest = { fontExpanded = false }
                                ) {
                                    fonts.forEach { font ->
                                        DropdownMenuItem(
                                            text = { Text(text = font) },
                                            onClick = {
                                                settingsViewModel.saveFont(font)
                                                fontExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "انتخاب تم رنگی:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = themeExpanded,
                                onExpandedChange = { themeExpanded = !themeExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = when (selectedTheme) {
                                        "Blue" -> "آبی کلاسیک"
                                        "Green" -> "سبز جنگلی"
                                        else -> "بنفش پیش‌فرض"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                ExposedDropdownMenu(
                                    expanded = themeExpanded,
                                    onDismissRequest = { themeExpanded = false }
                                ) {
                                    themes.forEach { theme ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = when (theme) {
                                                        "Blue" -> "آبی کلاسیک"
                                                        "Green" -> "سبز جنگلی"
                                                        else -> "بنفش پیش‌فرض"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                settingsViewModel.saveTheme(theme)
                                                themeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- مدیریت دسترسی ---
                val currentUserName by settingsViewModel.currentUserName.collectAsState()
                
                ExpandableSettingsSection(
                    title = "مدیریت دسترسی",
                    subtitle = if (userRole == "ADMIN") "مدیر (دسترسی کامل)" else "صندوق‌دار (دسترسی محدود)",
                    icon = Icons.Default.Security,
                    isExpanded = isAccessExpanded,
                    onExpandClick = { isAccessExpanded = !isAccessExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "کاربر فعلی: $currentUserName",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (userRole == "ADMIN") "مدیر (دسترسی کامل)" else "صندوق‌دار (دسترسی محدود)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Button(
                            onClick = {
                                settingsViewModel.setLocalLoggedIn(false)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("تعویض کاربر / خروج موقت")
                        }
                    }
                }

                // --- تنظیمات حساب و همگام‌سازی ---
                ExpandableSettingsSection(
                    title = "تنظیمات حساب و همگام‌سازی",
                    subtitle = if (isSyncEnabled) "همگام‌سازی فعال" else "همگام‌سازی غیرفعال",
                    icon = Icons.Default.CloudSync,
                    isExpanded = isAccountExpanded,
                    onExpandClick = { isAccountExpanded = !isAccountExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "همگام‌سازی خودکار", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "اطلاعات به طور خودکار در فضای ابری ذخیره می‌شوند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Switch(
                                checked = isSyncEnabled,
                                onCheckedChange = {
                                    settingsViewModel.setSyncEnabled(it)
                                }
                            )
                        }

                        if (isSyncEnabled) {
                            var isSyncing by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = {
                                    isSyncing = true
                                    viewModel.syncWithSupabase {
                                        isSyncing = false
                                        Toast.makeText(context, "اطلاعات با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("در حال دریافت...")
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("همین حالا همگام‌سازی کن")
                                }
                            }
                        }
                    }
                }

                // --- امکانات پیشرفته ---
                ExpandableSettingsSection(
                    title = "امکانات پیشرفته و ابزارها",
                    subtitle = "مدیریت کاربران، چاپگر و باشگاه مشتریان",
                    icon = Icons.Default.AutoAwesome,
                    isExpanded = isAdvancedExpanded,
                    onExpandClick = { isAdvancedExpanded = !isAdvancedExpanded },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isAdmin) {
                            SettingsItem(
                                title = "مدیریت کاربران و امنیت",
                                subtitle = "تعریف صندوق‌دار و محدودیت دسترسی",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { showUserManagement = true }
                            )
                        }

                        SettingsItem(
                            title = "تنظیمات چاپگر",
                            subtitle = "اتصال به چاپگر حرارتی (Bluetooth/USB)",
                            icon = Icons.Default.Print,
                            onClick = { showPrinterSettings = true }
                        )

                        if (isAdmin) {
                            SettingsItem(
                                title = "باشگاه مشتریان",
                                subtitle = "تنظیم امتیازات و تخفیف‌های خودکار",
                                icon = Icons.Default.CardMembership,
                                onClick = { showLoyaltySettings = true }
                            )
                        }
                    }
                }
            }

            // دکمه خروج ثابت در پایین صفحه
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            SupabaseManager.getClient()?.auth?.signOut()
                            onLogout()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("خروج از حساب کاربری")
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "برای تغییر فروشگاه، ابتدا خارج شوید.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showUserManagement) {
            UserManagementDialog(
                users = users,
                onDismiss = { showUserManagement = false },
                onAddUser = { user, pass, role ->
                    viewModel.insertUser(com.oqba26.hyperyar.data.User(username = user, passwordHash = pass, role = role))
                },
                onUpdateUser = { user ->
                    viewModel.insertUser(user)
                },
                onDeleteUser = { viewModel.deleteUser(it) }
            )
        }

        if (showPrinterSettings) {
            PrinterSettingsDialog(
                currentType = printerType,
                currentAddress = printerAddress,
                onDismiss = { showPrinterSettings = false },
                onSave = { type, address ->
                    settingsViewModel.savePrinterSettings(address, type)
                    showPrinterSettings = false
                }
            )
        }

        if (showLoyaltySettings) {
            LoyaltySettingsDialog(
                enabled = loyaltyEnabled,
                rate = loyaltyRate,
                value = loyaltyValue,
                onDismiss = { showLoyaltySettings = false },
                onSave = { enabled, rate, value ->
                    settingsViewModel.saveLoyaltySettings(enabled, rate, value)
                    showLoyaltySettings = false
                }
            )
        }
    }
}

@Composable
fun ExpandableSettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium,
        onClick = onExpandClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isExpanded && subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Color.LightGray)
        }
    }
}
