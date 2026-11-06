package com.murrel.pocketledger.domain

import com.murrel.pocketledger.data.Expense
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/**
 * Pure Kotlin business logic for summaries — no Android, no Room, no Compose.
 *
 * Because this object depends on nothing but the [Expense] data class and
 * java.time, it can be unit-tested on a plain JVM (see ExpenseCalculatorTest)
 * without an emulator. Keeping maths OUT of the Composables and OUT of the
 * ViewModel is what makes it testable like this.
 */
object ExpenseCalculator {

    /** Sum of every expense in the list. Empty list -> 0.0 */
    fun total(expenses: List<Expense>): Double =
        expenses.sumOf { it.amount }

    /** Only the expenses whose date falls inside [month] (in the device's time zone). */
    fun expensesInMonth(expenses: List<Expense>, month: YearMonth): List<Expense> =
        expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            YearMonth.from(date) == month
        }

    /** Total spent in one calendar month, e.g. totalForMonth(list, YearMonth.of(2026, 11)). */
    fun totalForMonth(expenses: List<Expense>, month: YearMonth): Double =
        total(expensesInMonth(expenses, month))

    /**
     * Spend per category, highest first, e.g. {"Food" -> 1250.0, "Bills" -> 900.0}.
     * The UI renders this map directly as the category breakdown.
     */
    fun categoryTotals(expenses: List<Expense>): Map<String, Double> =
        expenses.groupBy { it.category }
            .mapValues { (_, items) -> total(items) }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
}
