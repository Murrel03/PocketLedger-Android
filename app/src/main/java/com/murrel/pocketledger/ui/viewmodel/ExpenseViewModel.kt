package com.murrel.pocketledger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.murrel.pocketledger.data.Expense
import com.murrel.pocketledger.data.ExpenseRepository
import com.murrel.pocketledger.domain.ExpenseCalculator
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the list screen needs, in one immutable snapshot. */
data class PocketLedgerUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    /** Expenses in [selectedMonth] only, newest first (the DAO already sorts). */
    val expenses: List<Expense> = emptyList(),
    val monthlyTotal: Double = 0.0,
    /** Category -> amount for [selectedMonth], highest first. */
    val categoryTotals: Map<String, Double> = emptyMap()
)

/**
 * ViewModel: holds UI state and survives configuration changes
 * (e.g. rotating the phone does NOT recreate it or re-query the DB).
 *
 * It exposes ONE [StateFlow] of [PocketLedgerUiState]. Whenever Room
 * emits new expenses, or the user changes month, the state is recomputed
 * and Compose re-renders automatically.
 */
class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<PocketLedgerUiState> =
        combine(repository.allExpenses, selectedMonth) { allExpenses, month ->
            val monthExpenses = ExpenseCalculator.expensesInMonth(allExpenses, month)
            PocketLedgerUiState(
                selectedMonth = month,
                expenses = monthExpenses,
                monthlyTotal = ExpenseCalculator.total(monthExpenses),
                categoryTotals = ExpenseCalculator.categoryTotals(monthExpenses)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PocketLedgerUiState()
        )

    fun goToPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    suspend fun getExpenseById(id: Int): Expense? = repository.getExpenseById(id)

    fun addExpense(title: String, amount: Double, category: String, dateMillis: Long, note: String) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(title = title, amount = amount, category = category, dateMillis = dateMillis, note = note)
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}

/**
 * Factory so Compose's viewModel() can build an [ExpenseViewModel],
 * which needs the repository passed in (manual dependency injection —
 * no Hilt in v1, on purpose, to keep the moving parts visible).
 */
class ExpenseViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
