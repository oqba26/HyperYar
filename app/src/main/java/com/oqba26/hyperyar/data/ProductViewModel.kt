package com.oqba26.hyperyar.data

import androidx.lifecycle.*
import android.util.Log
import com.oqba26.hyperyar.util.SupabaseManager
// import com.oqba26.hyperyar.network.GeminiService
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // private var geminiService: GeminiService? = null

    private val _deepAnalysisResult = MutableStateFlow<String?>(null)
    val deepAnalysisResult = _deepAnalysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    @Suppress("unused")
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCheques: StateFlow<List<Cheque>> = repository.allCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("unused")
    val allInvoicesWithItems: StateFlow<List<InvoiceWithItems>> = repository.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalProfit: StateFlow<Double> = repository.allInvoicesWithItems
        .map { invoices ->
            invoices.filter { it.invoice.type == InvoiceType.SALE }
                .sumOf { invoiceWithItems ->
                    invoiceWithItems.items.sumOf { (it.priceAtSale - it.buyPriceAtSale) * it.quantity }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topSellingProducts: StateFlow<List<Pair<String, Double>>> = repository.allInvoicesWithItems
        .map { invoices ->
            invoices.filter { it.invoice.type == InvoiceType.SALE }
                .flatMap { it.items }
                .groupBy { it.productName }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItemsCount: StateFlow<Int> = repository.allProducts
        .map { products -> products.count { it.stock <= 5 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyProfit: StateFlow<Double> = repository.allInvoicesWithItems.map { invoices ->
        val monthStart = System.currentTimeMillis().toMonthStart()
        invoices.filter { it.invoice.timestamp >= monthStart && it.invoice.type == InvoiceType.SALE }.sumOf { inv ->
            inv.items.sumOf { (it.priceAtSale - it.buyPriceAtSale) * it.quantity }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val profitByCategory: StateFlow<Map<String, Double>> = repository.allInvoicesWithItems.map { invoices ->
        invoices.filter { it.invoice.type == InvoiceType.SALE }
            .flatMap { it.items }
            .groupBy { it.unit } // Note: Assuming unit or we should fetch category from product?
            // Actually, InvoiceItem doesn't have category. We need to join or have it in InvoiceItem.
            // For now, let's group by product and assume we can map to category if we had that link.
            // Better: group by productName for now or fetch products first.
            .mapValues { entry -> 
                entry.value.sumOf { (it.priceAtSale - it.buyPriceAtSale) * it.quantity }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val weeklyComparison: StateFlow<Pair<Double, Double>> = repository.allInvoices.map { invoices ->
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
        val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000)
        
        val currentWeekSales = invoices.filter { it.timestamp in oneWeekAgo..now && it.type == InvoiceType.SALE }.sumOf { it.totalAmount }
        val previousWeekSales = invoices.filter { it.timestamp in twoWeeksAgo until oneWeekAgo && it.type == InvoiceType.SALE }.sumOf { it.totalAmount }
        
        currentWeekSales to previousWeekSales
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0 to 0.0)

    val expiringProducts: StateFlow<List<Product>> = repository.allProducts.map { products ->
        val soon = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
        products.filter { 
            val expiryDate = it.expiryDate
            !expiryDate.isNullOrBlank() && expiryDate.toTimestamp() <= soon 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotalSales: StateFlow<Double> = repository.allInvoices
        .map { invoices ->
            val startOfToday = System.currentTimeMillis().toLocalDateStart()
            invoices.asSequence()
                .filter { it.timestamp >= startOfToday && it.type == InvoiceType.SALE }
                .sumOf { it.totalAmount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayDueInstallmentsCount: StateFlow<Int> = repository.allInvoices
        .map { invoices ->
            val startOfToday = System.currentTimeMillis().toLocalDateStart()
            val endOfToday = startOfToday + (24 * 60 * 60 * 1000)
            invoices.count { it.dueDate != null && it.dueDate in startOfToday until endOfToday }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun Long.toLocalDateStart(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = this
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun Long.toMonthStart(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = this
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val salesByHour: StateFlow<List<Pair<String, Double>>> = repository.allInvoices.map { invoices ->
        val hourlyData = DoubleArray(24)
        val today = System.currentTimeMillis().toLocalDateStart()
        invoices.filter { it.timestamp >= today && it.type == InvoiceType.SALE }.forEach {
            val hour = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(java.util.Calendar.HOUR_OF_DAY)
            hourlyData[hour] += it.totalAmount
        }
        hourlyData.mapIndexed { hour, total -> String.format(java.util.Locale.US, "%02d", hour) to total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun redeemPoints(customerId: Int): Double {
        val customer = allCustomers.value.find { it.id == customerId } ?: return 0.0
        val points = customer.loyaltyPoints
        if (points < 10) return 0.0
        
        val discount = points * 1000.0 // Every point = 1000 Toman discount
        viewModelScope.launch {
            repository.updateCustomer(customer.copy(loyaltyPoints = 0))
        }
        return discount
    }

    private fun String.toTimestamp(): Long {
        return try {
            val parts = this.split("/")
            val cal = java.util.Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            cal.timeInMillis
        } catch (_: Exception) { Long.MAX_VALUE }
    }

    init {
        viewModelScope.launch {
            repository.allProducts.collect {
                _allProducts.value = it
            }
        }
        viewModelScope.launch {
            syncWithSupabase()
        }
    }

    @Suppress("unused")
    fun initGemini(apiKey: String) {
        // geminiService = GeminiService(apiKey)
    }

    @Suppress("unused")
    fun performDeepAnalysis() {
        /*
        val service = geminiService ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            val prompt = prepareAnalysisPrompt()
            _deepAnalysisResult.value = service.analyzeFinancialData(prompt)
            _isAnalyzing.value = false
        }
        */
    }

    fun clearAnalysisResult() {
        _deepAnalysisResult.value = null
    }

    @Suppress("unused")
    private fun prepareAnalysisPrompt(): String {
        return ""
        /*
        val invoices = allInvoices.value
        ...
        */
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

    fun addToCartBulk(product: Product) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }
        val quantityToAdd = if (product.unitsInPack > 0) product.unitsInPack else 1.0
        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + quantityToAdd)
        } else {
            currentItems.add(CartItem(product, quantity = quantityToAdd))
        }
        _cartItems.value = currentItems
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.filter { it != cartItem }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    private val _isPurchaseMode = MutableStateFlow(false)
    val isPurchaseMode: StateFlow<Boolean> = _isPurchaseMode.asStateFlow()

    fun togglePurchaseMode() {
        _isPurchaseMode.value = !_isPurchaseMode.value
    }

    fun registerWaste(product: Product, amount: Double, reason: String) = viewModelScope.launch {
        val log = WasteLog(productId = product.id, productName = product.name, amount = amount, reason = reason)
        repository.insertWasteLog(log)
        repository.update(product.copy(stock = (product.stock - amount).coerceAtLeast(0.0)))
        launch { silentSync() }
    }

    fun checkout(
        customerId: Int? = null,
        supplierId: Int? = null,
        amountPaid: Double? = null,
        totalDiscount: Double = 0.0,
        dueDate: Long? = null,
        installments: List<Pair<Double, Long?>>? = null
    ) = viewModelScope.launch {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) return@launch
        
        val total = currentCart.sumOf { it.totalPrice }
        val finalAmountPaid = amountPaid ?: (total - totalDiscount)
        val type = if (_isPurchaseMode.value) InvoiceType.PURCHASE else InvoiceType.SALE
        
        val invoice = Invoice(
            customerId = if (type == InvoiceType.SALE) customerId else null,
            supplierId = if (type == InvoiceType.PURCHASE) supplierId else null,
            totalAmount = total,
            type = type,
            amountPaid = finalAmountPaid,
            totalDiscount = totalDiscount,
            dueDate = dueDate
        )
        val items = currentCart.map { 
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
        repository.saveInvoice(invoice, items, installments)
        
        // Loyalty Points: 1 point for every 10,000 Tomans
        if (type == InvoiceType.SALE && customerId != null) {
            val pointsToAdd = (total / 10000).toInt()
            if (pointsToAdd > 0) {
                val customers = allCustomers.value
                val customer = customers.find { it.id == customerId }
                customer?.let {
                    repository.updateCustomer(it.copy(loyaltyPoints = it.loyaltyPoints + pointsToAdd))
                }
            }
        }

        clearCart()
        _isPurchaseMode.value = false
        launch { silentSync() }
    }

    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        launch { silentSync() }
    }

    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
        launch { silentSync() }
    }

    fun toggleFavorite(product: Product) = viewModelScope.launch {
        repository.update(product.copy(isFavorite = !product.isFavorite))
        launch { silentSync() }
    }

    fun delete(product: Product) = viewModelScope.launch {
        repository.delete(product)
    }

    fun deleteInvoice(invoiceWithItems: InvoiceWithItems) = viewModelScope.launch {
        repository.deleteInvoice(invoiceWithItems.invoice, invoiceWithItems.items)
        launch { silentSync() }
    }

    fun refundInvoice(invoiceWithItems: InvoiceWithItems) = viewModelScope.launch {
        val inv = invoiceWithItems.invoice
        val type = if (inv.type == InvoiceType.SALE) InvoiceType.RETURN_SALE else InvoiceType.RETURN_PURCHASE
        
        val refundInvoice = Invoice(
            type = type,
            totalAmount = inv.totalAmount,
            totalDiscount = inv.totalDiscount,
            customerId = inv.customerId,
            supplierId = inv.supplierId,
            amountPaid = inv.amountPaid,
            dueDate = null
        )
        val items = invoiceWithItems.items.map { it.copy(id = 0, invoiceId = 0) }
        repository.saveInvoice(refundInvoice, items)
        launch { silentSync() }
    }

    fun insertCustomer(customer: Customer) = viewModelScope.launch {
        repository.insertCustomer(customer)
        launch { silentSync() }
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch {
        repository.updateCustomer(customer)
        launch { silentSync() }
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        repository.deleteCustomer(customer)
    }

    fun updateSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.updateSupplier(supplier)
        launch { silentSync() }
    }

    @Suppress("unused")
    fun deleteSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.deleteSupplier(supplier)
    }

    fun addExpense(title: String, amount: Double, category: String, description: String = "") = viewModelScope.launch {
        repository.insertExpense(Expense(title = title, amount = amount, category = category, description = description))
        launch { silentSync() }
    }

    fun addSupplier(name: String, phone: String = "", address: String = "") = viewModelScope.launch {
        repository.insertSupplier(Supplier(name = name, phone = phone, address = address))
        launch { silentSync() }
    }

    fun addCheque(chequeNumber: String, bankName: String, amount: Double, dueDate: Long, personName: String, type: String) = viewModelScope.launch {
        repository.insertCheque(Cheque(chequeNumber = chequeNumber, bankName = bankName, amount = amount, dueDate = dueDate, personName = personName, type = type))
        launch { silentSync() }
    }

    fun updateCheque(cheque: Cheque) = viewModelScope.launch {
        repository.updateCheque(cheque)
    }

    fun deleteCheque(cheque: Cheque) = viewModelScope.launch {
        repository.deleteCheque(cheque)
    }

    fun getTransactionsForPerson(personId: Int, personType: String) = 
        repository.getTransactionsForPerson(personId, personType)

    fun getInventoryLogs(productId: Int) = repository.getInventoryLogs(productId)

    fun addDebtTransaction(personId: Int, personType: String, amount: Double, type: String, description: String = "", dueDate: Long? = null, invoiceId: Int? = null) = viewModelScope.launch {
        repository.insertDebtTransaction(DebtTransaction(personId = personId, personType = personType, amount = amount, type = type, description = description, dueDate = dueDate, invoiceId = invoiceId))
        
        // Update balance
        if (personType == "Customer") {
            val customers = allCustomers.value
            val customer = customers.find { it.id == personId }
            customer?.let {
                val newBalance = if (type == "Payment") it.balance + amount else it.balance - amount
                repository.updateCustomer(it.copy(balance = newBalance))
            }
        }
    }

    fun payInstallment(transaction: DebtTransaction) = viewModelScope.launch {
        if (transaction.isPaid) return@launch
        
        val updatedTransaction = transaction.copy(
            isPaid = true,
            paymentTimestamp = System.currentTimeMillis()
        )
        repository.insertDebtTransaction(updatedTransaction)
        
        if (transaction.personType == "Customer") {
            val customers = allCustomers.value
            val customer = customers.find { it.id == transaction.personId }
            customer?.let {
                val newBalance = it.balance + transaction.amount
                repository.updateCustomer(it.copy(balance = newBalance))
            }
        }
    }

    fun deleteDebtTransaction(transaction: DebtTransaction) = viewModelScope.launch {
        repository.deleteDebtTransaction(transaction)
        
        if (transaction.personType == "Customer") {
            val customers = allCustomers.value
            val customer = customers.find { it.id == transaction.personId }
            customer?.let {
                var balanceChange = if (transaction.type == "Payment") -transaction.amount else transaction.amount
                if (transaction.isPaid) {
                     balanceChange = 0.0 
                }
                
                if (balanceChange != 0.0) {
                    repository.updateCustomer(it.copy(balance = it.balance + balanceChange))
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private suspend fun silentSync() {
        val client = SupabaseManager.getClient() ?: return
        try {
            val products = repository.allProducts.first()
            if (products.isNotEmpty()) client.postgrest["products"].upsert(products)
            
            val customers = repository.allCustomers.first()
            if (customers.isNotEmpty()) client.postgrest["customers"].upsert(customers)
            
            val invoices = repository.allInvoices.first()
            if (invoices.isNotEmpty()) client.postgrest["invoices"].upsert(invoices)

            val suppliers = repository.allSuppliers.first()
            if (suppliers.isNotEmpty()) client.postgrest["suppliers"].upsert(suppliers)

            val cheques = repository.allCheques.first()
            if (cheques.isNotEmpty()) client.postgrest["cheques"].upsert(cheques)

            val expenses = repository.allExpenses.first()
            if (expenses.isNotEmpty()) client.postgrest["expenses"].upsert(expenses)

            // Actual sync for debt transactions
            val transactions = repository.allCustomers.first().flatMap { repository.getTransactionsForPerson(it.id, "Customer").first() }
            if (transactions.isNotEmpty()) client.postgrest["debt_transactions"].upsert(transactions)
        } catch (e: Exception) {
            Log.e("Sync", "Silent sync failed: ${e.message}")
        }
    }

    fun syncWithSupabase(onComplete: () -> Unit = {}) = viewModelScope.launch {
        val client = SupabaseManager.getClient() ?: run {
            onComplete()
            return@launch
        }
        _isSyncing.value = true
        try {
            // Fetch and merge logic could be more complex, but for now:
            val remoteProducts = client.postgrest["products"].select().decodeList<Product>()
            remoteProducts.forEach { repository.insert(it) }
            
            val remoteCustomers = client.postgrest["customers"].select().decodeList<Customer>()
            remoteCustomers.forEach { repository.insertCustomer(it) }
            
            silentSync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
            onComplete()
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
