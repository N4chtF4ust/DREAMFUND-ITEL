// app/src/main/java/com/example/dreamfunds/ui/screens/ProfileScreen.kt
package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dreamfunds.viewmodel.AuthState
import com.example.dreamfunds.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val profile             by authViewModel.profile.collectAsState()
    val saveState           by authViewModel.saveProfileState.collectAsState()
    val changePasswordState by authViewModel.changePasswordState.collectAsState()

    var name  by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSaveSnackbar         by remember { mutableStateOf(false) }
    var showPasswordSnackbar     by remember { mutableStateOf(false) }

    val isEmailUser = remember { authViewModel.isEmailUser() }

    LaunchedEffect(Unit) { authViewModel.loadProfile() }

    LaunchedEffect(saveState) {
        when (saveState) {
            is AuthState.Success -> showSaveSnackbar = true
            else                 -> Unit
        }
    }

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is AuthState.Success) {
            showPasswordSnackbar = true
            showChangePasswordDialog = false
            authViewModel.resetChangePasswordState()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSaveSnackbar) {
        if (showSaveSnackbar) {
            snackbarHostState.showSnackbar("Profile saved successfully!")
            showSaveSnackbar = false
            authViewModel.resetSaveProfileState()
        }
    }

    LaunchedEffect(showPasswordSnackbar) {
        if (showPasswordSnackbar) {
            snackbarHostState.showSnackbar("Password changed successfully!")
            showPasswordSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier            = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Avatar ────────────────────────────────────────────
            val initials = profile?.fullName
                ?.trim()
                ?.split(" ")
                ?.filter { it.isNotBlank() }
                ?.take(2)
                ?.joinToString("") { it.first().uppercaseChar().toString() }
                ?.ifBlank { "?" }
                ?: "?"

            Box(
                modifier         = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = initials,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = profile?.fullName?.takeIf { it.isNotBlank() } ?: "DreamFunds User",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = profile?.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email change pending banner ────────────────────────
            if (saveState is AuthState.AwaitingEmailConfirmation) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier          = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint     = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                "Confirm your new email",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF2E7D32)
                            )
                            Text(
                                "A confirmation link was sent to your new address. Your email will update after you click it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }
            }

            // ── Profile fields ────────────────────────────────────
            ProfileField(
                label         = "Full Name",
                value         = name,
                onValueChange = { name = it },
                icon          = Icons.Default.Badge
            )

            // Email field — show pending indicator if change is awaiting confirmation
            ProfileField(
                label         = "Email Address",
                value         = email,
                onValueChange = { email = it },
                icon          = Icons.Default.Email,
                trailingIcon  = if (saveState is AuthState.AwaitingEmailConfirmation)
                    Icons.Default.HourglassEmpty else null,
                trailingIconTint = Color(0xFF388E3C),
                supportingText = if (saveState is AuthState.AwaitingEmailConfirmation)
                    "Pending confirmation" else null
            )

            if (saveState is AuthState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    (saveState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Change Password ───────────────────────────────────
            if (isEmailUser) {
                OutlinedButton(
                    onClick  = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VpnKey, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text("Password is managed by Google",
                            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Save button ───────────────────────────────────────
            Button(
                onClick  = { authViewModel.saveProfile(name, email) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = saveState !is AuthState.Loading
                        && saveState !is AuthState.AwaitingEmailConfirmation
            ) {
                if (saveState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            changePasswordState = changePasswordState,
            onDismiss           = {
                showChangePasswordDialog = false
                authViewModel.resetChangePasswordState()
            },
            onConfirm = { newPassword -> authViewModel.changePassword(newPassword) }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Change Password Dialog
// ─────────────────────────────────────────────────────────────
@Composable
private fun ChangePasswordDialog(
    changePasswordState: AuthState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newVisible      by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotBlank() && newPassword != confirmPassword
    val passwordTooShort = newPassword.isNotBlank() && newPassword.length < 6
    val isFormValid      = newPassword.length >= 6
            && newPassword == confirmPassword
            && changePasswordState !is AuthState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = {
            Icon(Icons.Default.VpnKey, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value                = newPassword,
                    onValueChange        = { newPassword = it },
                    label                = { Text("New Password") },
                    shape                = RoundedCornerShape(12.dp),
                    modifier             = Modifier.fillMaxWidth(),
                    singleLine           = true,
                    isError              = passwordTooShort,
                    visualTransformation = if (newVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    leadingIcon  = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(if (newVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff, null)
                        }
                    },
                    supportingText = {
                        if (passwordTooShort)
                            Text("At least 6 characters required",
                                color = MaterialTheme.colorScheme.error)
                        else if (newPassword.isBlank()) Text("Minimum 6 characters")
                    }
                )
                OutlinedTextField(
                    value                = confirmPassword,
                    onValueChange        = { confirmPassword = it },
                    label                = { Text("Confirm New Password") },
                    shape                = RoundedCornerShape(12.dp),
                    modifier             = Modifier.fillMaxWidth(),
                    singleLine           = true,
                    isError              = passwordMismatch,
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    leadingIcon  = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(if (confirmVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff, null)
                        }
                    },
                    supportingText = {
                        when {
                            passwordMismatch -> Text("Passwords do not match",
                                color = MaterialTheme.colorScheme.error)
                            confirmPassword.isNotBlank() && !passwordMismatch ->
                                Text("✓ Passwords match", color = Color(0xFF4CAF50))
                        }
                    }
                )
                if (changePasswordState is AuthState.Error) {
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer) {
                        Text(changePasswordState.message, modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newPassword) }, enabled = isFormValid) {
                if (changePasswordState is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(16.dp), Color.White, strokeWidth = 2.dp)
                else Text("Update Password")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─────────────────────────────────────────────────────────────
// Profile input field — extended to support trailing icon + supporting text
// ─────────────────────────────────────────────────────────────
@Composable
fun ProfileField(
    label            : String,
    value            : String,
    onValueChange    : (String) -> Unit,
    icon             : ImageVector,
    isPassword       : Boolean = false,
    trailingIcon     : ImageVector? = null,
    trailingIconTint : Color = Color.Gray,
    supportingText   : String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            leadingIcon          = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon         = trailingIcon?.let { iv ->
                { Icon(iv, null, tint = trailingIconTint) }
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            supportingText       = supportingText?.let { { Text(it, color = trailingIconTint) } },
            singleLine           = true
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Green preview theme
// ─────────────────────────────────────────────────────────────
private val GreenColorScheme = lightColorScheme(
    primary            = Color(0xFF2E7D32),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF002106),
    secondary          = Color(0xFF388E3C),
    onSecondary        = Color.White,
    tertiary           = Color(0xFF81C784),
    background         = Color(0xFFF1F8E9),
    surface            = Color(0xFFF1F8E9),
    surfaceVariant     = Color(0xFFDCEDC8),
    error              = Color(0xFFB00020),
    onBackground       = Color(0xFF1B1B1B),
    onSurface          = Color(0xFF1B1B1B),
)

@Composable
private fun DreamFundsPreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GreenColorScheme, content = content)
}

// ─────────────────────────────────────────────────────────────
// Stateless UI — previews only
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenStateless(
    fullName    : String,
    email       : String,
    initials    : String,
    isEmailUser : Boolean,
    saveState   : AuthState,
    onNameChange          : (String) -> Unit,
    onEmailChange         : (String) -> Unit,
    onSaveClick           : () -> Unit,
    onChangePasswordClick : () -> Unit,
    onBack                : () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier         = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(fullName.ifBlank { "DreamFunds User" },
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Email change pending banner
            if (saveState is AuthState.AwaitingEmailConfirmation) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.MarkEmailUnread, null,
                            tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                        Column {
                            Text("Confirm your new email",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                            Text("A confirmation link was sent to your new address.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C))
                        }
                    }
                }
            }

            ProfileField(label = "Full Name", value = fullName,
                onValueChange = onNameChange, icon = Icons.Default.Badge)
            ProfileField(
                label            = "Email Address",
                value            = email,
                onValueChange    = onEmailChange,
                icon             = Icons.Default.Email,
                trailingIcon     = if (saveState is AuthState.AwaitingEmailConfirmation)
                    Icons.Default.HourglassEmpty else null,
                trailingIconTint = Color(0xFF388E3C),
                supportingText   = if (saveState is AuthState.AwaitingEmailConfirmation)
                    "Pending confirmation" else null
            )

            if (saveState is AuthState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(saveState.message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEmailUser) {
                OutlinedButton(onClick = onChangePasswordClick,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.VpnKey, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
            } else {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray,
                            modifier = Modifier.size(18.dp))
                        Text("Password is managed by Google",
                            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick  = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = saveState !is AuthState.Loading
                        && saveState !is AuthState.AwaitingEmailConfirmation
            ) {
                if (saveState is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), Color.White, strokeWidth = 2.dp)
                else Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Profile — Email User")
@Composable
private fun ProfileEmailPreview() {
    DreamFundsPreviewTheme {
        ProfileScreenStateless("Juan dela Cruz", "juan@example.com", "JC",
            true, AuthState.Idle, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Profile — Google User")
@Composable
private fun ProfileGooglePreview() {
    DreamFundsPreviewTheme {
        ProfileScreenStateless("Maria Santos", "maria@gmail.com", "MS",
            false, AuthState.Idle, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Profile — Email Change Pending")
@Composable
private fun ProfileEmailChangePendingPreview() {
    DreamFundsPreviewTheme {
        ProfileScreenStateless("Juan dela Cruz", "juan.new@example.com", "JC",
            true, AuthState.AwaitingEmailConfirmation, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Profile — Saving")
@Composable
private fun ProfileSavingPreview() {
    DreamFundsPreviewTheme {
        ProfileScreenStateless("Juan dela Cruz", "juan@example.com", "JC",
            true, AuthState.Loading, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Profile — Save Error")
@Composable
private fun ProfileErrorPreview() {
    DreamFundsPreviewTheme {
        ProfileScreenStateless("Juan dela Cruz", "juan@example.com", "JC",
            true, AuthState.Error("Email already in use."), {}, {}, {}, {}, {})
    }
}