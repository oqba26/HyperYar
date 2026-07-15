package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val name: String,
    val barcode: String = "",
    val category: String = "General",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stock: Double = 0.0,
    val unit: String = "pcs",
    val unitsInPack: Double = 1.0, // New field for bulk to individual conversion
    val isFavorite: Boolean = false, // New field for quick access
    val expiryDate: String? = null,
    val description: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
