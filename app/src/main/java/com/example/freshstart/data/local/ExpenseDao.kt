// File: app/src/main/java/com/example/freshstart/data/local/ExpenseDao.kt
package com.example.freshstart.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesOrderedByDate(): Flow<List<Expense>>
}
