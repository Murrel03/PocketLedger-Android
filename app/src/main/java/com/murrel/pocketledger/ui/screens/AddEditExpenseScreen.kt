package com.murrel.pocketledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.murrel.pocketledger.data.Expense
import com.murrel.pocketledger.data.ExpenseCategories
import com.murrel.pocketledger.ui.viewmodel.ExpenseViewModel

/**
 * One screen for both Add and Edit — the difference is only whether
 * [expenseId] is null (add) or an existing row's id (edit).
 *
 * Form state lives in remember{} here in the UI layer, because it is
 * throwaway editing state, not app state. It is only written to Room
 * (via the ViewModel) when Save is tapped and validation passes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseId: Int?,
    onDone: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategories.ALL.first()) }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingExpense by remember { mutableStateOf<Expense?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // When editing, load the stored expense once and pre-fill the form.
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            viewModel.getExpenseById(expenseId)?.let { expense ->
                existingExpense = expense
                title = expense.title
                amountText = expense.amount.toString()
                category = expense.category
                note = expense.note
                dateMillis = expense.dateMillis
            }
        }
    }

    val isEditing = expenseId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit expense" else "Add expense") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Groceries at DMart") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseCategories.ALL.forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item) }
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Date: ${formatDate(dateMillis)}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> errorMessage = "Please enter a title."
                        amount == null || amount <= 0.0 -> errorMessage = "Please enter an amount greater than 0."
                        else -> {
                            val current = existingExpense
                            if (isEditing && current != null) {
                                viewModel.updateExpense(
                                    current.copy(
                                        title = title.trim(),
                                        amount = amount,
                                        category = category,
                                        note = note.trim(),
                                        dateMillis = dateMillis
                                    )
                                )
                            } else {
                                viewModel.addExpense(
                                    title = title.trim(),
                                    amount = amount,
                                    category = category,
                                    dateMillis = dateMillis,
                                    note = note.trim()
                                )
                            }
                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Save changes" else "Add expense")
            }

            if (isEditing && existingExpense != null) {
                OutlinedButton(
                    onClick = {
                        viewModel.deleteExpense(existingExpense!!)
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete this expense")
                }
            }
        }
    }
}
