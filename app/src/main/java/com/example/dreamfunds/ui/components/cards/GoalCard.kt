// ui/components/GoalCard.kt
package com.example.dreamfunds.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.data.model.SavingsGoal

// Shared fixed height — both GoalCard and LoadMoreGoalCard use this exact value
// so they are always the same height in the LazyRow without IntrinsicSize
// (which crashes on lazy layouts).
private val GOAL_CARD_HEIGHT: Dp = 230.dp
private val GOAL_CARD_WIDTH: Dp  = 180.dp
private val LOAD_MORE_CARD_WIDTH: Dp = 120.dp

// ─────────────────────────────────────────────────────────────────────────────
// GoalPaginatedRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GoalPaginatedRow(
    goals: List<SavingsGoal>,
    currentBalance: Double,
    hasMore: Boolean,
    totalShown: Int,
    totalCount: Int,
    onLoadMore: () -> Unit,
    onContribute: (SavingsGoal) -> Unit,
    onDelete: (SavingsGoal) -> Unit
) {
    val listState = rememberLazyListState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(goals, key = { it.id ?: it.name }) { goal ->
                GoalCard(
                    goal           = goal,
                    currentBalance = currentBalance,
                    onContribute   = { onContribute(goal) },
                    onDelete       = { onDelete(goal) }
                )
            }

            if (hasMore) {
                item {
                    LoadMoreGoalCard(
                        remaining = totalCount - totalShown,
                        onClick   = onLoadMore
                    )
                }
            }
        }

        GoalPageIndicator(
            totalShown = totalShown,
            totalCount = totalCount
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GoalPageIndicator — dot row below the LazyRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GoalPageIndicator(totalShown: Int, totalCount: Int) {
    if (totalCount <= 1) return
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isVisible = index < totalShown
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isVisible) 7.dp else 5.dp)
                    .background(
                        color = if (isVisible)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LoadMoreGoalCard
// Uses the same GOAL_CARD_HEIGHT constant so it always matches GoalCard exactly.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoadMoreGoalCard(
    remaining: Int,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier
            .width(LOAD_MORE_CARD_WIDTH)
            .height(GOAL_CARD_HEIGHT),          // ← same constant as GoalCard
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Load more goals",
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text       = "+$remaining more",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = "goal${if (remaining != 1) "s" else ""}",
                style     = MaterialTheme.typography.labelSmall,
                color     = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick        = onClick,
                shape          = RoundedCornerShape(10.dp),
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                border         = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Show", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GoalCard — single card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GoalCard(
    goal: SavingsGoal,
    currentBalance: Double,
    onContribute: () -> Unit,
    onDelete: () -> Unit
) {
    val color       = goal.toComposeColor()
    val progress    = (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    val isCompleted = goal.savedAmount >= goal.targetAmount

    Card(
        modifier  = Modifier
            .width(GOAL_CARD_WIDTH)
            .height(GOAL_CARD_HEIGHT),          // ← same constant as LoadMoreGoalCard
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                color.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column {
                // Header: status icon + delete button
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(36.dp)
                            .background(color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                            null, tint = color, modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close, contentDescription = "Delete goal",
                            tint = Color.Gray, modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    goal.name,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.bodyMedium,
                    maxLines   = 1
                )
                Text(
                    "₱${String.format("%,.0f", goal.savedAmount)} / ₱${String.format("%,.0f", goal.targetAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress   = { progress },
                    modifier   = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color      = color,
                    trackColor = color.copy(alpha = 0.15f)
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isCompleted) "Done! 🎉"
                        else "₱${String.format("%,.0f", goal.targetAmount - goal.savedAmount)} left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) color else Color.Gray
                    )
                }
            }

            // Bottom: Contribute button always pinned to bottom
            Button(
                onClick        = onContribute,
                modifier       = Modifier.fillMaxWidth().height(32.dp),
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors         = ButtonDefaults.buttonColors(
                    containerColor         = if (isCompleted || currentBalance <= 0.0) Color.Gray else color,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                ),
                enabled = !isCompleted && currentBalance > 0.0
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    if (isCompleted) "Completed" else "Contribute",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}