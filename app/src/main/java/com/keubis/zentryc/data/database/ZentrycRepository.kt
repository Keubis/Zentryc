package com.keubis.zentryc.data.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.data.model.TransactionWithCategory

class ZentrycRepository(context: Context) {

    private val database = ZentrycDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()

    // Transacciones
    val allTransactions: LiveData<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsWithCategory()

    val totalIncome: LiveData<Double?> = transactionDao.getTotalIncome()
    val totalExpenses: LiveData<Double?> = transactionDao.getTotalExpenses()

    // Inserta una transacción y devuelve el id generado por Room
    suspend fun insertExpense(expense: Expense): Long {
        return transactionDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        transactionDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        transactionDao.deleteExpense(expense)
    }

    fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    // Categorías
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteAllExpenses() {
        transactionDao.deleteAllTransactions()
    }

    // Total de ingresos en un rango de fechas
    fun getIncomeByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return transactionDao.getIncomeByDateRange(startDate, endDate)
    }

    // Total de gastos en un rango de fechas
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return transactionDao.getExpensesByDateRange(startDate, endDate)
    }

    // Obtiene transacciones por categoría
    fun getTransactionsByCategory(categoryId: Int): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByCategory(categoryId)
    }

    // Obtiene transacciones por categoría y rango de fechas
    fun getTransactionsByCategoryAndDateRange(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByCategoryAndDateRange(categoryId, startDate, endDate)
    }

    suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }
}