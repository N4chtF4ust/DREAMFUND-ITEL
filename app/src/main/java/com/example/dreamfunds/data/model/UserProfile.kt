// app/src/main/java/com/example/dreamfunds/data/models/UserProfile.kt
package com.example.dreamfunds.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String = "",
    val email: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)