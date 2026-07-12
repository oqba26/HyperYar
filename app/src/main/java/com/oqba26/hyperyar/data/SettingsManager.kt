package com.oqba26.hyperyar.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")
        val IS_SYNC_ENABLED = booleanPreferencesKey("is_sync_enabled")
    }

    val shopName: Flow<String> = context.dataStore.data.map { it[SHOP_NAME] ?: "هایپر من" }
    val supabaseUrl: Flow<String> = context.dataStore.data.map { it[SUPABASE_URL] ?: "https://ksbtotwvxeaaacnqvwado.supabase.co" }
    val supabaseKey: Flow<String> = context.dataStore.data.map { it[SUPABASE_KEY] ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzYnRvdHd2eGFlYWFjbnF3YWRvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4MTAzNTQsImV4cCI6MjA5OTM4NjM1NH0.x_SBDl_P8V0_Nlm-_a--YfOq76vRmjADpNMt5cMN2kM" }
    val isSyncEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_SYNC_ENABLED] ?: true }

    suspend fun saveSupabaseConfig(url: String, key: String) {
        context.dataStore.edit {
            it[SUPABASE_URL] = url
            it[SUPABASE_KEY] = key
        }
    }

    suspend fun setShopName(name: String) {
        context.dataStore.edit { it[SHOP_NAME] = name }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_SYNC_ENABLED] = enabled }
    }
}
