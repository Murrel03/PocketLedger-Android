package com.murrel.pocketledger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.murrel.pocketledger.data.ExpenseRepository
import com.murrel.pocketledger.data.PocketLedgerDatabase
import com.murrel.pocketledger.ui.screens.AddEditExpenseScreen
import com.murrel.pocketledger.ui.screens.ExpenseListScreen
import com.murrel.pocketledger.ui.viewmodel.ExpenseViewModel
import com.murrel.pocketledger.ui.viewmodel.ExpenseViewModelFactory

/** Navigation routes for the whole app — only three destinations in v1. */
object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{expenseId}"
    fun edit(expenseId: Int) = "edit/$expenseId"
}

/**
 * App entry composable: builds the database -> repository -> ViewModel
 * chain once, then hosts the navigation graph.
 *
 * Data flow, top to bottom:
 *   Room (SQLite) -> Repository -> ViewModel (StateFlow) -> Compose screens
 */
@Composable
fun PocketLedgerApp() {
    val context = LocalContext.current

    // remember{} so the database/repository are built once per composition tree.
    val repository = remember {
        val database = PocketLedgerDatabase.getInstance(context)
        ExpenseRepository(database.expenseDao())
    }
    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(repository)
    )

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            ExpenseListScreen(
                viewModel = expenseViewModel,
                onAddExpense = { navController.navigate(Routes.ADD) },
                onEditExpense = { expense -> navController.navigate(Routes.edit(expense.id)) }
            )
        }

        composable(Routes.ADD) {
            AddEditExpenseScreen(
                viewModel = expenseViewModel,
                expenseId = null,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId")
            AddEditExpenseScreen(
                viewModel = expenseViewModel,
                expenseId = expenseId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
