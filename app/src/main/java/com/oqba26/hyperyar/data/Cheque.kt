package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cheques")
data class Cheque(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val chequeNumber: String,
    val amount: Double,
    val dueDate: Long,
    val issueDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Cashed, Bounced
    val type: String, // Received, Paid
    val personName: String, // Customer or Supplier name
    val description: String = ""
)
