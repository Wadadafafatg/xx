// File: app/src/main/java/com/example/freshstart/presentation/expense/ExpenseViewModel.kt
package com.example.freshstart.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshstart.data.local.Expense
import com.example.freshstart.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository
        .getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalExpenses: StateFlow<Long> = expenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun addExpense(amount: Long, description: String) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(amount = amount, description = description.trim())
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}
