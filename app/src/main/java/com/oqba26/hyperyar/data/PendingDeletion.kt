package com.oqba26.hyperyar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableName: String,
    val remoteId: String,
    val timestamp: Long = System.currentTimeMillis()
)
