package com.murrel.pocketledger.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository: the single source of truth for expense data.
 *
 * The ViewModel talks ONLY to this class — never to the DAO or Room
 * directly. Today the data comes from Room; if a backend API is added
 * later, this is the one class that changes. That separation is the
 * whole point of the repository layer in MVVM.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    /** Emits a fresh list every time the expenses table changes. */
    val allExpenses: Flow<List<Expense>> = expenseDao.observeAllExpenses()

    suspend fun getExpenseById(id: Int): Expense? = expenseDao.getExpenseById(id)

    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
}
