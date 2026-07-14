package com.oqba26.hyperyar.util

import android.content.Context
import android.net.Uri
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.oqba26.hyperyar.data.Product
import java.io.File

object FileHelper {

    fun exportProductsToCsv(context: Context, products: List<Product>): File {
        val file = File(context.cacheDir, "products_export.csv")
        csvWriter().open(file) {
            writeRow(listOf("نام", "بارکد", "قیمت فروش", "قیمت خرید", "موجودی", "واحد", "دسته بندی"))
            products.forEach { product ->
                writeRow(
                    listOf(
                        product.name,
                        product.barcode,
                        product.sellPrice.toString(),
                        product.buyPrice.toString(),
                        product.stock.toString(),
                        product.unit,
                        product.category
                    )
                )
            }
        }
        return file
    }

    fun importProductsFromCsv(context: Context, uri: Uri): List<Product> {
        val products = mutableListOf<Product>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            csvReader().readAllWithHeader(inputStream).forEach { row ->
                val name = row["نام"] ?: return@forEach
                val barcode = row["بارکد"] ?: ""
                val sellPrice = row["قیمت فروش"]?.toDoubleOrNull() ?: 0.0
                val buyPrice = row["قیمت خرید"]?.toDoubleOrNull() ?: 0.0
                val stock = row["موجودی"]?.toDoubleOrNull() ?: 0.0
                val unit = row["واحد"] ?: "عدد"
                val category = row["دسته بندی"] ?: "عمومی"
                
                products.add(
                    Product(
                        name = name,
                        barcode = barcode,
                        sellPrice = sellPrice,
                        buyPrice = buyPrice,
                        stock = stock,
                        unit = unit,
                        category = category
                    )
                )
            }
        }
        return products
    }
}
