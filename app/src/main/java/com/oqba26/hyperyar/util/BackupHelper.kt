package com.oqba26.hyperyar.util

import android.content.Context
import com.oqba26.hyperyar.data.AppDatabase
import com.oqba26.hyperyar.data.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object BackupHelper {
    suspend fun exportBackup(context: Context, database: AppDatabase): File {
        val products = database.productDao().getAllProductsList()
        val json = Json { prettyPrint = true }
        val content = json.encodeToString<List<Product>>(products)
        
        val file = File(context.getExternalFilesDir(null), "hyperyar_backup_${System.currentTimeMillis()}.json")
        file.writeText(content)
        return file
    }
}
