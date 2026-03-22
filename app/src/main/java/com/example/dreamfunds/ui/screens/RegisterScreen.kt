// app/src/main/java/com/example/dreamfunds/ui/screens/RegisterScreen.kt
package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dreamfunds.viewmodel.AuthState
import com.example.dreamfunds.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName               by remember { mutableStateOf("") }
    var email                  by remember { mutableStateOf("") }
    var password               by remember { mutableStateOf("") }
    var confirmPassword        by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    // ── If somehow Success fires (e.g. future flow change) still navigate
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onRegisterSuccess()
        }
    }

    // ── Show "check your inbox" screen after successful registration
    if (authState is AuthState.AwaitingEmailConfirmation) {
        EmailConfirmationScreen(
            email       = email,
            onBackToLogin = {
                authViewModel.resetState()
                onBackToLogin()
            }
        )
        return
    }

    val passwordMismatch = confirmPassword.isNotBlank() && password != confirmPassword
    val passwordTooShort = password.isNotBlank() && password.length < 6
    val isFormValid      = fullName.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6
            && password == confirmPassword
            && authState !is AuthState.Loading

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = "Create Account",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = "Start your savings journey",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value         = fullName,
            onValueChange = { fullName = it },
            label         = { Text("Full Name") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            leadingIcon   = { Icon(Icons.Default.Badge, null) },
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = { Text("Email Address") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            leadingIcon   = { Icon(Icons.Default.Email, null) },
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            label                = { Text("Password (min 6 chars)") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(16.dp),
            leadingIcon          = { Icon(Icons.Default.Lock, null) },
            trailingIcon         = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError              = passwordTooShort,
            supportingText       = {
                if (passwordTooShort)
                    Text("At least 6 characters required",
                        color = MaterialTheme.colorScheme.error)
                else if (password.isBlank())
                    Text("Minimum 6 characters")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value                = confirmPassword,
            onValueChange        = { confirmPassword = it },
            label                = { Text("Confirm Password") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(16.dp),
            leadingIcon          = { Icon(Icons.Default.Lock, null) },
            trailingIcon         = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError              = passwordMismatch,
            supportingText       = {
                when {
                    passwordMismatch -> Text("Passwords do not match",
                        color = MaterialTheme.colorScheme.error)
                    confirmPassword.isNotBlank() && !passwordMismatch ->
                        Text("✓ Passwords match", color = Color(0xFF4CAF50))
                }
            },
            singleLine = true
        )

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = (authState as AuthState.Error).message,
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick  = { authViewModel.register(fullName, email, password) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            enabled  = isFormValid
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already Have an Account?")
            TextButton(onClick = onBackToLogin) {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// "Check your inbox" screen shown after successful registration
// ─────────────────────────────────────────────────────────────
@Composable
private fun EmailConfirmationScreen(
    email: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailUnread,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint     = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text       = "Check Your Inbox",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text      = "We sent a confirmation link to",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            text       = email,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text      = "Click the link in the email to activate your account, then come back and log in.",
            style     = MaterialTheme.typography.bodySmall,
            color     = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick  = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp)
        ) {
            Text("GO TO LOGIN", fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Green preview theme
// ─────────────────────────────────────────────────────────────
private val GreenColorScheme = lightColorScheme(
    primary          = Color(0xFF2E7D32),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    secondary        = Color(0xFF388E3C),
    onSecondary      = Color.White,
    tertiary         = Color(0xFF81C784),
    background       = Color(0xFFF1F8E9),
    surface          = Color(0xFFF1F8E9),
    error            = Color(0xFFB00020),
    onBackground     = Color(0xFF1B1B1B),
    onSurface        = Color(0xFF1B1B1B),
)

@Composable
private fun DreamFundsPreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GreenColorScheme, content = content)
}

// ─────────────────────────────────────────────────────────────
// Stateless UI — previews only
// ─────────────────────────────────────────────────────────────
@Composable
private fun RegisterScreenStateless(
    fullName               : String,
    email                  : String,
    password               : String,
    confirmPassword        : String,
    passwordVisible        : Boolean,
    confirmPasswordVisible : Boolean,
    isLoading              : Boolean,
    errorMessage           : String?,
    onFullNameChange        : (String) -> Unit,
    onEmailChange           : (String) -> Unit,
    onPasswordChange        : (String) -> Unit,
    onConfirmPasswordChange : (String) -> Unit,
    onTogglePassword        : () -> Unit,
    onToggleConfirmPassword : () -> Unit,
    onRegisterClick         : () -> Unit,
    onBackToLogin           : () -> Unit
) {
    val passwordMismatch = confirmPassword.isNotBlank() && password != confirmPassword
    val passwordTooShort = password.isNotBlank() && password.length < 6
    val isFormValid      = fullName.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6
            && password == confirmPassword
            && !isLoading

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Start your savings journey",
            style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = fullName, onValueChange = onFullNameChange,
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Badge, null) }, singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = onEmailChange,
            label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Email, null) }, singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = onPasswordChange,
            label = { Text("Password (min 6 chars)") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(if (passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError = passwordTooShort,
            supportingText = {
                if (passwordTooShort) Text("At least 6 characters required",
                    color = MaterialTheme.colorScheme.error)
                else if (password.isBlank()) Text("Minimum 6 characters")
            },
            singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPassword) {
                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError = passwordMismatch,
            supportingText = {
                when {
                    passwordMismatch -> Text("Passwords do not match",
                        color = MaterialTheme.colorScheme.error)
                    confirmPassword.isNotBlank() && !passwordMismatch ->
                        Text("✓ Passwords match", color = Color(0xFF4CAF50))
                }
            },
            singleLine = true)

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp), enabled = isFormValid) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already Have an Account?")
            TextButton(onClick = onBackToLogin) {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Register — Idle")
@Composable
private fun RegisterIdlePreview() {
    DreamFundsPreviewTheme {
        RegisterScreenStateless("", "", "", "", false, false, false, null,
            {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Register — All Valid")
@Composable
private fun RegisterValidPreview() {
    DreamFundsPreviewTheme {
        RegisterScreenStateless("Juan", "juan@example.com", "secret123", "secret123",
            false, false, false, null, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Register — Mismatch")
@Composable
private fun RegisterMismatchPreview() {
    DreamFundsPreviewTheme {
        RegisterScreenStateless("Juan", "juan@example.com", "secret123", "different",
            false, false, false, null, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Register — Awaiting Confirmation")
@Composable
private fun RegisterAwaitingPreview() {
    DreamFundsPreviewTheme {
        EmailConfirmationScreen(
            email         = "juan@example.com",
            onBackToLogin = {}
        )
    }
}

@Preview(showBackground = true, name = "Register — API Error")
@Composable
private fun RegisterErrorPreview() {
    DreamFundsPreviewTheme {
        RegisterScreenStateless("Juan", "juan@example.com", "secret123", "secret123",
            false, false, false, "An account with this email already exists.",
            {}, {}, {}, {}, {}, {}, {}, {})
    }
}