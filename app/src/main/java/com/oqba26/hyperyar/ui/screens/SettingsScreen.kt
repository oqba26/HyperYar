package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.oqba26.hyperyar.data.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        url = settingsManager.supabaseUrl.first()
        key = settingsManager.supabaseKey.first()
        shopName = settingsManager.shopName.first()
        isEnabled = settingsManager.isSyncEnabled.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات برنامه") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "تنظیمات ظاهری:",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("نام فروشگاه (هایپر مارکت...)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text(
                "مشخصات سینک آنلاین (Supabase):",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Project URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://xxx.supabase.co") }
            )

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("فعال‌سازی همگام‌سازی")
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        settingsManager.setShopName(shopName)
                        settingsManager.saveSupabaseConfig(url, key)
                        settingsManager.setSyncEnabled(isEnabled)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudSync, null)
                Spacer(Modifier.width(8.dp))
                Text("ذخیره تنظیمات")
            }
        }
    }
}
