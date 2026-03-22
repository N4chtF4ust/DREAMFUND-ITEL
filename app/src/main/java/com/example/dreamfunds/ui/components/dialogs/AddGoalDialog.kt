// ui/components/AddGoalDialog.kt
package com.example.dreamfunds.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.SavingsGoalInsert

private val GOAL_COLOR_OPTIONS = listOf(
    "#4CAF50" to Color(0xFF4CAF50),
    "#2196F3" to Color(0xFF2196F3),
    "#FF9800" to Color(0xFFFF9800),
    "#9C27B0" to Color(0xFF9C27B0),
    "#F44336" to Color(0xFFF44336),
    "#00BCD4" to Color(0xFF00BCD4)
)

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAddGoal: (SavingsGoalInsert) -> Unit
) {
    var name          by remember { mutableStateOf("") }
    var targetAmount  by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    val targetVal    = targetAmount.toDoubleOrNull()
    val isTargetValid = targetVal != null && targetVal > 0.0 && targetVal <= 999_999.99

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Star, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        },
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. New Phone, Vacation…") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${name.length}/30") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { input ->
                        val filtered = input.filter { c -> c.isDigit() || c == '.' }
                        val dot = filtered.indexOf('.')
                        val valid = if (dot >= 0)
                            filtered.substring(0, dot + 1) +
                                    filtered.substring(dot + 1).filter { it.isDigit() }.take(2)
                        else filtered
                        if (valid.replace(".", "").length <= 9) targetAmount = valid
                    },
                    label = { Text("Target Amount") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₱ ") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = targetAmount.isNotBlank() && !isTargetValid,
                    supportingText = {
                        if (targetAmount.isNotBlank() && !isTargetValid)
                            Text("Enter a valid amount (max ₱999,999.99)",
                                color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true
                )
                Text("Goal Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GOAL_COLOR_OPTIONS.forEach { (hex, color) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(Icons.Default.Check, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Surface(
                                modifier = Modifier.matchParentSize(),
                                color    = Color.Transparent,
                                shape    = CircleShape,
                                onClick  = { selectedColor = hex }
                            ) {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetVal ?: return@Button
                    onAddGoal(SavingsGoalInsert(
                        userId       = "",
                        name         = name.trim(),
                        targetAmount = target,
                        savedAmount  = 0.0,
                        colorHex     = selectedColor
                    ))
                },
                enabled = name.isNotBlank() && isTargetValid
            ) { Text("Create Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}