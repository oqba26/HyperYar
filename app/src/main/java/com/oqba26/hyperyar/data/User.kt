package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

import androidx.room.Index

@Serializable
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val role: String, // ADMIN, STAFF
    val lastLogin: Long = System.currentTimeMillis()
)
