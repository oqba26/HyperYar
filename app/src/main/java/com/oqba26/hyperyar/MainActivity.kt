package com.oqba26.hyperyar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.oqba26.hyperyar.data.*
import com.oqba26.hyperyar.ui.screens.ProductScreen
import com.oqba26.hyperyar.ui.screens.SettingsScreen
import com.oqba26.hyperyar.ui.theme.HyperYarTheme
import com.oqba26.hyperyar.util.SupabaseManager

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ProductRepository(database.productDao()) }
    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val supabaseUrl by settingsManager.supabaseUrl.collectAsState(initial = "")
            val supabaseKey by settingsManager.supabaseKey.collectAsState(initial = "")
            val syncEnabled by settingsManager.isSyncEnabled.collectAsState(initial = false)

            LaunchedEffect(supabaseUrl, supabaseKey, syncEnabled) {
                if (syncEnabled && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
                    try {
                        SupabaseManager.init(supabaseUrl, supabaseKey)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            HyperYarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val isSyncing by viewModel.isSyncing.collectAsState()
                    var currentScreen by remember { mutableStateOf("products") }
                    
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (currentScreen) {
                                "products" -> ProductScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { currentScreen = "settings" }
                                )
                                "settings" -> SettingsScreen(
                                    onNavigateBack = { currentScreen = "products" }
                                )
                            }
                            
                            if (isSyncing) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(innerPadding)
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
