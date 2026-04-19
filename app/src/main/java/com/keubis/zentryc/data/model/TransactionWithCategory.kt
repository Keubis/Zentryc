
package com.keubis.zentryc.data.model

import androidx.room.Embedded
import androidx.room.Relation

// Esta clase nos permite obtener una transacción con su categoría en una sola consulta
data class TransactionWithCategory(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)