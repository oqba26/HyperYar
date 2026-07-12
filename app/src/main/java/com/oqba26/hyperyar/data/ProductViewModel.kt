package com.oqba26.hyperyar.data

import androidx.lifecycle.*
import com.oqba26.hyperyar.util.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allProducts.collect {
                _allProducts.value = it
            }
        }
    }

    fun addToCart(product: Product) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(CartItem(product))
        }
        _cartItems.value = currentItems
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.filter { it != cartItem }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        syncWithSupabase()
    }

    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
        syncWithSupabase()
    }

    fun delete(product: Product) = viewModelScope.launch {
        repository.delete(product)
        // Ideally handle remote deletion here
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun syncWithSupabase() = viewModelScope.launch {
        val client = SupabaseManager.getClient() ?: return@launch
        _isSyncing.value = true
        try {
            // Basic push sync: upload all unsynced products
            _allProducts.value.filter { !it.isSynced }.forEach { product ->
                // This is a simplified sync logic
                val remoteProduct = product.copy(isSynced = true)
                client.postgrest["products"].upsert(remoteProduct)
                repository.update(remoteProduct)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
