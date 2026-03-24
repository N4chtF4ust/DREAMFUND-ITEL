package com.example.dreamfunds.data.repository

import com.example.dreamfunds.SupabaseClientProvider
import com.example.dreamfunds.data.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.exceptions.RestException

class AuthRepository {

    private val client = SupabaseClientProvider.client
    private val auth   = client.auth
    private val db     = client.postgrest

    /** Sign up with email + password. */
    suspend fun register(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            val user = auth.signUpWith(Email, redirectUrl = "dreamfunds://auth/callback") {
                this.email = email
                this.password = password
                data = kotlinx.serialization.json.buildJsonObject {
                    put("full_name", kotlinx.serialization.json.JsonPrimitive(fullName))
                }
            }

            // If identities is null or empty, email is already taken
            if (user?.identities.isNullOrEmpty()) {
                return Result.failure(Exception("An account with this email already exists."))
            }

            Result.success(Unit)
        } catch (e: RestException) {
            Result.failure(Exception(e.error ?: "Registration failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign in with email + password. */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign out the current user. */
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = auth.currentSessionOrNull() != null

    fun isEmailConfirmed(): Boolean {
        val user = auth.currentUserOrNull() ?: return false
        return user.emailConfirmedAt != null
    }

    /** Fetch the current user's profile row. */
    suspend fun getCurrentProfile(): Result<UserProfile> {
        return try {
            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))
            val profile = db["profiles"]
                .select(Columns.ALL) {
                    filter { eq("id", userId) }
                    limit(1)
                    single()
                }
                .decodeAs<UserProfile>()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update the user's full name and avatar URL in the profiles table.
     * Changes take effect immediately with no confirmation needed.
     */
    suspend fun updateProfile(fullName: String, avatarUrl: String?): Result<Unit> {
        return try {
            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))

            // Build the map of fields we want to update
            val updates = mutableMapOf<String, String>()
            updates["full_name"] = fullName

            // Only add the avatar_url to the update if it's not null
            if (avatarUrl != null) {
                updates["avatar_url"] = avatarUrl
            }

            db["profiles"].update(updates) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Request an email address change via Supabase Auth.
     *
     * Supabase sends a confirmation link to the NEW email address.
     * The email in auth.users is only updated after the user clicks
     * the link — the profiles table is NOT updated here; it should be
     * synced via a Supabase database trigger or updated after confirmation.
     *
     * redirectUrl ensures the confirmation link opens the app.
     */
    suspend fun requestEmailChange(newEmail: String): Result<Unit> {
        return try {
            auth.updateUser(redirectUrl = "dreamfunds://auth/callback") {
                email = newEmail
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Change the current user's password. */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isEmailUser(): Boolean {
        val identities = auth.currentUserOrNull()?.identities
        return identities?.any { it.provider == "email" } == true
    }
}