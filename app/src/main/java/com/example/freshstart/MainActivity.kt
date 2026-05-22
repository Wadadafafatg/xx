package com.example.freshstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.freshstart.data.local.ExpenseDatabase
import com.example.freshstart.data.repository.ExpenseRepositoryImpl
import com.example.freshstart.presentation.expense.ExpenseScreen
import com.example.freshstart.presentation.expense.ExpenseViewModel
import com.example.freshstart.ui.theme.FreshStartTheme

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseDatabase::class.java,
            "expense_db"
        ).build()
    }

    private val viewModel by lazy {
        ExpenseViewModel(
            repository = ExpenseRepositoryImpl(database.expenseDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreshStartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseScreen(viewModel = viewModel)
                }
            }
        }
    }
}
