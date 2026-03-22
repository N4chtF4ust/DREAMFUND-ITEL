// app/src/main/java/com/example/dreamfunds/ui/screens/ReportsScreen.kt
package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamfunds.viewmodel.ReportItem
import com.example.dreamfunds.viewmodel.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onOpenDrawer: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadReports() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Month Selector ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
                    }
                    Text(
                        uiState.displayMonth,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick  = { viewModel.goToNextMonth() },
                        enabled  = uiState.monthOffset < 0
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Next month",
                            tint = if (uiState.monthOffset < 0)
                                LocalContentColor.current
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Income vs Expenses mini cards ───────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(
                    modifier       = Modifier.weight(1f),
                    label          = "Income",
                    amount         = uiState.totalIncome,
                    icon           = Icons.Default.ArrowDownward,
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                    contentColor   = Color(0xFF2E7D32)
                )
                SummaryMiniCard(
                    modifier       = Modifier.weight(1f),
                    label          = "Expenses",
                    amount         = uiState.totalSpent,
                    icon           = Icons.Default.ArrowUpward,
                    containerColor = Color(0xFFF44336).copy(alpha = 0.12f),
                    contentColor   = Color(0xFFC62828)
                )
            }

            // ── No data state ───────────────────────────────────
            if (uiState.filteredExpenses.isEmpty() && uiState.totalIncome == 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart, null,
                            modifier = Modifier.size(48.dp), tint = Color.Gray
                        )
                        Text(
                            "No transactions in ${uiState.displayMonth}",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Use ← to navigate to a month with data",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {

                // ── Expense Breakdown card ──────────────────────
                if (uiState.filteredExpenses.isNotEmpty()) {
                    Text(
                        "Expense Breakdown",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier            = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            uiState.reportItems
                                .filter { it.rawAmount > 0.0 }
                                .sortedByDescending { it.rawAmount }
                                .forEach { ReportProgressItem(it) }

                            val zeroes = uiState.reportItems.filter { it.rawAmount == 0.0 }
                            if (zeroes.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                zeroes.forEach { item ->
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray)
                                        Text("₱ 0.00",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Net insight card ────────────────────────────
                val net        = uiState.totalIncome - uiState.totalSpent
                val isPositive = net >= 0.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (isPositive)
                            Color(0xFF4CAF50).copy(alpha = 0.12f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp
                            else Icons.Default.TrendingDown,
                            null,
                            tint     = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isPositive) "You saved money this month! 🎉"
                                else "Expenses exceeded income this month",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                "Net: ${if (isPositive) "+" else ""}₱${String.format("%,.2f", net)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryMiniCard(
    modifier       : Modifier = Modifier,
    label          : String,
    amount         : Double,
    icon           : ImageVector,
    containerColor : Color,
    contentColor   : Color
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "₱ ${String.format("%,.2f", amount)}",
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color      = contentColor
            )
        }
    }
}

@Composable
fun ReportProgressItem(item: ReportItem) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(item.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.amount, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "${(item.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = item.color
                )
            }
        }
        LinearProgressIndicator(
            progress    = { item.percentage },
            modifier    = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color       = item.color,
            trackColor  = item.color.copy(alpha = 0.1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Green preview theme (consistent across all screens)
// ─────────────────────────────────────────────────────────────
private val GreenColorScheme = lightColorScheme(
    primary          = Color(0xFF2E7D32),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    secondary        = Color(0xFF388E3C),
    onSecondary      = Color.White,
    tertiary         = Color(0xFF81C784),
    background       = Color(0xFFF1F8E9),
    surface          = Color(0xFFF1F8E9),
    error            = Color(0xFFB00020),
    onBackground     = Color(0xFF1B1B1B),
    onSurface        = Color(0xFF1B1B1B),
)

@Composable
private fun DreamFundsPreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GreenColorScheme, content = content)
}

// ─────────────────────────────────────────────────────────────
// Fake data helpers for previews
// ─────────────────────────────────────────────────────────────
private val fakeReportItems = listOf(
    ReportItem(
        name       = "Food & Dining",
        amount     = "₱ 3,200.00",
        rawAmount  = 3200.0,
        percentage = 0.52f,
        color      = Color(0xFFF44336)
    ),
    ReportItem(
        name       = "Transportation",
        amount     = "₱ 1,500.00",
        rawAmount  = 1500.0,
        percentage = 0.24f,
        color      = Color(0xFF2196F3)
    ),
    ReportItem(
        name       = "Utilities",
        amount     = "₱ 850.00",
        rawAmount  = 850.0,
        percentage = 0.14f,
        color      = Color(0xFFFF9800)
    ),
    ReportItem(
        name       = "Others",
        amount     = "₱ 610.00",
        rawAmount  = 610.0,
        percentage = 0.10f,
        color      = Color(0xFF9C27B0)
    ),
    ReportItem(
        name       = "Entertainment",
        amount     = "₱ 0.00",
        rawAmount  = 0.0,
        percentage = 0.0f,
        color      = Color(0xFF00BCD4)
    ),
)

