// app/src/main/java/com/example/dreamfunds/data/models/Transaction.kt
package com.example.dreamfunds.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val amount: Double,
    val category: String,
    @SerialName("is_income") val isIncome: Boolean,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("created_at") val createdAt: String? = null
)

// DTO for inserting — user_id must be supplied explicitly for RLS to pass
@Serializable
data class TransactionInsert(
    @SerialName("user_id") val userId: String,
    val title: String,
    val amount: Double,
    val category: String,
    @SerialName("is_income") val isIncome: Boolean,
    @SerialName("transaction_date") val transactionDate: String
)