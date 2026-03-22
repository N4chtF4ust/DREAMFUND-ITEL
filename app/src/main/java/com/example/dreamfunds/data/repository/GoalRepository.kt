// app/src/main/java/com/example/dreamfunds/data/repository/GoalRepository.kt
package com.example.dreamfunds.data.repository

import com.example.dreamfunds.SupabaseClientProvider
import com.example.dreamfunds.data.model.SavingsGoal
import com.example.dreamfunds.data.model.SavingsGoalInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class GoalRepository {

    private val client = SupabaseClientProvider.client
    private val db = client.postgrest
    private val auth = client.auth

    private fun currentUserId(): String =
        auth.currentUserOrNull()?.id
            ?: throw Exception("Not logged in — cannot perform this action.")

    /** Fetch all savings goals for the logged-in user. */
    suspend fun getGoals(): Result<List<SavingsGoal>> {
        return try {
            val goals = db["savings_goals"]
                .select(Columns.ALL)
                .decodeList<SavingsGoal>()
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Insert a new savings goal, attaching the current user's ID. */
    suspend fun addGoal(insert: SavingsGoalInsert): Result<SavingsGoal> {
        return try {
            val withUser = insert.copy(userId = currentUserId())
            val result = db["savings_goals"]
                .insert(withUser) { select() }
                .decodeSingle<SavingsGoal>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update saved amount for a goal. */
    suspend fun updateSavedAmount(id: String, newAmount: Double): Result<Unit> {
        return try {
            db["savings_goals"].update(
                mapOf("saved_amount" to newAmount)
            ) {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete a goal by ID. */
    suspend fun deleteGoal(id: String): Result<Unit> {
        return try {
            db["savings_goals"].delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}