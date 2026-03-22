// ui/components/AddExpenseDialog.kt
package com.example.dreamfunds.ui.components.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.TransactionInsert
import java.text.SimpleDateFormat
import java.util.*

private const val MAX_EXPENSE = 999_999.99

@Composable
fun AddExpenseDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onAddExpense: (TransactionInsert) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var title  by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }

    val categories = listOf("Food", "Bills", "Travel", "Shopping", "Health", "Others")
    val amountVal         = amount.toDoubleOrNull()
    val isExceeded        = amountVal != null && amountVal > MAX_EXPENSE
    val isZeroOrNeg       = amountVal != null && amountVal <= 0.0
    val isInsufficient    = amountVal != null && amountVal > currentBalance
    val isAmountValid     = amountVal != null && amountVal > 0.0
            && amountVal <= MAX_EXPENSE && amountVal <= currentBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.ShoppingCart, null,
                tint = Color(0xFFF44336), modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Add Expense", fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Available balance chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (currentBalance <= 0.0)
                        Color(0xFFF44336).copy(alpha = 0.1f)
                    else
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Available Balance",
                            style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "₱ ${String.format("%,.2f", currentBalance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentBalance <= 0.0) Color(0xFFF44336) else Color(0xFF4CAF50)
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val filtered = input.filter { c -> c.isDigit() || c == '.' }
                        val dotIndex = filtered.indexOf('.')
                        val valid = if (dotIndex >= 0)
                            filtered.substring(0, dotIndex + 1) +
                                    filtered.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
                        else filtered
                        if (valid.replace(".", "").length <= 9) amount = valid
                    },
                    label   = { Text("Amount") },
                    shape   = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix  = { Text("₱ ") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isExceeded || isZeroOrNeg || isInsufficient,
                    supportingText = {
                        when {
                            isInsufficient -> Text(
                                "Insufficient balance — only ₱${String.format("%,.2f", currentBalance)} available",
                                color = MaterialTheme.colorScheme.error)
                            isExceeded     -> Text("Maximum expense is ₱999,999.99", color = MaterialTheme.colorScheme.error)
                            isZeroOrNeg    -> Text("Amount must be greater than zero", color = MaterialTheme.colorScheme.error)
                            isAmountValid  -> Text(
                                "Remaining after: ₱${String.format("%,.2f", currentBalance - (amountVal ?: 0.0))}",
                                color = Color(0xFF4CAF50))
                            else -> {}
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isExceeded || isZeroOrNeg || isInsufficient)
                            MaterialTheme.colorScheme.error else Color(0xFFF44336),
                        focusedLabelColor  = Color(0xFFF44336)
                    )
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${title.length}/50") },
                    singleLine = true
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick  = { selectedCategory = category },
                            label    = { Text(category) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF44336),
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amountVal ?: return@Button
                    val date  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    onAddExpense(TransactionInsert(
                        userId          = "",
                        title           = title,
                        amount          = value,
                        category        = selectedCategory,
                        isIncome        = false,
                        transactionDate = date
                    ))
                },
                enabled = isAmountValid && title.isNotBlank(),
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Expense")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}