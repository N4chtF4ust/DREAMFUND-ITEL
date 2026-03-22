package com.example.dreamfunds.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamfunds.SupabaseClientProvider
import com.example.dreamfunds.data.model.UserProfile
import com.example.dreamfunds.data.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthSessionMissingException
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle                      : AuthState()
    object Loading                   : AuthState()
    object Success                   : AuthState()
    object AwaitingEmailConfirmation : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class SessionState {
    object Loading   : SessionState()
    object LoggedIn  : SessionState()
    object LoggedOut : SessionState()
}

class AuthViewModel : ViewModel() {

    private val repo   = AuthRepository()
    private val client = SupabaseClientProvider.client

    private val _authState        = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _sessionState     = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _profile          = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _saveProfileState = MutableStateFlow<AuthState>(AuthState.Idle)
    val saveProfileState: StateFlow<AuthState> = _saveProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val changePasswordState: StateFlow<AuthState> = _changePasswordState.asStateFlow()

    private val _changeEmailState = MutableStateFlow<AuthState>(AuthState.Idle)
    val changeEmailState: StateFlow<AuthState> = _changeEmailState.asStateFlow()

    // Version counter for avatar cache busting
    private val _profileUpdateVersion = MutableStateFlow(0)
    val profileUpdateVersion: StateFlow<Int> = _profileUpdateVersion.asStateFlow()

    init {
        viewModelScope.launch {
            client.auth.sessionStatus
                .catch { e ->
                    if (e is AuthSessionMissingException) {
                        _sessionState.value = SessionState.LoggedOut
                    } else throw e
                }
                .collect { status ->
                    _sessionState.value = when (status) {
                        is SessionStatus.Authenticated    -> SessionState.LoggedIn
                        is SessionStatus.NotAuthenticated -> SessionState.LoggedOut
                        is SessionStatus.Initializing     -> SessionState.Loading
                        is SessionStatus.RefreshFailure   -> {
                            if (repo.isLoggedIn()) SessionState.LoggedIn
                            else SessionState.LoggedOut
                        }
                    }
                }
        }
    }

    fun isLoggedIn(): Boolean = repo.isLoggedIn()

    fun onGoogleSignInSuccess() { _authState.value = AuthState.Success }
    fun onGoogleSignInError(message: String) { _authState.value = AuthState.Error(message) }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.login(email, password)
            if (result.isSuccess) {
                if (repo.isEmailConfirmed()) {
                    _authState.value = AuthState.Success
                } else {
                    repo.logout()
                    _authState.value = AuthState.Error(
                        "Please confirm your email before logging in. Check your inbox."
                    )
                }
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.register(fullName, email, password)
            _authState.value = if (result.isSuccess)
                AuthState.AwaitingEmailConfirmation
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            _authState.value = AuthState.Idle
            _profile.value   = null
            onLoggedOut()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val result = repo.getCurrentProfile()
            if (result.isSuccess) _profile.value = result.getOrNull()
        }
    }

    /**
     * Save profile changes.
     * - Uploads a new avatar if provided.
     * - Updates the database with the new name and avatar URL.
     * - If email changed, sends a confirmation link and sets state to AwaitingEmailConfirmation.
     */
    fun saveProfile(fullName: String, newEmail: String, avatarBytes: ByteArray? = null) {
        viewModelScope.launch {
            _saveProfileState.value = AuthState.Loading

            val currentEmail = _profile.value?.email ?: ""
            val emailChanged = newEmail.isNotBlank() && newEmail != currentEmail

            var newAvatarUrl = _profile.value?.avatarUrl

            // 1. Upload Avatar if provided
            if (avatarBytes != null) {
                try {
                    val userId = client.auth.currentUserOrNull()?.id
                        ?: throw Exception("User not logged in")

                    // 👇 Unique filename per upload — guarantees a new public URL every time
                    val timestamp = System.currentTimeMillis()
                    val fileName = "${userId}_${timestamp}.jpg"
                    val bucket = client.storage["avatars"]
                    bucket.upload(fileName, avatarBytes) { upsert = false }
                    newAvatarUrl = bucket.publicUrl(fileName)

                } catch (e: Exception) {
                    _saveProfileState.value = AuthState.Error("Failed to upload image: ${e.message}")
                    return@launch
                }
            }

            // 2. Update profile in database (name + avatarUrl)
            val profileResult = repo.updateProfile(fullName, newAvatarUrl)
            if (profileResult.isFailure) {
                _saveProfileState.value = AuthState.Error(
                    profileResult.exceptionOrNull()?.message ?: "Failed to save profile details"
                )
                return@launch
            }

            // 3. Handle email change (if any)
            if (emailChanged) {
                val emailResult = repo.requestEmailChange(newEmail)
                if (emailResult.isSuccess) {
                    _profile.value = _profile.value?.copy(
                        fullName  = fullName,
                        email     = currentEmail,
                        avatarUrl = newAvatarUrl
                    )
                    _profileUpdateVersion.value++
                    _saveProfileState.value = AuthState.AwaitingEmailConfirmation
                } else {
                    _saveProfileState.value = AuthState.Error(
                        emailResult.exceptionOrNull()?.message ?: "Failed to request email change"
                    )
                }
            } else {
                // 4. No email change — update local profile immediately
                _profile.value = _profile.value?.copy(
                    fullName  = fullName,
                    avatarUrl = newAvatarUrl
                )
                _profileUpdateVersion.value++
                _saveProfileState.value = AuthState.Success  // 👈 must be LAST
            }
        }
    }

    fun resetSaveProfileState() { _saveProfileState.value = AuthState.Idle }

    fun isEmailUser(): Boolean = repo.isEmailUser()

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = AuthState.Loading
            val result = repo.changePassword(newPassword)
            _changePasswordState.value = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to change password")
        }
    }

    fun resetChangePasswordState() { _changePasswordState.value = AuthState.Idle }
    fun resetChangeEmailState()    { _changeEmailState.value = AuthState.Idle }
    fun resetState()               { _authState.value = AuthState.Idle }

    fun refreshSessionAndProfile() {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "refreshSessionAndProfile called")
            loadProfile()
            _saveProfileState.value = AuthState.Idle
        }
    }
}