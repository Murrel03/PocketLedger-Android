package com.murrel.pocketledger

import com.murrel.pocketledger.data.Expense
import com.murrel.pocketledger.domain.ExpenseCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Plain JVM unit tests for [ExpenseCalculator] — no emulator, no Android.
 * Run with: ./gradlew test   (or the green play button in Android Studio).
 *
 * This is exactly why the summary maths lives in a pure Kotlin object:
 * it can be verified here in milliseconds.
 */
class ExpenseCalculatorTest {

    /** Build an expense on a given date, in the same time zone the calculator uses. */
    private fun expenseOn(date: LocalDate, amount: Double, category: String = "Food"): Expense {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return Expense(title = "Test", amount = amount, category = category, dateMillis = millis)
    }

    @Test
    fun total_sumsAllAmounts() {
        val expenses = listOf(
            expenseOn(LocalDate.of(2026, 11, 3), 100.0),
            expenseOn(LocalDate.of(2026, 11, 10), 250.50)
        )
        assertEquals(350.50, ExpenseCalculator.total(expenses), 0.001)
    }

    @Test
    fun totalForMonth_ignoresOtherMonths() {
        val expenses = listOf(
            expenseOn(LocalDate.of(2026, 11, 3), 100.0),
            expenseOn(LocalDate.of(2026, 10, 28), 999.0), // October — must NOT be counted
            expenseOn(LocalDate.of(2026, 11, 20), 50.0)
        )
        val novemberTotal = ExpenseCalculator.totalForMonth(expenses, YearMonth.of(2026, 11))
        assertEquals(150.0, novemberTotal, 0.001)
    }

    @Test
    fun categoryTotals_groupsAndSortsHighestFirst() {
        val expenses = listOf(
            expenseOn(LocalDate.of(2026, 11, 3), 300.0, category = "Food"),
            expenseOn(LocalDate.of(2026, 11, 4), 900.0, category = "Bills"),
            expenseOn(LocalDate.of(2026, 11, 5), 200.0, category = "Food")
        )
        val totals = ExpenseCalculator.categoryTotals(expenses)

        assertEquals(500.0, totals["Food"]!!, 0.001)
        assertEquals(900.0, totals["Bills"]!!, 0.001)
        // Highest first: Bills (900) before Food (500)
        assertEquals("Bills", totals.keys.first())
    }

    @Test
    fun emptyList_totalsAreZero() {
        assertEquals(0.0, ExpenseCalculator.total(emptyList()), 0.001)
        assertTrue(ExpenseCalculator.categoryTotals(emptyList()).isEmpty())
    }
}
