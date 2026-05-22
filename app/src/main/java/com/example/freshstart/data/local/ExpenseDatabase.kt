// File: app/src/main/java/com/example/freshstart/data/local/ExpenseDatabase.kt
package com.example.freshstart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
