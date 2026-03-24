package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamfunds.data.model.SavingsGoal
import com.example.dreamfunds.data.model.Transaction
import com.example.dreamfunds.ui.components.dialogs.AddBalanceDialog
import com.example.dreamfunds.ui.components.dialogs.AddExpenseDialog
import com.example.dreamfunds.ui.components.dialogs.AddGoalDialog
import com.example.dreamfunds.ui.components.cards.BalanceSummaryCard
import com.example.dreamfunds.ui.components.dialogs.ContributeGoalDialog
import com.example.dreamfunds.ui.components.buttons.DashboardFab
import com.example.dreamfunds.ui.components.cards.GoalPaginatedRow
import com.example.dreamfunds.ui.components.buttons.QuickActionButtons
import com.example.dreamfunds.ui.components.common.DashboardSkeleton
import com.example.dreamfunds.ui.components.common.SectionHeader
import com.example.dreamfunds.ui.components.list.TransactionItem
import com.example.dreamfunds.ui.components.list.TransactionPaginationFooter
import com.example.dreamfunds.ui.theme.DreamFundsTheme
import com.example.dreamfunds.viewmodel.DashboardUiState
import com.example.dreamfunds.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenDrawer: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddBalanceDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog    by remember { mutableStateOf(false) }
    var showContributeDialog by remember { mutableStateOf(false) }
    var selectedGoal         by remember { mutableStateOf<SavingsGoal?>(null) }
    var fabExpanded          by remember { mutableStateOf(false) }

    // Show skeleton when loading OR when data hasn't arrived yet (no error either)

