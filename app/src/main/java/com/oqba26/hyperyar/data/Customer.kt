package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val loyaltyPoints: Int = 0,
    val balance: Double = 0.0, // positive means credit, negative means debt
    val address: String = "",
    val registrationDate: Long = System.currentTimeMillis()
)
