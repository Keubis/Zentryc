package com.keubis.zentryc.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.keubis.zentryc.data.model.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category) : Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}