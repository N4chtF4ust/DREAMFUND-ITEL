// app/src/main/java/com/example/dreamfunds/ui/components/common/DashboardSkeleton.kt
package com.example.dreamfunds.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ── Balance card ────────────────────────────────────────────────
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            height   = 160.dp,
            radius   = 28.dp,
        )

        // ── Quick action buttons ─────────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(modifier = Modifier.weight(1f), height = 48.dp, radius = 14.dp)
            ShimmerBox(modifier = Modifier.weight(1f), height = 48.dp, radius = 14.dp)
        }

        // ── Section header — Goals ───────────────────────────────────────
        SkeletonSectionHeader()

        // ── Goals row — 2 cards ──────────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier.weight(1f),
                    height   = 130.dp,
                    radius   = 16.dp,
                )
            }
        }

        // ── Section header — Activity ────────────────────────────────────
        SkeletonSectionHeader()

        // ── Transaction rows ─────────────────────────────────────────────
        repeat(5) { SkeletonTransactionRow() }
    }
}

@Composable
private fun SkeletonSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ShimmerBox(modifier = Modifier.width(120.dp), height = 18.dp)
        ShimmerBox(modifier = Modifier.width(60.dp),  height = 18.dp)
    }
}

@Composable
private fun SkeletonTransactionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar / icon circle
        ShimmerBox(
            modifier = Modifier.size(44.dp),
            height   = 44.dp,
            radius   = 22.dp,
        )
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.55f), height = 14.dp)
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f), height = 12.dp)
        }
        ShimmerBox(modifier = Modifier.width(64.dp), height = 14.dp)
    }
}