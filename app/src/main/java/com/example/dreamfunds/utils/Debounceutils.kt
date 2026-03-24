package com.example.dreamfunds.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Returns a debounced version of [block] that can only fire once
 * every [delayMs] milliseconds. Any calls during the cooldown are silently dropped.
 *
 * Usage:
 *   val onBack = rememberDebouncedAction { navController.popBackStack() }
 *   IconButton(onClick = onBack) { ... }
 */
@Composable
fun rememberDebouncedAction(
    delayMs: Long = 500L,
    block: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    // `enabled` tracks whether the action is ready to fire again
    var enabled by remember { mutableStateOf(true) }

    return remember(block) {
        {
            if (enabled) {
                enabled = false
                block()
                scope.launch {
                    block()
                    delay(delayMs)
                    enabled = true
                }
            }
        }
    }
}

/**
 * Non-composable variant for use inside ViewModels or plain coroutine scopes.
 */
fun CoroutineScope.debouncedAction(
    delayMs: Long = 500L,
    block: () -> Unit,
): () -> Unit {
    var enabled = true
    return {
        if (enabled) {
            enabled = false
            block()
            launch {
                delay(delayMs)
                enabled = true
            }
        }
    }
}