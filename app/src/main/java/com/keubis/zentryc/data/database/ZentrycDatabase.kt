package com.keubis.zentryc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Expense::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class ZentrycDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ZentrycDatabase? = null

        fun getDatabase(context: Context): ZentrycDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZentrycDatabase::class.java,
                    "zentryc_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Inserta categorías por defecto la primera vez que se crea la base de datos
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Alimentación",  iconName = "food",      colorHex = "#FF5733"),
                Category(name = "Transporte",    iconName = "transport", colorHex = "#3380FF"),
                Category(name = "Ocio",          iconName = "leisure",   colorHex = "#9B59B6"),
                Category(name = "Salud",         iconName = "health",    colorHex = "#2ECC71"),
                Category(name = "Hogar",         iconName = "home",      colorHex = "#F39C12"),
                Category(name = "Nómina",        iconName = "salary",    colorHex = "#1ABC9C"),
                Category(name = "Otros",         iconName = "other",     colorHex = "#95A5A6")
            )
            defaultCategories.forEach { categoryDao.insertCategory(it) }
        }
    }
}