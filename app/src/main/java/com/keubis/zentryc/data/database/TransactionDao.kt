package com.keubis.zentryc.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.data.model.TransactionWithCategory

@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): LiveData<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenses(): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Elimina todas las transacciones de Room
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}