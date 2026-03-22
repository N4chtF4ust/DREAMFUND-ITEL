// ui/components/TransactionItem.kt
package com.example.dreamfunds.ui.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.Transaction

// ─────────────────────────────────────────────────────────────
// Single transaction row
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(transaction.title, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Text("${transaction.category} • ${transaction.transactionDate}")
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (transaction.isIncome) "+ ₱ " else "- ₱ ") +
                            String.format("%,.2f", transaction.amount),
                    color = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(
                        Icons.Default.Delete, null,
                        tint = Color.Gray, modifier = Modifier.size(14.dp)
                    )
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(transaction.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// Pagination footer — "Load More" or "All caught up" indicator
// ─────────────────────────────────────────────────────────────
@Composable
fun TransactionPaginationFooter(
    hasMore: Boolean,
    totalShown: Int,
    totalCount: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress indicator: e.g. "Showing 10 of 24 transactions"
        Text(
            text  = "Showing $totalShown of $totalCount transactions",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        // Thin progress bar showing how far through the list we are
        LinearProgressIndicator(
            progress = {
                if (totalCount == 0) 0f
                else (totalShown.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp),
            color      = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (hasMore) {
            // Load More button
            OutlinedButton(
                onClick = onLoadMore,
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown, null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load More")
            }
        } else if (totalCount > 0) {
            // All loaded indicator
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "All transactions loaded",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Category → icon mapping
// ─────────────────────────────────────────────────────────────
fun categoryIcon(category: String): ImageVector = when (category) {
    "Food"     -> Icons.Default.Restaurant
    "Travel"   -> Icons.Default.DirectionsCar
    "Bills"    -> Icons.Default.ReceiptLong
    "Income"   -> Icons.Default.AccountBalanceWallet
    "Shopping" -> Icons.Default.ShoppingBag
    "Health"   -> Icons.Default.LocalHospital
    else       -> Icons.Default.Category
}