package com.keubis.zentryc.data.model

import androidx.room.Entity
import androidx.room.Index
//import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    /*foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )],*/
    indices = [Index(value = ["categoryId"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val description: String,
    val date: Long,             // guardamos la fecha como timestamp
    val type: String,           // "INCOME" o "EXPENSE"
    val categoryId: Int?        // puede ser null si se borra la categoría
)