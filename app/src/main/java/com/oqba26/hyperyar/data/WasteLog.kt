package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "waste_logs")
data class WasteLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val amount: Double,
    val reason: String, // EXPIRED, DAMAGED, OTHER
    val timestamp: Long = System.currentTimeMillis()
)
