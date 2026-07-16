package com.oqba26.hyperyar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 4, 
    exportSchema = true
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add columns to products table if they don't exist
                if (!columnExists(db, "products", "unitsInPack")) {
                    db.execSQL("ALTER TABLE products ADD COLUMN unitsInPack REAL NOT NULL DEFAULT 1.0")
                }
                if (!columnExists(db, "products", "isFavorite")) {
                    db.execSQL("ALTER TABLE products ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                }
                if (!columnExists(db, "products", "isSynced")) {
                    db.execSQL("ALTER TABLE products ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                }
                
                // Add columns to invoices table if they don't exist
                if (!columnExists(db, "invoices", "isSynced")) {
                    db.execSQL("ALTER TABLE invoices ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private fun columnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
            val cursor = db.query("PRAGMA table_info($tableName)")
            cursor.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndexOrThrow("name"))
                    if (name == columnName) return true
                }
            }
            return false
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hyperyar_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
