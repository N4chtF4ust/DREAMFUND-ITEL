// app/src/main/java/com/example/dreamfunds/viewmodel/ReportsViewModel.kt
package com.example.dreamfunds.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamfunds.data.model.Transaction
import com.example.dreamfunds.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReportItem(
    val name: String,
    val percentage: Float,
    val amount: String,
    val rawAmount: Double,
    val color: Color
)

data class ReportsUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val filteredExpenses: List<Transaction> = emptyList(),
    val reportItems: List<ReportItem> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    // Selected month offset: 0 = current month, -1 = last month, etc.
    val monthOffset: Int = 0,
    val displayMonth: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportsViewModel : ViewModel() {

    private val repo = TransactionRepository()

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val categoryColors = mapOf(
        "Food"     to Color(0xFF4CAF50),
        "Bills"    to Color(0xFF2196F3),
        "Travel"   to Color(0xFFFF9800),
        "Shopping" to Color(0xFFE91E63),
        "Health"   to Color(0xFF00BCD4),
        "Others"   to Color(0xFF9C27B0)
    )

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.getTransactions()
            if (result.isSuccess) {
                val all = result.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(allTransactions = all, isLoading = false)
                applyMonthFilter(_uiState.value.monthOffset)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun goToPreviousMonth() = applyMonthFilter(_uiState.value.monthOffset - 1)
    fun goToNextMonth()     = applyMonthFilter(_uiState.value.monthOffset + 1)

    private fun applyMonthFilter(offset: Int) {
        // Cap: don't go into the future
        val cappedOffset = offset.coerceAtMost(0)

        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, cappedOffset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val year  = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1   // 1-based

        // Label shown in UI e.g. "March 2025"
        val displayMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        val all = _uiState.value.allTransactions

        // Filter by YYYY-MM prefix of transaction_date ("yyyy-MM-dd")
        val monthPrefix = String.format("%04d-%02d", year, month)
        val inMonth = all.filter { it.transactionDate.startsWith(monthPrefix) }

        val expenses = inMonth.filter { !it.isIncome }
        val income   = inMonth.filter {  it.isIncome }
        val totalSpent  = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf   { it.amount }

        val categories = listOf("Food", "Bills", "Travel", "Shopping", "Health", "Others")
        val divisor = totalSpent.coerceAtLeast(0.01)   // avoid /0
        val reportItems = categories.map { category ->
            val amt = expenses.filter { it.category == category }.sumOf { it.amount }
            ReportItem(
                name       = category,
                percentage = (amt / divisor).toFloat().coerceIn(0f, 1f),
                amount     = "₱ ${String.format("%,.2f", amt)}",
                rawAmount  = amt,
                color      = categoryColors[category] ?: Color.Gray
            )
        }

        _uiState.value = _uiState.value.copy(
            filteredExpenses = expenses,
            reportItems      = reportItems,
            totalSpent       = totalSpent,
            totalIncome      = totalIncome,
            monthOffset      = cappedOffset,
            displayMonth     = displayMonth
        )
    }
}