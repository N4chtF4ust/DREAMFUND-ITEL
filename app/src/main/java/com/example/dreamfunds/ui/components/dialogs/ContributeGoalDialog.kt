// ui/components/ContributeGoalDialog.kt
package com.example.dreamfunds.ui.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.SavingsGoal

@Composable
fun ContributeGoalDialog(
    goal: SavingsGoal,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onContribute: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val color     = goal.toComposeColor()
    val remaining = goal.targetAmount - goal.savedAmount

    val amountVal      = amount.toDoubleOrNull()
    val isInsufficient = amountVal != null && amountVal > currentBalance
    val isOverGoal     = amountVal != null && amountVal > remaining
    val isZeroOrNeg    = amountVal != null && amountVal <= 0.0
    val isValid        = amountVal != null && amountVal > 0.0
            && amountVal <= currentBalance && amountVal <= remaining

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.AccountBalanceWallet, null,
                tint = color, modifier = Modifier.size(32.dp))
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Contribute to Goal", fontWeight = FontWeight.Bold)
                Text(goal.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Goal progress summary
                Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saved so far", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₱${String.format("%,.2f", goal.savedAmount)}",
                                fontWeight = FontWeight.Bold, color = color)
                        }
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Still needed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₱${String.format("%,.2f", remaining)}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = color, trackColor = color.copy(alpha = 0.15f)
                        )
                    }
                }

                // Current balance row
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("₱${String.format("%,.2f", currentBalance)}",
                        fontWeight = FontWeight.Bold,
                        color = if (currentBalance <= 0.0) Color(0xFFF44336) else Color(0xFF4CAF50))
                }

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val filtered = input.filter { c -> c.isDigit() || c == '.' }
                        val dot = filtered.indexOf('.')
                        val valid = if (dot >= 0)
                            filtered.substring(0, dot + 1) +
                                    filtered.substring(dot + 1).filter { it.isDigit() }.take(2)
                        else filtered
                        if (valid.replace(".", "").length <= 9) amount = valid
                    },
                    label   = { Text("Amount to Contribute") },
                    shape   = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix  = { Text("₱ ") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isInsufficient || isOverGoal || isZeroOrNeg,
                    supportingText = {
                        when {
                            isInsufficient -> Text("Insufficient balance", color = MaterialTheme.colorScheme.error)
                            isOverGoal     -> Text("Exceeds remaining (₱${String.format("%,.2f", remaining)})",
                                color = MaterialTheme.colorScheme.error)
                            isZeroOrNeg    -> Text("Amount must be greater than zero", color = MaterialTheme.colorScheme.error)
                            isValid        -> Text("Balance after: ₱${String.format("%,.2f", currentBalance - (amountVal ?: 0.0))}",
                                color = color)
                            else -> {}
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isInsufficient || isOverGoal || isZeroOrNeg)
                            MaterialTheme.colorScheme.error else color,
                        focusedLabelColor  = color
                    ),
                    singleLine = true
                )

                // Quick-fill: 25% / 50% / Full
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.25 to "25%", 0.5 to "50%", 1.0 to "Full").forEach { (fraction, label) ->
                        val quickAmt = (remaining * fraction).coerceAtMost(currentBalance)
                        if (quickAmt > 0) {
                            OutlinedButton(
                                onClick = { amount = String.format("%.2f", quickAmt) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, color)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onContribute(amountVal ?: return@Button) },
                enabled = isValid,
                colors  = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Contribute")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}