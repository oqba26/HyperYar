package com.oqba26.hyperyar.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun insert(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun update(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)
    }

    fun search(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    suspend fun getByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)
    }
}
