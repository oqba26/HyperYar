package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "debt_transactions")
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int, // Customer or Supplier ID
    val personType: String, // Customer, Supplier
    val amount: Double,
    val type: String, // Payment, Debt
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val isPaid: Boolean = false,
    val dueDate: Long? = null,
    val invoiceId: Int? = null,
    val paymentTimestamp: Long? = null
)
