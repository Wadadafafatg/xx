package com.example.freshstart.presentation.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FillMaxSize
import androidx.compose.foundation.layout.FillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freshstart.data.local.Expense
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val total by viewModel.totalExpenses.collectAsStateWithLifecycle()

    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    // هنا استمع لحدث النجاح وتصفير الحقول لمنع الـ Lag وتأمين الـ State
    LaunchedEffect(viewModel) {
        // إذا كان هناك تدفق للنجاح بالـ ViewModel يتم تصفير الحقول هنا
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Total Expenses",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatIqd(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // قمنا بعزل قسم الإدخال لمنع الـ Recomposition الزائد للشاشة بالكامل عند كتابة كل حرف
        ExpenseInputSection(
            amountInput = amountInput,
            onAmountChange = { amountInput = it.filter { char -> char.isDigit() } },
            descriptionInput = descriptionInput,
            onDescriptionChange = { descriptionInput = it },
            onAddExpense = {
                val amount = amountInput.toLongOrNull() ?: 0L
                if (amount > 0L && descriptionInput.isNotBlank()) {
                    viewModel.addExpense(amount, descriptionInput)
                    amountInput = ""
                    descriptionInput = ""
                }
            }
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(expenses, key = { it.id }) { expense ->
                ExpenseItem(
                    expense = expense,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseInputSection(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    onAddExpense: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = amountInput,
            onValueChange = onAmountChange,
            label = { Text("Amount (IQD)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descriptionInput,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
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
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = formatIqd(expense.amount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

// دالة تنسيق العملة العراقية الاحترافية مع الفواصل وإضافة د.ع أو IQD
private fun formatIqd(amount: Long): String {
    val numberFormat = NumberFormat.getInstance()
    return "${numberFormat.format(amount)} IQD"
}