package com.murrel.pocketledger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object. This interface is the ONLY place SQL lives.
 * Room generates the implementation at compile time.
 *
 * Reads return [Flow] so the UI is notified automatically whenever the
 * table changes. Writes are `suspend` so they run inside a coroutine,
 * off the main thread.
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun observeAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
