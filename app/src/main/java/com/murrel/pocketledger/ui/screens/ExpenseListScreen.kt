package com.murrel.pocketledger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.murrel.pocketledger.data.Expense
import com.murrel.pocketledger.ui.viewmodel.ExpenseViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** "₹1,250.50" style formatting, kept in one place. */
fun formatAmount(amount: Double): String = "₹%,.2f".format(amount)

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

fun formatDate(dateMillis: Long): String =
    Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)

/**
 * Home screen: month switcher, total + category breakdown for that month,
 * then the expense list itself.
 *
 * This composable is STATELESS about data: it reads the ViewModel's
 * StateFlow with collectAsState(), and every recomposition is driven by
 * a new state emission. It never touches Room or the repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("PocketLedger") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Month switcher ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = uiState.selectedMonth.format(monthFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.goToNextMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            }

            // --- Monthly total + category breakdown ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total spent", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = formatAmount(uiState.monthlyTotal),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.categoryTotals.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("By category", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            uiState.categoryTotals.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(category)
                                    Text(formatAmount(amount))
                                }
                            }
                        }
                    }
                }
            }

            // --- Expense list ---
            if (uiState.expenses.isEmpty()) {
                item {
                    Text(
                        text = "No expenses this month. Tap + to add your first one.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(uiState.expenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onClick = { onEditExpense(expense) },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${expense.category} • ${formatDate(expense.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (expense.note.isNotBlank()) {
                    Text(expense.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(formatAmount(expense.amount), fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${expense.title}")
            }
        }
    }
}
