package com.example.dreamfunds.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dreamfunds.R
import com.example.dreamfunds.SupabaseClientProvider
import com.example.dreamfunds.ui.theme.DreamFundsTheme // <-- IMPORTED YOUR THEME HERE
import com.example.dreamfunds.viewmodel.AuthState
import com.example.dreamfunds.viewmodel.AuthViewModel
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    val googleSignIn = SupabaseClientProvider.client.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                NativeSignInResult.Success      -> authViewModel.onGoogleSignInSuccess()
                NativeSignInResult.ClosedByUser -> { /* dismissed — do nothing */ }
                is NativeSignInResult.Error     -> authViewModel.onGoogleSignInError(result.message)
                else                            -> authViewModel.onGoogleSignInError("Google sign-in failed. Please try again.")
            }
        }
    )

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Brand — app logo ──────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.dreamfunds_logo),
            contentDescription = "DreamFunds Logo",
            modifier           = Modifier.size(100.dp),
            contentScale       = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "DreamFunds",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            "Smart Savings for Your Future",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Google Sign-In button ─────────────────────────────────
        OutlinedButton(
            onClick  = { googleSignIn.startFlow() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor   = Color(0xFF1F1F1F)
            ),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = Color(0xFF4285F4)
                )
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleGLogo(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Continue with Google",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF1F1F1F)
                    )
                }
            }
        }

        // ── OR divider ────────────────────────────────────────────
        OrDivider()

        // ── Email field ───────────────────────────────────────────
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = { Text("Email") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            leadingIcon   = { Icon(Icons.Default.Email, null) },
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Password field ────────────────────────────────────────
        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            label                = { Text("Password") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            singleLine  = true
        )

        // ── Error message ─────────────────────────────────────────
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = (authState as AuthState.Error).message,
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Email login button ────────────────────────────────────
        Button(
            onClick  = { authViewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape   = RoundedCornerShape(16.dp),
            enabled = email.isNotBlank()
                    && password.length >= 4
                    && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Register link ─────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?")
            TextButton(onClick = onRegisterClick) {
                Text("REGISTER", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Google "G" logo drawn with Canvas — no drawable asset needed
// ─────────────────────────────────────────────────────────────
@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r       = minOf(size.width, size.height) / 2f
        val cx      = size.width  / 2f
        val cy      = size.height / 2f
        val strokeW = r * 0.28f
        val arcR    = r * 0.82f

        data class Arc(val color: Color, val start: Float, val sweep: Float)
        listOf(
            Arc(Color(0xFF4285F4), -23f, 73f),
            Arc(Color(0xFF34A853),  50f, 85f),
            Arc(Color(0xFFFBBC05), 135f, 57f),
            Arc(Color(0xFFEA4335), 192f, 91f),
        ).forEach { arc ->
            drawArc(
                color      = arc.color,
                startAngle = arc.start,
                sweepAngle = arc.sweep,
                useCenter  = false,
                style      = Stroke(width = strokeW),
                topLeft    = Offset(cx - arcR, cy - arcR),
                size       = Size(arcR * 2, arcR * 2)
            )
        }
        drawRect(
            color   = Color(0xFF4285F4),
            topLeft = Offset(cx - strokeW * 0.1f, cy - strokeW * 0.55f),
            size    = Size(r - strokeW * 0.2f, strokeW)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// "─── OR ───" divider
// ─────────────────────────────────────────────────────────────
@Composable
private fun OrDivider() {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
        Text("  OR  ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
    }
}


// ─────────────────────────────────────────────────────────────
// Preview — never touches SupabaseClientProvider or AuthViewModel
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Login — Idle")
@Composable
private fun LoginScreenPreview() {
    DreamFundsTheme {
        LoginScreenStateless(
            email               = "",
            password            = "",
            passwordVisible     = false,
            isLoading           = false,
            errorMessage        = null,
            onEmailChange       = {},
            onPasswordChange    = {},
            onTogglePassword    = {},
            onLoginClick        = {},
            onGoogleSignInClick = {},
            onRegisterClick     = {}
        )
    }
}

@Preview(showBackground = true, name = "Login — Error")
@Composable
private fun LoginScreenErrorPreview() {
    DreamFundsTheme {
        LoginScreenStateless(
            email               = "user@example.com",
            password            = "wrong",
            passwordVisible     = false,
            isLoading           = false,
            errorMessage        = "Invalid email or password.",
            onEmailChange       = {},
            onPasswordChange    = {},
            onTogglePassword    = {},
            onLoginClick        = {},
            onGoogleSignInClick = {},
            onRegisterClick     = {}
        )
    }
}

@Preview(showBackground = true, name = "Login — Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    DreamFundsTheme {
        LoginScreenStateless(
            email               = "user@example.com",
            password            = "pass123",
            passwordVisible     = false,
            isLoading           = true,
            errorMessage        = null,
            onEmailChange       = {},
            onPasswordChange    = {},
            onTogglePassword    = {},
            onLoginClick        = {},
            onGoogleSignInClick = {},
            onRegisterClick     = {}
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Stateless UI — used exclusively by previews.
// LoginScreen (above) is NOT changed; this is a separate
// composable that previews can call safely.
// ─────────────────────────────────────────────────────────────
@Composable
private fun LoginScreenStateless(
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Brand ─────────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.dreamfunds_logo),
            contentDescription = "DreamFunds Logo",
            modifier           = Modifier.size(100.dp),
            contentScale       = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "DreamFunds",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            "Smart Savings for Your Future",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Google Sign-In button ─────────────────────────────────
        OutlinedButton(
            onClick  = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            border   = BorderStroke(1.dp, Color(0xFFDDDDDD)),
            colors   = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor   = Color(0xFF1F1F1F)
            ),
            enabled  = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = Color(0xFF4285F4)
                )
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleGLogo(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Continue with Google",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF1F1F1F)
                    )
                }
            }
        }

        OrDivider()

        // ── Email field ───────────────────────────────────────────
        OutlinedTextField(
            value         = email,
            onValueChange = onEmailChange,
            label         = { Text("Email") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            leadingIcon   = { Icon(Icons.Default.Email, null) },
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Password field ────────────────────────────────────────
        OutlinedTextField(
            value                = password,
            onValueChange        = onPasswordChange,
            label                = { Text("Password") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            singleLine  = true
        )

        // ── Error message ─────────────────────────────────────────
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = errorMessage,
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Login button ──────────────────────────────────────────
        Button(
            onClick  = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            enabled  = email.isNotBlank() && password.length >= 4 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Register link ─────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?")
            TextButton(onClick = onRegisterClick) {
                Text("REGISTER", fontWeight = FontWeight.Bold)
            }
        }
    }
}