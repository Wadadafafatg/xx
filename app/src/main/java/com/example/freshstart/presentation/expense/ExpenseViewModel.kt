package com.example.freshstart.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshstart.data.local.Expense
import com.example.freshstart.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository
        .getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // حساب المجموع على الـ Background Thread لضمان أداء عالي وسلاسة تامة بدون Lag
    val totalExpenses: StateFlow<Long> = repository
        .getAllExpenses()
        .map { list -> list.sumOf { it.amount } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _addExpenseSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val addExpenseSuccess = _addExpenseSuccess

    fun addExpense(amount: Long, description: String) {
        viewModelScope.launch {
            if (amount <= 0L || description.isBlank()) return@launch

            repository.insertExpense(
                Expense(amount = amount, description = description.trim())
            )
            _addExpenseSuccess.tryEmit(Unit)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}