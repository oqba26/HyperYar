package com.oqba26.hyperyar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class, 
        Customer::class, 
        Invoice::class, 
        InvoiceItem::class,
        Supplier::class,
        Cheque::class,
        Expense::class,
        DebtTransaction::class,
        PendingDeletion::class,
        Bundle::class,
        BundleProductCrossRef::class,
        InventoryLog::class,
        User::class,
        WasteLog::class
    ], 
    version = 3, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun supplierDao(): SupplierDao
    abstract fun chequeDao(): ChequeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun debtTransactionDao(): DebtTransactionDao
    abstract fun bundleDao(): BundleDao
    abstract fun inventoryLogDao(): InventoryLogDao
    abstract fun userDao(): UserDao
    abstract fun wasteLogDao(): WasteLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hyperyar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
