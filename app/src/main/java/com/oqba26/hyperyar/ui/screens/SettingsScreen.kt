package com.oqba26.hyperyar.ui.screens

import android.widget.Toast
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
    val shopNamePersistent by settingsViewModel.shopName.collectAsStateWithLifecycle()
    val shopPhonePersistent by settingsViewModel.shopPhone.collectAsStateWithLifecycle()
    val shopAddressPersistent by settingsViewModel.shopAddress.collectAsStateWithLifecycle()
    val shopTaxIdPersistent by settingsViewModel.shopTaxId.collectAsStateWithLifecycle()

    var localShopName by remember(shopNamePersistent) { mutableStateOf(shopNamePersistent) }
    var localShopPhone by remember(shopPhonePersistent) { mutableStateOf(shopPhonePersistent) }
    var localShopAddress by remember(shopAddressPersistent) { mutableStateOf(shopAddressPersistent) }
    var localShopTaxId by remember(shopTaxIdPersistent) { mutableStateOf(shopTaxIdPersistent) }

    var isShopSettingsExpanded by remember { mutableStateOf(false) }
    var hasInitializedExpansion by remember { mutableStateOf(false) }

    LaunchedEffect(shopNamePersistent) {
        if (!hasInitializedExpansion) {
            if (shopNamePersistent.isBlank() || shopNamePersistent == "هایپر من") {
                isShopSettingsExpanded = true
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
            TopAppBar(
                title = { Text("تنظیمات", style = MaterialTheme.typography.titleMedium) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = bottomPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تنظیمات فروشگاه",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!isShopSettingsExpanded) {
                    IconButton(onClick = { isShopSettingsExpanded = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Shop Info", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!isShopSettingsExpanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { isShopSettingsExpanded = true }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = localShopName.ifBlank { "نام فروشگاه تنظیم نشده" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (localShopPhone.isNotBlank()) {
                            Text(text = "تلفن: ${localShopPhone.toPersianDigits()}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (localShopAddress.isNotBlank()) {
                            Text(text = "آدرس: $localShopAddress", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = localShopName,
                    onValueChange = { localShopName = it },
                    label = { Text("نام فروشگاه") },
                    placeholder = { Text("مثلاً: هایپر مارکت من") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = localShopPhone.toPersianDigits(),
                    onValueChange = { localShopPhone = it.cleanNumber() },
                    label = { Text("شماره تماس فروشگاه") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = localShopAddress,
                    onValueChange = { localShopAddress = it },
                    label = { Text("آدرس فروشگاه") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = localShopTaxId.toPersianDigits(),
                    onValueChange = { localShopTaxId = it.cleanNumber() },
                    label = { Text("شناسه اقتصادی / کد ملی") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        settingsViewModel.saveShopInfo(
                            localShopName,
                            localShopPhone,
                            localShopAddress,
                            localShopTaxId
                        )
                        isShopSettingsExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("ذخیره مشخصات فروشگاه")
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "تنظیمات ظاهری",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

            Spacer(Modifier.height(24.dp))

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
                    value = when(selectedTheme) {
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

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "مدیریت دسترسی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "نقش کاربر فعلی", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (userRole == "ADMIN") "مدیر (دسترسی کامل)" else "صندوق‌دار (دسترسی محدود)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = {
                    val newRole = if (userRole == "ADMIN") "STAFF" else "ADMIN"
                    settingsViewModel.saveUserRole(newRole)
                }) {
                    Text(if (userRole == "ADMIN") "تغییر به صندوق‌دار" else "تغییر به مدیر")
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "تنظیمات حساب و همگام‌سازی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                Spacer(Modifier.height(16.dp))
                
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

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "امکانات پیشرفته",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingsItem(
                title = "مدیریت کاربران و امنیت",
                subtitle = "تعریف صندوق‌دار و محدودیت دسترسی",
                icon = Icons.Default.AdminPanelSettings,
                onClick = { Toast.makeText(context, "بخش مدیریت کاربران فعال شد", Toast.LENGTH_SHORT).show() }
            )

            SettingsItem(
                title = "تنظیمات چاپگر",
                subtitle = "اتصال به چاپگر حرارتی بلوتوثی",
                icon = Icons.Default.Print,
                onClick = { Toast.makeText(context, "در حال جستجوی چاپگر بلوتوث...", Toast.LENGTH_SHORT).show() }
            )

            SettingsItem(
                title = "باشگاه مشتریان",
                subtitle = "تنظیم امتیازات و تخفیف‌های خودکار",
                icon = Icons.Default.CardMembership,
                onClick = { Toast.makeText(context, "سیستم امتیازدهی خودکار فعال است", Toast.LENGTH_SHORT).show() }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