// ─────────────────────────────────────────────────────────────
// Stateless UI — used exclusively by previews.
// ReportsScreen above is NOT changed.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsScreenStateless(
    displayMonth     : String,
    monthOffset      : Int,
    totalIncome      : Double,
    totalSpent       : Double,
    reportItems      : List<ReportItem>,
    filteredExpenses : List<ReportItem>,
    isLoading        : Boolean,
    onPreviousMonth  : () -> Unit,
    onNextMonth      : () -> Unit,
    onOpenDrawer     : () -> Unit,
    onRefresh        : () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier            = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Month Selector ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
                    }
                    Text(
                        displayMonth,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onNextMonth, enabled = monthOffset < 0) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Next month",
                            tint = if (monthOffset < 0)
                                LocalContentColor.current
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Income vs Expenses mini cards ───────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(
                    modifier       = Modifier.weight(1f),
                    label          = "Income",
                    amount         = totalIncome,
                    icon           = Icons.Default.ArrowDownward,
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                    contentColor   = Color(0xFF2E7D32)
                )
                SummaryMiniCard(
                    modifier       = Modifier.weight(1f),
                    label          = "Expenses",
                    amount         = totalSpent,
                    icon           = Icons.Default.ArrowUpward,
                    containerColor = Color(0xFFF44336).copy(alpha = 0.12f),
                    contentColor   = Color(0xFFC62828)
                )
            }

            // ── No data state ───────────────────────────────────
            if (filteredExpenses.isEmpty() && totalIncome == 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BarChart, null,
                            modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("No transactions in $displayMonth",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray, textAlign = TextAlign.Center)
                        Text("Use ← to navigate to a month with data",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center)
                    }
                }
            } else {
                // ── Expense Breakdown ───────────────────────────
                if (filteredExpenses.isNotEmpty()) {
                    Text("Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier            = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            reportItems
                                .filter { it.rawAmount > 0.0 }
                                .sortedByDescending { it.rawAmount }
                                .forEach { ReportProgressItem(it) }

                            val zeroes = reportItems.filter { it.rawAmount == 0.0 }
                            if (zeroes.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                zeroes.forEach { item ->
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray)
                                        Text("₱ 0.00",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Net insight card ────────────────────────────
                val net        = totalIncome - totalSpent
                val isPositive = net >= 0.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (isPositive)
                            Color(0xFF4CAF50).copy(alpha = 0.12f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp
                            else Icons.Default.TrendingDown,
                            null,
                            tint     = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isPositive) "You saved money this month! 🎉"
                                else "Expenses exceeded income this month",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                "Net: ${if (isPositive) "+" else ""}₱${String.format("%,.2f", net)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Reports — With Data")
@Composable
private fun ReportsWithDataPreview() {
    DreamFundsPreviewTheme {
        ReportsScreenStateless(
            displayMonth     = "March 2026",
            monthOffset      = 0,
            totalIncome      = 45000.0,
            totalSpent       = 6160.0,
            reportItems      = fakeReportItems,
            filteredExpenses = fakeReportItems.filter { it.rawAmount > 0 },
            isLoading        = false,
            onPreviousMonth  = {},
            onNextMonth      = {},
            onOpenDrawer     = {},
            onRefresh        = {}
        )
    }
}

@Preview(showBackground = true, name = "Reports — Empty Month")
@Composable
private fun ReportsEmptyPreview() {
    DreamFundsPreviewTheme {
        ReportsScreenStateless(
            displayMonth     = "January 2026",
            monthOffset      = -2,
            totalIncome      = 0.0,
            totalSpent       = 0.0,
            reportItems      = emptyList(),
            filteredExpenses = emptyList(),
            isLoading        = false,
            onPreviousMonth  = {},
            onNextMonth      = {},
            onOpenDrawer     = {},
            onRefresh        = {}
        )
    }
}

@Preview(showBackground = true, name = "Reports — Over Budget")
@Composable
private fun ReportsOverBudgetPreview() {
    DreamFundsPreviewTheme {
        ReportsScreenStateless(
            displayMonth     = "February 2026",
            monthOffset      = -1,
            totalIncome      = 15000.0,
            totalSpent       = 18500.0,
            reportItems      = fakeReportItems,
            filteredExpenses = fakeReportItems.filter { it.rawAmount > 0 },
            isLoading        = false,
            onPreviousMonth  = {},
            onNextMonth      = {},
            onOpenDrawer     = {},
            onRefresh        = {}
        )
    }
}

@Preview(showBackground = true, name = "Reports — Loading")
@Composable
private fun ReportsLoadingPreview() {
    DreamFundsPreviewTheme {
        ReportsScreenStateless(
            displayMonth     = "March 2026",
            monthOffset      = 0,
            totalIncome      = 0.0,
            totalSpent       = 0.0,
            reportItems      = emptyList(),
            filteredExpenses = emptyList(),
            isLoading        = true,
            onPreviousMonth  = {},
            onNextMonth      = {},
            onOpenDrawer     = {},
            onRefresh        = {}
        )
    }
}