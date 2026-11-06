package com.murrel.pocketledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room database: a thin wrapper around SQLite that knows about the
 * [Expense] entity and hands out the [ExpenseDao].
 *
 * Only ONE instance should ever exist for the app, so [getInstance]
 * uses the classic double-checked singleton pattern.
 */
@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class PocketLedgerDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: PocketLedgerDatabase? = null

        fun getInstance(context: Context): PocketLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketLedgerDatabase::class.java,
                    "pocketledger.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