// AFTER (fixed)
    val showSkeleton = !uiState.isInitialLoadDone
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dashboard",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("Welcome back!", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAll(showSkeleton = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        floatingActionButton = {
            DashboardFab(
                expanded          = fabExpanded,
                onToggle          = { fabExpanded = !fabExpanded },
                onAddBalanceClick = {
                    fabExpanded = false
                    showAddBalanceDialog = true
                },
                onAddExpenseClick = {
                    fabExpanded = false
                    showAddExpenseDialog = true
                },
            )
        },
    ) { padding ->

        // FAB backdrop
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        // ── Skeleton ────────────────────────────────────────────────────────
        if (showSkeleton) {
            DashboardSkeleton(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            )
        } else {
            // ── Real content ───────────────────────────────────────────────
            LazyColumn(
                modifier       = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                item {
                    BalanceSummaryCard(
                        totalBalance = uiState.totalBalance,
                        totalIncome  = uiState.totalIncome,
                        totalExpense = uiState.totalExpense,
                    )
                }

                item {
                    QuickActionButtons(
                        onAddBalanceClick = { showAddBalanceDialog = true },
                        onAddExpenseClick = { showAddExpenseDialog = true },
                    )
                }

                // Error banner
                uiState.error?.let { errorMsg ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                text     = "Error: $errorMsg",
                                modifier = Modifier.padding(12.dp),
                                color    = MaterialTheme.colorScheme.onErrorContainer,
                                style    = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                // ── Goals section ──────────────────────────────────────────
                item {
                    SectionHeader(
                        title       = "Savings Goals",
                        actionLabel = if (uiState.allGoals.isNotEmpty())
                            "${uiState.visibleGoals.size} / ${uiState.allGoals.size} • + New"
                        else "+ New Goal",
                        onAction    = { showAddGoalDialog = true },
                    )
                }

                if (uiState.allGoals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            OutlinedButton(
                                onClick = { showAddGoalDialog = true },
                                shape   = RoundedCornerShape(14.dp),
                            ) {
                                Text("Create your first savings goal")
                            }
                        }
                    }
                } else {
                    item {
                        GoalPaginatedRow(
                            goals          = uiState.visibleGoals,
                            currentBalance = uiState.totalBalance,
                            hasMore        = uiState.hasMoreGoals,
                            totalShown     = uiState.visibleGoals.size,
                            totalCount     = uiState.allGoals.size,
                            onLoadMore     = { viewModel.loadMoreGoals() },
                            onContribute   = { goal ->
                                selectedGoal = goal
                                showContributeDialog = true
                            },
                            onDelete = { goal ->
                                goal.id?.let { viewModel.deleteGoal(it) }
                            },
                        )
                    }
                }

                // ── Transactions section ───────────────────────────────────
                item {
                    SectionHeader(
                        title       = "Recent Activity",
                        actionLabel = if (uiState.allTransactions.isNotEmpty())
                            "${uiState.visibleTransactions.size} / ${uiState.allTransactions.size}"
                        else null,
                    )
                }

                if (uiState.visibleTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No activity yet. Add your balance to get started!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(uiState.visibleTransactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete    = {
                                transaction.id?.let { viewModel.deleteTransaction(it) }
                            },
                        )
                    }
                    item {
                        TransactionPaginationFooter(
                            hasMore    = uiState.hasMoreTransactions,
                            totalShown = uiState.visibleTransactions.size,
                            totalCount = uiState.allTransactions.size,
                            onLoadMore = { viewModel.loadMore() },
                        )
                    }
                }
            }
        }

        // ── Dialogs ─────────────────────────────────────────────────────────
        if (showAddBalanceDialog) {
            AddBalanceDialog(
                onDismiss    = { showAddBalanceDialog = false },
                onAddBalance = { insert ->
                    viewModel.addTransaction(insert)
                    showAddBalanceDialog = false
                },
            )
        }
        if (showAddExpenseDialog) {
            AddExpenseDialog(
                currentBalance = uiState.totalBalance,
                onDismiss      = { showAddExpenseDialog = false },
                onAddExpense   = { insert ->
                    viewModel.addTransaction(insert)
                    showAddExpenseDialog = false
                },
            )
        }
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onAddGoal = { insert ->
                    viewModel.addGoal(insert)
                    showAddGoalDialog = false
                },
            )
        }
        if (showContributeDialog && selectedGoal != null) {
            ContributeGoalDialog(
                goal           = selectedGoal!!,
                currentBalance = uiState.totalBalance,
                onDismiss      = {
                    showContributeDialog = false
                    selectedGoal = null
                },
                onContribute   = { amount ->
                    viewModel.contributeToGoal(selectedGoal!!, amount)
                    showContributeDialog = false
                    selectedGoal = null
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fake data for previews
// ─────────────────────────────────────────────────────────────────────────────

private val fakeGoals = listOf(
    SavingsGoal(
        id           = "1",
        name         = "Emergency Fund",
        targetAmount = 50000.0,
        savedAmount  = 32000.0,
        colorHex     = "#4CAF50",
    ),
    SavingsGoal(
        id           = "2",
        name         = "Japan Trip ✈️",
        targetAmount = 80000.0,
        savedAmount  = 15500.0,
        colorHex     = "#2196F3",
    ),
    SavingsGoal(
        id           = "3",
        name         = "New Laptop 💻",
        targetAmount = 70000.0,
        savedAmount  = 70000.0,
        colorHex     = "#9C27B0",
    ),
)

private val fakeTransactions = listOf(
    Transaction(
        id              = "1",
        title           = "Monthly Salary",
        amount          = 45000.0,
        category        = "Income",
        isIncome        = true,
        transactionDate = "2026-03-01",
    ),
    Transaction(
        id              = "2",
        title           = "Grocery — SM Supermarket",
        amount          = 2340.50,
        category        = "Food",
        isIncome        = false,
        transactionDate = "2026-03-03",
    ),
    Transaction(
        id              = "3",
        title           = "Freelance Project",
        amount          = 12000.0,
        category        = "Income",
        isIncome        = true,
        transactionDate = "2026-03-05",
    ),
    Transaction(
        id              = "4",
        title           = "Meralco Bill",
        amount          = 1850.0,
        category        = "Utilities",
        isIncome        = false,
        transactionDate = "2026-03-06",
    ),
    Transaction(
        id              = "5",
        title           = "Grab Food",
        amount          = 320.0,
        category        = "Food",
        isIncome        = false,
        transactionDate = "2026-03-07",
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Preview shell
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardPreviewShell(uiState: DashboardUiState) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var fabExpanded       by remember { mutableStateOf(false) }

    val showSkeleton = uiState.isLoading ||
            (uiState.allTransactions.isEmpty() &&
                    uiState.allGoals.isEmpty() &&
                    uiState.error == null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dashboard",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("Welcome back!", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        floatingActionButton = {
            DashboardFab(
                expanded          = fabExpanded,
                onToggle          = { fabExpanded = !fabExpanded },
                onAddBalanceClick = { fabExpanded = false },
                onAddExpenseClick = { fabExpanded = false },
            )
        },
    ) { padding ->

        if (showSkeleton) {
            DashboardSkeleton(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                BalanceSummaryCard(
                    totalBalance = uiState.totalBalance,
                    totalIncome  = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                )
            }
            item {
                QuickActionButtons(onAddBalanceClick = {}, onAddExpenseClick = {})
            }

            uiState.error?.let { errorMsg ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            "Error: $errorMsg",
                            modifier = Modifier.padding(12.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            style    = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title       = "Savings Goals",
                    actionLabel = if (uiState.allGoals.isNotEmpty())
                        "${uiState.visibleGoals.size} / ${uiState.allGoals.size} • + New"
                    else "+ New Goal",
                    onAction    = { showAddGoalDialog = true },
                )
            }
            if (uiState.allGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(14.dp)) {
                            Text("Create your first savings goal")
                        }
                    }
                }
            } else {
                item {
                    GoalPaginatedRow(
                        goals          = uiState.visibleGoals,
                        currentBalance = uiState.totalBalance,
                        hasMore        = uiState.hasMoreGoals,
                        totalShown     = uiState.visibleGoals.size,
                        totalCount     = uiState.allGoals.size,
                        onLoadMore     = {},
                        onContribute   = {},
                        onDelete       = {},
                    )
                }
            }

            item {
                SectionHeader(
                    title       = "Recent Activity",
                    actionLabel = if (uiState.allTransactions.isNotEmpty())
                        "${uiState.visibleTransactions.size} / ${uiState.allTransactions.size}"
                    else null,
                )
            }
            if (uiState.visibleTransactions.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No activity yet. Add your balance to get started!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(uiState.visibleTransactions) { transaction ->
                    TransactionItem(transaction = transaction, onDelete = {})
                }
                item {
                    TransactionPaginationFooter(
                        hasMore    = uiState.hasMoreTransactions,
                        totalShown = uiState.visibleTransactions.size,
                        totalCount = uiState.allTransactions.size,
                        onLoadMore = {},
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Dashboard — Skeleton (Loading)")
@Composable
private fun DashboardLoadingPreview() {
    DreamFundsTheme {
        DashboardPreviewShell(uiState = DashboardUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Dashboard — Skeleton (Empty)")
@Composable
private fun DashboardEmptyPreview() {
    DreamFundsTheme {
        DashboardPreviewShell(uiState = DashboardUiState(isLoading = false))
    }
}

@Preview(showBackground = true, name = "Dashboard — With Data")
@Composable
private fun DashboardWithDataPreview() {
    DreamFundsTheme {
        DashboardPreviewShell(
            uiState = DashboardUiState(
                isLoading           = false,
                totalBalance        = 52489.50,
                totalIncome         = 57000.0,
                totalExpense        = 4510.50,
                allGoals            = fakeGoals,
                visibleGoals        = fakeGoals,
                allTransactions     = fakeTransactions,
                visibleTransactions = fakeTransactions,
                hasMoreTransactions = false,
                hasMoreGoals        = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Dashboard — Error")
@Composable
private fun DashboardErrorPreview() {
    DreamFundsTheme {
        DashboardPreviewShell(
            uiState = DashboardUiState(
                isLoading    = false,
                totalBalance = 12000.0,
                totalIncome  = 15000.0,
                totalExpense = 3000.0,
                error        = "Network timeout. Pull to refresh.",
            ),
        )
    }
}

@Preview(showBackground = true, name = "Dashboard — Goals Only")
@Composable
private fun DashboardGoalsOnlyPreview() {
    DreamFundsTheme {
        DashboardPreviewShell(
            uiState = DashboardUiState(
                isLoading    = false,
                totalBalance = 85000.0,
                totalIncome  = 85000.0,
                totalExpense = 0.0,
                allGoals     = fakeGoals,
                visibleGoals = fakeGoals,
                hasMoreGoals = false,
            ),
        )
    }
}