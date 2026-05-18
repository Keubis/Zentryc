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
            // Guarda en local y obtiene el id real generado por Room
            val generatedId = localRepository.insertCategory(category)
            // Crea una copia con el id real
            val categoryWithId = category.copy(id = generatedId.toInt())
            // Sincroniza con Firebase usando el id real
            firebaseRepository.syncCategory(categoryWithId)
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

    // Sincroniza los datos de Firestore a Room al iniciar sesión
    fun syncFromFirestore() {
        viewModelScope.launch {

            // Limpia Room para evitar duplicados
            localRepository.deleteAllExpenses()


            // Descarga y guarda categorías
            firebaseRepository.loadCategoriesFromFirestore { categories ->
                viewModelScope.launch {
                    localRepository.deleteAllCategories()

                    if (categories.isEmpty()) {
                        // Cuenta nueva — inserta categorías por defecto y las sube a Firestore
                        val defaultCategories = listOf(
                            com.keubis.zentryc.data.model.Category(name = "Alimentación", iconName = "food",      colorHex = "#FF5733"),
                            com.keubis.zentryc.data.model.Category(name = "Transporte",   iconName = "transport", colorHex = "#3380FF"),
                            com.keubis.zentryc.data.model.Category(name = "Ocio",         iconName = "leisure",   colorHex = "#9B59B6"),
                            com.keubis.zentryc.data.model.Category(name = "Salud",        iconName = "health",    colorHex = "#2ECC71"),
                            com.keubis.zentryc.data.model.Category(name = "Hogar",        iconName = "home",      colorHex = "#F39C12"),
                            com.keubis.zentryc.data.model.Category(name = "Nómina",       iconName = "salary",    colorHex = "#1ABC9C"),
                            com.keubis.zentryc.data.model.Category(name = "Otros",        iconName = "other",     colorHex = "#95A5A6")
                        )
                        defaultCategories.forEach { category ->
                            val generatedId = localRepository.insertCategory(category)
                            val categoryWithId = category.copy(id = generatedId.toInt())
                            firebaseRepository.syncCategory(categoryWithId)
                        }
                    } else {
                        // Cuenta existente — carga sus categorías
                        categories.forEach { category ->
                            localRepository.insertCategory(category)
                        }
                    }

                    // Descarga y guarda transacciones
                    firebaseRepository.loadExpensesFromFirestore { expenses ->
                        viewModelScope.launch {
                            expenses.forEach { expense ->
                                localRepository.insertExpense(expense)
                            }
                        }
                    }
                }
            }
        }
    }

    // Limpia todos los datos locales de Room al cerrar sesión
    fun clearLocalData() {
        viewModelScope.launch {
            localRepository.deleteAllExpenses()
            localRepository.deleteAllCategories()
        }
    }

    // Obtiene las transacciones filtradas por rango de fechas
    fun getTransactionsByDateRange(startDate: Long, endDate: Long) =
        localRepository.getTransactionsByDateRange(startDate, endDate)

    // Obtiene el total de ingresos en un rango de fechas
    fun getIncomeByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return localRepository.getIncomeByDateRange(startDate, endDate)
    }

    // Obtiene el total de gastos en un rango de fechas
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return localRepository.getExpensesByDateRange(startDate, endDate)
    }

    // Obtiene transacciones filtradas por categoría
    fun getTransactionsByCategory(categoryId: Int) =
        localRepository.getTransactionsByCategory(categoryId)

    // Obtiene transacciones filtradas por categoría y rango de fechas
    fun getTransactionsByCategoryAndDateRange(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ) = localRepository.getTransactionsByCategoryAndDateRange(categoryId, startDate, endDate)
}