package com.oqba26.hyperyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BundleDao {
    @Transaction
    @Query("SELECT * FROM bundles")
    fun getAllBundles(): Flow<List<BundleWithProducts>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundle(bundle: Bundle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleProductCrossRef(crossRef: BundleProductCrossRef)

    @Delete
    suspend fun deleteBundle(bundle: Bundle)
}
