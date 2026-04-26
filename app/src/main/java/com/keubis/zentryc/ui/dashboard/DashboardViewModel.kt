package com.keubis.zentryc.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.keubis.zentryc.data.database.ZentrycRepository
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.data.model.TransactionWithCategory
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ZentrycRepository(application)

    val allTransactions: LiveData<List<TransactionWithCategory>> = repository.allTransactions
    val allCategories: LiveData<List<Category>> = repository.allCategories
    val totalIncome: LiveData<Double?> = repository.totalIncome
    val totalExpenses: LiveData<Double?> = repository.totalExpenses

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }
    fun getTransactionsByDateRange(startDate: Long, endDate: Long) =
        repository.getTransactionsByDateRange(startDate, endDate)

}