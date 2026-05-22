// File: app/src/main/java/com/example/freshstart/presentation/expense/ExpenseScreen.kt
package com.example.freshstart.presentation.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freshstart.data.local.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val expenses = viewModel.expenses.collectAsStateWithLifecycle().value
    val total = viewModel.totalExpenses.collectAsStateWithLifecycle().value

    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total Expenses", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$total IQD",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it.filter(Char::isDigit) },
            label = { Text("Amount (IQD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
            label = { Text("Description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val amount = amountInput.toLongOrNull() ?: 0L
                if (amount > 0L && descriptionInput.isNotBlank()) {
                    viewModel.addExpense(amount, descriptionInput)
                    amountInput = ""
                    descriptionInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = Expense::id) { expense ->
                ExpenseItem(
                    expense = expense,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.description, style = MaterialTheme.typography.titleSmall)
                Text(text = "${expense.amount} IQD", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
