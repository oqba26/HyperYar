package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val balance: Double = 0.0, // positive means we owe them, negative means they owe us
    val lastUpdated: Long = System.currentTimeMillis()
)
