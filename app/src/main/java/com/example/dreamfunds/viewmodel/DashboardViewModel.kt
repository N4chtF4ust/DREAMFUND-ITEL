// app/src/main/java/com/example/dreamfunds/viewmodel/DashboardViewModel.kt
package com.example.dreamfunds.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamfunds.data.model.SavingsGoal
import com.example.dreamfunds.data.model.SavingsGoalInsert
import com.example.dreamfunds.data.model.Transaction
import com.example.dreamfunds.data.model.TransactionInsert
import com.example.dreamfunds.data.repository.GoalRepository
import com.example.dreamfunds.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val visibleTransactions: List<Transaction> = emptyList(),
    val allGoals: List<SavingsGoal> = emptyList(),
    val visibleGoals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    // Transaction pagination
    val currentPage: Int = 1,
    val pageSize: Int = TX_PAGE_SIZE,
    val hasMoreTransactions: Boolean = false,
    // Goal pagination
    val goalPage: Int = 1,
    val hasMoreGoals: Boolean = false
) {
    companion object {
        const val TX_PAGE_SIZE   = 10
        const val GOAL_PAGE_SIZE = 3
    }
}

class DashboardViewModel : ViewModel() {

    private val transactionRepo = TransactionRepository()
    private val goalRepo = GoalRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val tResult      = transactionRepo.getTransactions()
            val gResult      = goalRepo.getGoals()
            val transactions = tResult.getOrDefault(emptyList())
            val goals        = gResult.getOrDefault(emptyList())
            val totalIncome  = transactions.filter {  it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
            val txPage       = 1
            val txPageSize   = DashboardUiState.TX_PAGE_SIZE
            val goalPage     = 1
            val goalPageSize = DashboardUiState.GOAL_PAGE_SIZE
            _uiState.value = DashboardUiState(
                allTransactions      = transactions,
                visibleTransactions  = transactions.take(txPage * txPageSize),
                allGoals             = goals,
                visibleGoals         = goals.take(goalPage * goalPageSize),
                isLoading            = false,
                error                = tResult.exceptionOrNull()?.message,
                totalIncome          = totalIncome,
                totalExpense         = totalExpense,
                totalBalance         = totalIncome - totalExpense,
                currentPage          = txPage,
                pageSize             = txPageSize,
                hasMoreTransactions  = transactions.size > txPage * txPageSize,
                goalPage             = goalPage,
                hasMoreGoals         = goals.size > goalPage * goalPageSize
            )
        }
    }

    /** Show the next page of transactions (no network call). */
    fun loadMore() {
        val state    = _uiState.value
        if (!state.hasMoreTransactions) return
        val nextPage = state.currentPage + 1
        val visible  = state.allTransactions.take(nextPage * state.pageSize)
        _uiState.value = state.copy(
            visibleTransactions = visible,
            currentPage         = nextPage,
            hasMoreTransactions = state.allTransactions.size > nextPage * state.pageSize
        )
    }

    /** Show the next page of goals (no network call). */
    fun loadMoreGoals() {
        val state    = _uiState.value
        if (!state.hasMoreGoals) return
        val nextPage = state.goalPage + 1
        val visible  = state.allGoals.take(nextPage * DashboardUiState.GOAL_PAGE_SIZE)
        _uiState.value = state.copy(
            visibleGoals = visible,
            goalPage     = nextPage,
            hasMoreGoals = state.allGoals.size > nextPage * DashboardUiState.GOAL_PAGE_SIZE
        )
    }

    fun addTransaction(insert: TransactionInsert) {
        viewModelScope.launch {
            val result = transactionRepo.addTransaction(insert)
            if (result.isSuccess) loadAll()
            else _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepo.deleteTransaction(id)
            loadAll()
        }
    }

    fun addGoal(insert: SavingsGoalInsert) {
        viewModelScope.launch {
            val result = goalRepo.addGoal(insert)
            if (result.isSuccess) loadAll()
            else _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            goalRepo.deleteGoal(id)
            loadAll()
        }
    }

    /**
     * Contribute [amount] to a savings goal.
     * Deducts from balance (adds an expense transaction tagged "Goal: <name>")
     * and bumps the goal's saved_amount.
     */
    fun contributeToGoal(goal: SavingsGoal, amount: Double) {
        val id = goal.id ?: return
        viewModelScope.launch {
            // 1. Record as an expense transaction so balance decreases
            val insert = TransactionInsert(
                userId = "",
                title = "Goal: ${goal.name}",
                amount = amount,
                category = "Others",
                isIncome = false,
                transactionDate = java.text.SimpleDateFormat(
                    "yyyy-MM-dd", java.util.Locale.getDefault()
                ).format(java.util.Date())
            )
            val txResult = transactionRepo.addTransaction(insert)
            if (txResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = txResult.exceptionOrNull()?.message
                )
                return@launch
            }
            // 2. Bump saved_amount on the goal
            val newSaved = (goal.savedAmount + amount).coerceAtMost(goal.targetAmount)
            goalRepo.updateSavedAmount(id, newSaved)
            loadAll()
        }
    }
}