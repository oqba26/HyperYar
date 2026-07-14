package com.oqba26.hyperyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteLogDao {
    @Query("SELECT * FROM waste_logs ORDER BY timestamp DESC")
    fun getAllWasteLogs(): Flow<List<WasteLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteLog(log: WasteLog)

    @Delete
    suspend fun deleteWasteLog(log: WasteLog)
}
