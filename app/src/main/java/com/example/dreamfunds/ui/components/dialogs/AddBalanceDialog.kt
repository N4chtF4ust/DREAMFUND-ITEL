// ui/components/AddBalanceDialog.kt
package com.example.dreamfunds.ui.components.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.TransactionInsert
import java.text.SimpleDateFormat
import java.util.*

private const val MAX_AMOUNT = 999_999.99

@Composable
fun AddBalanceDialog(
    onDismiss: () -> Unit,
    onAddBalance: (TransactionInsert) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note   by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf("Salary") }

    val sources   = listOf("Salary", "Freelance", "Gift", "Investment", "Refund", "Other")
    val amountVal = amount.toDoubleOrNull()
    val isExceeded   = amountVal != null && amountVal > MAX_AMOUNT
    val isZeroOrNeg  = amountVal != null && amountVal <= 0.0
    val isAmountValid = amountVal != null && amountVal > 0.0 && amountVal <= MAX_AMOUNT

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.AccountBalanceWallet, null,
                tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Add Balance", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val filtered  = input.filter { c -> c.isDigit() || c == '.' }
                        val dotIndex  = filtered.indexOf('.')
                        val valid = if (dotIndex >= 0)
                            filtered.substring(0, dotIndex + 1) +
                                    filtered.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
                        else filtered
                        if (valid.replace(".", "").length <= 9) amount = valid
                    },
                    label   = { Text("Amount to Add") },
                    shape   = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix  = { Text("₱ ") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isExceeded || isZeroOrNeg,
                    supportingText = {
                        when {
                            isExceeded   -> Text("Maximum top-up is ₱999,999.99", color = MaterialTheme.colorScheme.error)
                            isZeroOrNeg  -> Text("Amount must be greater than zero", color = MaterialTheme.colorScheme.error)
                            isAmountValid -> Text("Adding ₱ ${String.format("%,.2f", amountVal)}", color = Color(0xFF4CAF50))
                            else -> {}
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isExceeded || isZeroOrNeg)
                            MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                        focusedLabelColor  = Color(0xFF4CAF50)
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 50) note = it },
                    label = { Text("Note (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${note.length}/50") },
                    singleLine = true
                )
                Text("Source", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { source ->
                        FilterChip(
                            selected = selectedSource == source,
                            onClick  = { selectedSource = source },
                            label    = { Text(source) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50),
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
                    val title = if (note.isNotBlank()) note else selectedSource
                    onAddBalance(TransactionInsert(
                        userId          = "",
                        title           = title,
                        amount          = value,
                        category        = "Income",
                        isIncome        = true,
                        transactionDate = date
                    ))
                },
                enabled = isAmountValid,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Balance")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}