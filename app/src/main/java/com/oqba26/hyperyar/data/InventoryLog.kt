package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val changeAmount: Double,
    val type: String, // Manual, Sale, Purchase
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
