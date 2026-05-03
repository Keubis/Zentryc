package com.keubis.zentryc.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.keubis.zentryc.data.database.FirebaseRepository
import com.keubis.zentryc.data.database.ZentrycRepository
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.data.model.TransactionWithCategory
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio local Room
    private val localRepository = ZentrycRepository(application)

    // Repositorio remoto Firebase
    private val firebaseRepository = FirebaseRepository()

    val allTransactions: LiveData<List<TransactionWithCategory>> =
        localRepository.allTransactions
    val allCategories: LiveData<List<Category>> = localRepository.allCategories
    val totalIncome: LiveData<Double?> = localRepository.totalIncome
    val totalExpenses: LiveData<Double?> = localRepository.totalExpenses

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            // Guarda en local y obtiene el id real generado por Room
            val generatedId = localRepository.insertExpense(expense)

            // Crea una copia del expense con el id real de Room
            val expenseWithId = expense.copy(id = generatedId.toInt())

            // Sincroniza con Firebase usando el id real
            firebaseRepository.syncExpense(expenseWithId)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            // Elimina en local
            localRepository.deleteExpense(expense)
            // Luego elimina en Firebase
            firebaseRepository.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            // Actualiza en local
            localRepository.updateExpense(expense)
            // Luego sincroniza con Firebase
            firebaseRepository.syncExpense(expense)
        }
    }

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            // Guarda primero en local
            localRepository.insertCategory(category)
            // Luego sincroniza con Firebase
            firebaseRepository.syncCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            // Elimina en local
            localRepository.deleteCategory(category)
            // Luego elimina en Firebase
            firebaseRepository.deleteCategory(category)
        }
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long) =
        localRepository.getTransactionsByDateRange(startDate, endDate)

    // Sincroniza los datos de Firestore a Room al iniciar sesión
    fun syncFromFirestore() {
        viewModelScope.launch {

            // Limpia Room para evitar duplicados
            localRepository.deleteAllExpenses()

            // Descarga y guarda transacciones
            firebaseRepository.loadExpensesFromFirestore { expenses ->
                viewModelScope.launch {
                    expenses.forEach { expense ->
                        localRepository.insertExpense(expense)
                    }
                }
            }

            // Descarga y guarda categorías
            firebaseRepository.loadCategoriesFromFirestore { categories ->
                viewModelScope.launch {
                    categories.forEach { category ->
                        localRepository.insertCategory(category)
                    }
                }
            }
        }
    }

    // Limpia todos los datos locales de Room al cerrar sesión
    fun clearLocalData() {
        viewModelScope.launch {
            localRepository.deleteAllExpenses()
        }
    }
}