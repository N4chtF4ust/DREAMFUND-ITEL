package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Full-screen overlay shown whenever the device loses internet connectivity.
 * It is dismissed automatically by [DreamFundsApp] the moment connectivity
 * is restored — no manual action required.
 *
 * [onRetry] is called when the user taps the button; the overlay itself will
 * auto-hide as soon as the network flow emits `true` again.
 */
@Composable
fun NoConnectionScreen(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.WifiOff,
                    contentDescription = "No internet",
                    modifier           = Modifier.size(72.dp),
                    tint               = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text      = "No Internet Connection",
                    style     = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text      = "Please check your connection.\nThis screen will dismiss automatically once you're back online.",
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick  = onRetry,
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text("Retry")
                }
            }
        }
    }
}