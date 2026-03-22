// app/src/main/java/com/example/dreamfunds/data/repository/TransactionRepository.kt
package com.example.dreamfunds.data.repository

import com.example.dreamfunds.SupabaseClientProvider
import com.example.dreamfunds.data.model.Transaction
import com.example.dreamfunds.data.model.TransactionInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class TransactionRepository {

    private val client = SupabaseClientProvider.client
    private val db = client.postgrest
    private val auth = client.auth

    private fun currentUserId(): String =
        auth.currentUserOrNull()?.id
            ?: throw Exception("Not logged in — cannot perform this action.")

    /** Fetch all transactions for the logged-in user, newest first. */
    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val transactions = db["transactions"]
                .select(Columns.ALL) {
                    order("transaction_date", Order.DESCENDING)
                }
                .decodeList<Transaction>()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Insert a new transaction.
     * The insert DTO already carries user_id so the RLS
     * `with check (auth.uid() = user_id)` policy is satisfied.
     */
    suspend fun addTransaction(insert: TransactionInsert): Result<Transaction> {
        return try {
            // Attach the current user's ID right before sending
            val withUser = insert.copy(userId = currentUserId())
            val result = db["transactions"]
                .insert(withUser) { select() }
                .decodeSingle<Transaction>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete a transaction by ID. */
    suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            db["transactions"].delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}