package com.oqba26.hyperyar.data

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val sellPrice: Double = product.sellPrice,
) {
    val totalPrice: Double get() = quantity * sellPrice
}
