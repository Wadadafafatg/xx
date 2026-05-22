// File: app/src/main/java/com/example/freshstart/data/repository/ExpenseRepositoryImpl.kt
package com.example.freshstart.data.repository

import com.example.freshstart.data.local.Expense
import com.example.freshstart.data.local.ExpenseDao
import com.example.freshstart.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpensesOrderedByDate()

    override suspend fun insertExpense(expense: Expense) {
        expenseDao.insert(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
    }
}
