package com.keubis.zentryc.data.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense

class FirebaseRepository {

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Obtiene el uid del usuario actual
    private val userId get() = auth.currentUser?.uid

    // Referencia a la colección de transacciones del usuario actual
    private fun transactionsRef() = firestore
        .collection("users")
        .document(userId ?: "unknown")
        .collection("transactions")

    // Referencia a la colección de categorías del usuario actual
    private fun categoriesRef() = firestore
        .collection("users")
        .document(userId ?: "unknown")
        .collection("categories")

    // Guarda una transacción en Firestore usando id único compuesto
    fun syncExpense(expense: Expense) {
        val data = hashMapOf(
            "id" to expense.id,
            "amount" to expense.amount,
            "description" to expense.description,
            "date" to expense.date,
            "type" to expense.type,
            "categoryId" to expense.categoryId
        )
        // Usamos el id de Room como clave del documento
        // Cada usuario tiene su propia colección así que no hay conflictos
        transactionsRef()
            .document("expense_${expense.id}")
            .set(data)
    }

    // Elimina una transacción de Firestore
    fun deleteExpense(expense: Expense) {
        transactionsRef()
            .document("expense_${expense.id}")
            .delete()
    }

    // Guarda una categoría en Firestore
    fun syncCategory(category: Category) {
        val data = hashMapOf(
            "id" to category.id,
            "name" to category.name,
            "iconName" to category.iconName,
            "colorHex" to category.colorHex
        )
        categoriesRef().document(category.id.toString()).set(data)
    }

    // Elimina una categoría de Firestore
    fun deleteCategory(category: Category) {
        categoriesRef().document(category.id.toString()).delete()
    }

    // Descarga todas las transacciones del usuario desde Firestore
    fun loadExpensesFromFirestore(onComplete: (List<Expense>) -> Unit) {
        val uid = userId ?: return
        firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.mapNotNull { doc ->
                    try {
                        Expense(
                            // Convierte cada documento de Firestore a un objeto Expense
                            id = (doc.getLong("id") ?: 0).toInt(),
                            amount = doc.getDouble("amount") ?: 0.0,
                            description = doc.getString("description") ?: "",
                            date = doc.getLong("date") ?: 0L,
                            type = doc.getString("type") ?: "EXPENSE",
                            categoryId = doc.getLong("categoryId")?.toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onComplete(expenses)
            }
            .addOnFailureListener {
                // Si falla simplemente devuelve lista vacía
                onComplete(emptyList())
            }
    }

    // Descarga todas las categorías del usuario desde Firestore
    fun loadCategoriesFromFirestore(onComplete: (List<Category>) -> Unit) {
        val uid = userId ?: return
        firestore.collection("users")
            .document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                val categories = documents.mapNotNull { doc ->
                    try {
                        Category(
                            id = (doc.getLong("id") ?: 0).toInt(),
                            name = doc.getString("name") ?: "",
                            iconName = doc.getString("iconName") ?: "other",
                            colorHex = doc.getString("colorHex") ?: "#95A5A6"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onComplete(categories)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }
}