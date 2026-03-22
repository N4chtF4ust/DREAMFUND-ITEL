// app/src/main/java/com/example/dreamfunds/data/models/SavingsGoal.kt
package com.example.dreamfunds.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavingsGoal(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("saved_amount") val savedAmount: Double,
    @SerialName("color_hex") val colorHex: String = "#4CAF50",
    @SerialName("created_at") val createdAt: String? = null
) {
    /** Parse hex string to Compose Color for UI use */
    fun toComposeColor(): Color {
        return try {
            val hex = colorHex.removePrefix("#")
            Color(android.graphics.Color.parseColor("#$hex"))
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }
    }
}

@Serializable
data class SavingsGoalInsert(
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("saved_amount") val savedAmount: Double,
    @SerialName("color_hex") val colorHex: String = "#4CAF50"
)