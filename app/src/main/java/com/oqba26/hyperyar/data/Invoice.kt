package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int? = null,
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, Card, Credit
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalLine: Double
)
