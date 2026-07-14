package com.oqba26.hyperyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtTransactionDao {
    @Query("SELECT * FROM debt_transactions WHERE personId = :personId AND personType = :personType ORDER BY timestamp DESC")
    fun getTransactionsForPerson(personId: Int, personType: String): Flow<List<DebtTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DebtTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: DebtTransaction)
}
