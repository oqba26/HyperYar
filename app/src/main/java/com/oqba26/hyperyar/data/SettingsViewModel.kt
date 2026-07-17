package com.oqba26.hyperyar.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    val shopName: StateFlow<String> = settingsManager.shopName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "هایپر من")

    val shopPhone: StateFlow<String> = settingsManager.shopPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val shopAddress: StateFlow<String> = settingsManager.shopAddress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val shopTaxId: StateFlow<String> = settingsManager.shopTaxId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedFont: StateFlow<String> = settingsManager.selectedFont
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Vazirmatn")

    val selectedTheme: StateFlow<String> = settingsManager.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Purple")

    val isLoggedIn: StateFlow<Boolean?> = settingsManager.isLoggedIn
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSyncEnabled: StateFlow<Boolean> = settingsManager.isSyncEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)

    val isLocalLoggedIn: StateFlow<Boolean> = settingsManager.isLocalLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)

    val currentUserName: StateFlow<String> = settingsManager.currentUserName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = "")

    val userRole: StateFlow<String> = settingsManager.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ADMIN")

    val loyaltyEnabled: StateFlow<Boolean> = settingsManager.loyaltyEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)

    val loyaltyRate: StateFlow<String> = settingsManager.loyaltyRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "10000")

    val loyaltyValue: StateFlow<String> = settingsManager.loyaltyValue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1000")

    val printerAddress: StateFlow<String> = settingsManager.printerAddress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val printerType: StateFlow<String> = settingsManager.printerType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SHARE")

    fun saveShopInfo(name: String, phone: String, address: String, taxId: String) {
        viewModelScope.launch {
            settingsManager.saveShopInfo(name, phone, address, taxId)
        }
    }

    fun saveFont(font: String) {
        viewModelScope.launch {
            settingsManager.saveFont(font)
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            settingsManager.saveTheme(theme)
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setSyncEnabled(enabled)
        }
    }

    fun saveUserRole(role: String) {
        viewModelScope.launch {
            settingsManager.saveUserRole(role)
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        viewModelScope.launch {
            settingsManager.setLoggedIn(loggedIn)
        }
    }

    fun setLocalLoggedIn(loggedIn: Boolean, userName: String = "") {
        viewModelScope.launch {
            settingsManager.setLocalLoggedIn(loggedIn, userName)
        }
    }

    fun saveLoyaltySettings(enabled: Boolean, rate: String, value: String) {
        viewModelScope.launch {
            settingsManager.saveLoyaltySettings(enabled, rate, value)
        }
    }

    fun savePrinterSettings(address: String, type: String) {
        viewModelScope.launch {
            settingsManager.savePrinterSettings(address, type)
        }
    }
}

class SettingsViewModelFactory(private val settingsManager: SettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
