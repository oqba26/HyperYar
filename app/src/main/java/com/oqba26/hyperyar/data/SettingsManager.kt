package com.oqba26.hyperyar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val SELECTED_FONT = stringPreferencesKey("selected_font")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")
        val IS_SYNC_ENABLED = booleanPreferencesKey("is_sync_enabled")
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_PHONE = stringPreferencesKey("shop_phone")
        val SHOP_ADDRESS = stringPreferencesKey("shop_address")
        val SHOP_TAX_ID = stringPreferencesKey("shop_tax_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_LOCAL_LOGGED_IN = booleanPreferencesKey("is_local_logged_in")
        val CURRENT_USER_NAME = stringPreferencesKey("current_user_name")
        val USER_ROLE = stringPreferencesKey("user_role") // ADMIN or STAFF
        val LOYALTY_ENABLED = booleanPreferencesKey("loyalty_enabled")
        val LOYALTY_RATE = stringPreferencesKey("loyalty_rate") // Purchase amount for 1 point
        val LOYALTY_VALUE = stringPreferencesKey("loyalty_value") // Discount for 1 point
        val PRINTER_ADDRESS = stringPreferencesKey("printer_address") // BT MAC or USB info
        val PRINTER_TYPE = stringPreferencesKey("printer_type") // BT or USB or SHARE
    }

    val loyaltyEnabled: Flow<Boolean> = context.dataStore.data.map { it[LOYALTY_ENABLED] ?: true }
    val loyaltyRate: Flow<String> = context.dataStore.data.map { it[LOYALTY_RATE] ?: "10000" }
    val loyaltyValue: Flow<String> = context.dataStore.data.map { it[LOYALTY_VALUE] ?: "1000" }
    val printerAddress: Flow<String> = context.dataStore.data.map { it[PRINTER_ADDRESS] ?: "" }
    val printerType: Flow<String> = context.dataStore.data.map { it[PRINTER_TYPE] ?: "SHARE" }

    suspend fun saveLoyaltySettings(enabled: Boolean, rate: String, value: String) {
        context.dataStore.edit { preferences ->
            preferences[LOYALTY_ENABLED] = enabled
            preferences[LOYALTY_RATE] = rate
            preferences[LOYALTY_VALUE] = value
        }
    }

    suspend fun savePrinterSettings(address: String, type: String) {
        context.dataStore.edit { preferences ->
            preferences[PRINTER_ADDRESS] = address
            preferences[PRINTER_TYPE] = type
        }
    }

    val selectedFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_FONT] ?: "Vazirmatn"
    }

    val selectedTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_THEME] ?: "Purple"
    }

    val supabaseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUPABASE_URL] ?: "https://ksbtotwvxaeaacnqwado.supabase.co"
    }

    val supabaseKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUPABASE_KEY] ?: "sb_publishable_qq9uniEFh06EQ7QKedlX2Q_ja9b_XMZ"
    }

    val isSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_SYNC_ENABLED] ?: true
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val isLocalLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOCAL_LOGGED_IN] ?: false
    }

    val currentUserName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USER_NAME] ?: ""
    }

    val shopName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_NAME] ?: "هایپر من"
    }

    val shopPhone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_PHONE] ?: ""
    }

    val shopAddress: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_ADDRESS] ?: ""
    }

    val shopTaxId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_TAX_ID] ?: ""
    }

    val userRole: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE] ?: "ADMIN"
    }

    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_FONT] = fontName
        }
    }

    suspend fun saveTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_THEME] = themeName
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
            if (!loggedIn) {
                preferences[IS_LOCAL_LOGGED_IN] = false
                preferences[CURRENT_USER_NAME] = ""
            }
        }
    }

    suspend fun setLocalLoggedIn(loggedIn: Boolean, userName: String = "") {
        context.dataStore.edit { preferences ->
            preferences[IS_LOCAL_LOGGED_IN] = loggedIn
            preferences[CURRENT_USER_NAME] = userName
        }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    suspend fun saveShopInfo(name: String, phone: String, address: String, taxId: String) {
        context.dataStore.edit { preferences ->
            preferences[SHOP_NAME] = name
            preferences[SHOP_PHONE] = phone
            preferences[SHOP_ADDRESS] = address
            preferences[SHOP_TAX_ID] = taxId
        }
    }

    suspend fun fixOldUrls() {
        context.dataStore.edit { prefs ->
            val currentUrl = prefs[SUPABASE_URL]
            // پاک کردن هرگونه آدرس قبلی که حتی یک حرف اشتباه داشته باشد
            if (currentUrl != null && !currentUrl.contains("ksbtotwvxaeaacnqwado")) {
                prefs.remove(SUPABASE_URL)
                prefs.remove(SUPABASE_KEY)
            }
        }
    }
}
