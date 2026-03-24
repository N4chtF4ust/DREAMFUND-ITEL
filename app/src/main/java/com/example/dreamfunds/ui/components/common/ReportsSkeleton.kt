// app/src/main/java/com/example/dreamfunds/ui/components/common/ReportsSkeleton.kt
package com.example.dreamfunds.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Month selector bar
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height   = 52.dp,
            radius   = 16.dp,
        )

        // Income / Expense mini cards
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(modifier = Modifier.weight(1f), height = 76.dp, radius = 16.dp)
            ShimmerBox(modifier = Modifier.weight(1f), height = 76.dp, radius = 16.dp)
        }

        // "Expense Breakdown" title
        ShimmerBox(modifier = Modifier.width(160.dp), height = 20.dp, radius = 6.dp)

        // Breakdown card
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height   = 220.dp,
            radius   = 20.dp,
        )

        // Net insight card
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height   = 72.dp,
            radius   = 16.dp,
        )
    }
}