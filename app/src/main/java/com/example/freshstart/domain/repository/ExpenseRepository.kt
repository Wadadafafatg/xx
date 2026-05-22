// File: app/src/main/java/com/example/freshstart/domain/repository/ExpenseRepository.kt
package com.example.freshstart.domain.repository

import com.example.freshstart.data.local.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}
