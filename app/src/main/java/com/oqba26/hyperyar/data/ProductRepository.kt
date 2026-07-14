package com.oqba26.hyperyar.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val supplierDao: SupplierDao,
    private val chequeDao: ChequeDao,
    private val expenseDao: ExpenseDao,
    private val debtTransactionDao: DebtTransactionDao,
    private val inventoryLogDao: InventoryLogDao,
    private val userDao: UserDao,
    private val wasteLogDao: WasteLogDao,
) {
    // --- Users ---
    @Suppress("unused")
    fun getAllUsers() = userDao.getAllUsers()
    @Suppress("unused")
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    @Suppress("unused")
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    @Suppress("unused")
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)

    // --- Waste Logs ---
    @Suppress("unused")
    fun getAllWasteLogs() = wasteLogDao.getAllWasteLogs()
    suspend fun insertWasteLog(log: WasteLog) = wasteLogDao.insertWasteLog(log)

    // --- Products ---
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    suspend fun insert(product: Product) = productDao.insertProduct(product)
    suspend fun update(product: Product) = productDao.updateProduct(product)
    suspend fun delete(product: Product) = productDao.deleteProduct(product)

    // --- Customers ---
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // --- Invoices ---
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    val allInvoicesWithItems: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItems()
    
    @Suppress("unused")
    fun getInvoicesForCustomer(customerId: Int) = invoiceDao.getInvoicesForCustomer(customerId)
    @Suppress("unused")
    fun getInvoicesInDateRange(start: Long, end: Long) = invoiceDao.getInvoicesInDateRange(start, end)
    
    suspend fun saveInvoice(
        invoice: Invoice, 
        items: List<InvoiceItem>, 
        installments: List<Pair<Double, Long?>>? = null
    ) {
        val id = invoiceDao.insertInvoice(invoice).toInt()
        items.forEach { item ->
            invoiceDao.insertInvoiceItem(item.copy(invoiceId = id))
            if (invoice.type == InvoiceType.SALE) {
                productDao.reduceStock(item.productId, item.quantity)
                inventoryLogDao.insertLog(
                    InventoryLog(
                        productId = item.productId, 
                        changeAmount = -item.quantity, 
                        type = "Sale", 
                        note = "فاکتور فروش #$id"
                    )
                )
            } else {
                productDao.addStock(item.productId, item.quantity)
                inventoryLogDao.insertLog(
                    InventoryLog(
                        productId = item.productId, 
                        changeAmount = item.quantity, 
                        type = "Purchase", 
                        note = "فاکتور خرید #$id"
                    )
                )
            }
        }
        
        // Handle Customer Balance and Debt Transactions for Credit Sales
        if (invoice.type == InvoiceType.SALE && invoice.customerId != null) {
            val debtAmount = invoice.totalAmount - invoice.amountPaid
            if (debtAmount > 0) {
                val customer = customerDao.getCustomerById(invoice.customerId)
                customer?.let {
                    customerDao.updateCustomer(it.copy(balance = it.balance - debtAmount))
                    
                    if (!installments.isNullOrEmpty()) {
                        installments.forEachIndexed { index, pair ->
                            debtTransactionDao.insertTransaction(
                                DebtTransaction(
                                    personId = it.id,
                                    personType = "Customer",
                                    amount = pair.first,
                                    type = "Debt",
                                    description = "قسط ${index + 1} فاکتور فروش #$id",
                                    dueDate = pair.second,
                                    invoiceId = id
                                )
                            )
                        }
                    } else {
                        debtTransactionDao.insertTransaction(
                            DebtTransaction(
                                personId = it.id,
                                personType = "Customer",
                                amount = debtAmount,
                                type = "Debt",
                                description = "فاکتور فروش #$id",
                                dueDate = invoice.dueDate,
                                invoiceId = id
                            )
                        )
                    }
                }
            }
        }

        // Handle Supplier Balance and Debt Transactions for Credit Purchases
        if (invoice.type == InvoiceType.PURCHASE && invoice.supplierId != null) {
            val debtAmount = invoice.totalAmount - invoice.amountPaid
            if (debtAmount > 0) {
                val supplier = supplierDao.getSupplierById(invoice.supplierId)
                supplier?.let {
                    supplierDao.updateSupplier(it.copy(balance = it.balance + debtAmount))
                    
                    if (!installments.isNullOrEmpty()) {
                        installments.forEachIndexed { index, pair ->
                            debtTransactionDao.insertTransaction(
                                DebtTransaction(
                                    personId = it.id,
                                    personType = "Supplier",
                                    amount = pair.first,
                                    type = "Debt",
                                    description = "قسط ${index + 1} فاکتور خرید #$id",
                                    dueDate = pair.second,
                                    invoiceId = id
                                )
                            )
                        }
                    } else {
                        debtTransactionDao.insertTransaction(
                            DebtTransaction(
                                personId = it.id,
                                personType = "Supplier",
                                amount = debtAmount,
                                type = "Debt",
                                description = "فاکتور خرید #$id",
                                dueDate = invoice.dueDate,
                                invoiceId = id
                            )
                        )
                    }
                }
            }
        }
    }

    // --- Suppliers ---
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun insertSupplier(supplier: Supplier) = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.deleteSupplier(supplier)

    // --- Expenses ---
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)

    // --- Cheques ---
    val allCheques: Flow<List<Cheque>> = chequeDao.getAllCheques()
    suspend fun insertCheque(cheque: Cheque) = chequeDao.insertCheque(cheque)
    suspend fun updateCheque(cheque: Cheque) = chequeDao.updateCheque(cheque)
    suspend fun deleteCheque(cheque: Cheque) = chequeDao.deleteCheque(cheque)

    // --- Debt Transactions ---
    fun getTransactionsForPerson(personId: Int, personType: String) = 
        debtTransactionDao.getTransactionsForPerson(personId, personType)
    
    suspend fun insertDebtTransaction(transaction: DebtTransaction) = 
        debtTransactionDao.insertTransaction(transaction)

    suspend fun deleteDebtTransaction(transaction: DebtTransaction) = 
        debtTransactionDao.deleteTransaction(transaction)

    // --- Inventory Logs ---
    fun getInventoryLogs(productId: Int) = inventoryLogDao.getLogsForProduct(productId)
    @Suppress("unused")
    suspend fun insertInventoryLog(log: InventoryLog) = inventoryLogDao.insertLog(log)
}
