package com.keubis.zentryc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconName: String,   // nombre del icono, ej: "food", "transport"
    val colorHex: String    // color en hex, ej: "#FF5733"
)