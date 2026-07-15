package com.oqba26.hyperyar.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceType {
    SALE, PURCHASE, RETURN_SALE, RETURN_PURCHASE
}

@Serializable
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: InvoiceType = InvoiceType.SALE,
    val totalAmount: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val customerId: Int? = null,
    val supplierId: Int? = null,
    val amountPaid: Double = 0.0,
    val dueDate: Long? = null,
    val isSynced: Boolean = false
)

@Serializable
@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Double = 0.0,
    val unit: String = "عدد",
    val priceAtSale: Double = 0.0,
    val buyPriceAtSale: Double = 0.0,
    val discount: Double = 0.0
)

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)
