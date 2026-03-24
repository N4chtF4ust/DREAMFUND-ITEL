package com.example.dreamfunds.ui.components.cards

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BalanceSummaryCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Dark mode : dark green card  (primaryContainer = Color(0xFF005223))
    // Light mode: branded green card (primary = Color(0xFF1B6B35))
    val cardColor    = if (isDark) MaterialTheme.colorScheme.primaryContainer
    else        MaterialTheme.colorScheme.primary

    // Dark mode : light green text  (onPrimaryContainer = Color(0xFFA6F5B9))
    // Light mode: white text        (onPrimary = Color.White)
    val contentColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer
    else        MaterialTheme.colorScheme.onPrimary

    // Subtle tints for the income/expense icons
    val incomeIconTint  = if (isDark) Color(0xFF8BED9F) else Color(0xFFB9F6CA)
    val expenseIconTint = if (isDark) Color(0xFFFFE082) else Color(0xFFFFD180)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape  = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TOTAL BALANCE",
                color = contentColor.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "₱ ${String.format("%,.2f", totalBalance)}",
                color = contentColor,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceSummaryItem(
                    label        = "Balance Added",
                    amount       = "₱ ${String.format("%,.0f", totalIncome)}",
                    icon         = Icons.Default.ArrowDownward,
                    iconTint     = incomeIconTint,
                    contentColor = contentColor,
                )
                BalanceSummaryItem(
                    label        = "Expenses",
                    amount       = "₱ ${String.format("%,.0f", totalExpense)}",
                    icon         = Icons.Default.ArrowUpward,
                    iconTint     = expenseIconTint,
                    contentColor = contentColor,
                )
            }
        }
    }
}

@Composable
fun BalanceSummaryItem(
    label        : String,
    amount       : String,
    icon         : ImageVector,
    iconTint     : Color,
    contentColor : Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                label,
                color = contentColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                amount,
                color      = contentColor,
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}