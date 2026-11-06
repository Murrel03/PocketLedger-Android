package com.murrel.pocketledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row in the "expenses" table.
 *
 * Room reads these annotations at compile time (via KSP) and generates the
 * SQLite code for us — we never write CREATE TABLE by hand.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    /** Amount in rupees, e.g. 249.50 */
    val amount: Double,
    /** One of [ExpenseCategories.ALL], e.g. "Food". Stored as plain text to keep v1 simple. */
    val category: String,
    /** When the expense happened, as epoch milliseconds (System.currentTimeMillis style). */
    val dateMillis: Long,
    val note: String = ""
)

/** The fixed category list used by the add/edit screen and the breakdown summary. */
object ExpenseCategories {
    val ALL = listOf(
        "Food",
        "Transport",
        "Bills",
        "Shopping",
        "Entertainment",
        "Health",
        "Other"
    )
}
