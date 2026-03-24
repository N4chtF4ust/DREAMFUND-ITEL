package com.example.dreamfunds.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dreamfunds.ui.theme.DreamFundsTheme
import com.example.dreamfunds.viewmodel.AuthState
import com.example.dreamfunds.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val profile             by authViewModel.profile.collectAsState()
    val saveState           by authViewModel.saveProfileState.collectAsState()
    val changePasswordState by authViewModel.changePasswordState.collectAsState()

    var name  by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSaveSnackbar         by remember { mutableStateOf(false) }
    var showPasswordSnackbar     by remember { mutableStateOf(false) }

    // ── Back button debounce ──────────────────────────────────────
    var isNavigatingBack by remember { mutableStateOf(false) }

    val isEmailUser = remember { authViewModel.isEmailUser() }
    val scope       = rememberCoroutineScope()
    val context     = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    LaunchedEffect(Unit) { authViewModel.loadProfile() }

    LaunchedEffect(saveState) {
        when (saveState) {
            is AuthState.Success -> {
                showSaveSnackbar = true
                selectedImageUri = null
                authViewModel.loadProfile()
            }
            else -> Unit
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
                    IconButton(
                        onClick = {
                            if (!isNavigatingBack) {
                                isNavigatingBack = true
                                onBack()
                            }
                        },
                        enabled = !isNavigatingBack,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Avatar Section ────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    val currentImageToDisplay = selectedImageUri ?: profile?.avatarUrl

                    if (currentImageToDisplay != null &&
                        currentImageToDisplay.toString().isNotBlank()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentImageToDisplay)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize(),
                        )
                    } else {
                        val initials = profile?.fullName
                            ?.trim()
                            ?.split(" ")
                            ?.filter { it.isNotBlank() }
                            ?.take(2)
                            ?.joinToString("") { it.first().uppercaseChar().toString() }
                            ?.ifBlank { "?" } ?: "?"

                        Text(
                            text       = initials,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape          = CircleShape,
                    color          = MaterialTheme.colorScheme.primary,
                    tonalElevation = 2.dp,
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Edit Photo",
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text       = profile?.fullName?.takeIf { it.isNotBlank() } ?: "DreamFunds User",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text  = profile?.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email Confirmation Banner ─────────────────────────
            if (saveState is AuthState.AwaitingEmailConfirmation) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Default.MarkEmailUnread, null,
                            tint     = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp),
                        )
                        Column {
                            Text(
                                "Confirm your new email",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF2E7D32),
                            )
                            Text(
                                "A confirmation link was sent to your new address.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C),
                            )
                        }
                    }
                }
            }

            // ── Fields ────────────────────────────────────────────
            ProfileField(
                label         = "Full Name",
                value         = name,
                onValueChange = { name = it },
                icon          = Icons.Default.Badge,
            )

            ProfileField(
                label            = "Email Address",
                value            = email,
                onValueChange    = { email = it },
                icon             = Icons.Default.Email,
                trailingIcon     = if (saveState is AuthState.AwaitingEmailConfirmation)
                    Icons.Default.HourglassEmpty else null,
                trailingIconTint = Color(0xFF388E3C),
                supportingText   = if (saveState is AuthState.AwaitingEmailConfirmation)
                    "Pending confirmation" else null,
            )

            if (saveState is AuthState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    (saveState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Change Password ───────────────────────────────────
            if (isEmailUser) {
                OutlinedButton(
                    onClick  = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.VpnKey, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Default.Info, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            "Password is managed by Google",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Save Button ───────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        var avatarBytes: ByteArray? = null
                        if (selectedImageUri != null) {
                            avatarBytes = processAndCompressImage(context, selectedImageUri!!)
                        }
                        authViewModel.saveProfile(name, email, avatarBytes)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape   = RoundedCornerShape(16.dp),
                enabled = saveState !is AuthState.Loading &&
                        saveState !is AuthState.AwaitingEmailConfirmation,
            ) {
                if (saveState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp,
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
            onConfirm = { newPassword -> authViewModel.changePassword(newPassword) },
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Utility Functions
// ─────────────────────────────────────────────────────────────

suspend fun processAndCompressImage(context: Context, uri: Uri): ByteArray? =
    withContext(Dispatchers.IO) {
        val maxSizeBytes = 2 * 1024 * 1024
        val inputStream  = context.contentResolver.openInputStream(uri)
            ?: return@withContext null
        val originalBytes = inputStream.readBytes()
        inputStream.close()

        if (originalBytes.size <= maxSizeBytes) return@withContext originalBytes

        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
        var quality = 90
        var compressedBytes: ByteArray
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            quality -= 10
        } while (compressedBytes.size > maxSizeBytes && quality > 10)

        return@withContext compressedBytes
    }

@Composable
private fun ChangePasswordDialog(
    changePasswordState : AuthState,
    onDismiss           : () -> Unit,
    onConfirm           : (String) -> Unit,
) {
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newVisible      by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotBlank() && newPassword != confirmPassword
    val passwordTooShort = newPassword.isNotBlank() && newPassword.length < 6
    val isFormValid      = newPassword.length >= 6 &&
            newPassword == confirmPassword &&
            changePasswordState !is AuthState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.VpnKey, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        },
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = newPassword,
                    onValueChange = { newPassword = it },
                    label         = { Text("New Password") },
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = passwordTooShort,
                    visualTransformation = if (newVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon  = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                if (newVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff, null,
                            )
                        }
                    },
                    supportingText = {
                        if (passwordTooShort) Text(
                            "At least 6 characters required",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
                OutlinedTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label         = { Text("Confirm New Password") },
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = passwordMismatch,
                    visualTransformation = if (confirmVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon  = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff, null,
                            )
                        }
                    },
                    supportingText = {
                        if (passwordMismatch) Text(
                            "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
                if (changePasswordState is AuthState.Error) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            changePasswordState.message,
                            modifier = Modifier.padding(10.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            style    = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPassword) },
                enabled = isFormValid,
            ) {
                if (changePasswordState is AuthState.Loading)
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        Color.White,
                        strokeWidth = 2.dp,
                    )
                else Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ProfileField(
    label            : String,
    value            : String,
    onValueChange    : (String) -> Unit,
    icon             : ImageVector,
    isPassword       : Boolean     = false,
    trailingIcon     : ImageVector? = null,
    trailingIconTint : Color        = Color.Gray,
    supportingText   : String?      = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp,
            color      = MaterialTheme.colorScheme.primary,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            leadingIcon   = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon  = trailingIcon?.let { iv -> { Icon(iv, null, tint = trailingIconTint) } },
            visualTransformation = if (isPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            supportingText = supportingText?.let { { Text(it, color = trailingIconTint) } },
            singleLine     = true,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenStateless(
    fullName    : String,
    email       : String,
    initials    : String,
    avatarUrl   : String?,
    isEmailUser : Boolean,
    saveState   : AuthState,
    onBack      : () -> Unit,
) {
    var isNavigatingBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isNavigatingBack) {
                                isNavigatingBack = true
                                onBack()
                            }
                        },
                        enabled = !isNavigatingBack,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(110.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model            = avatarUrl,
                            contentDescription = null,
                            contentScale     = ContentScale.Crop,
                            modifier         = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            initials,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        Icons.Default.CameraAlt, null,
                        tint     = Color.White,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            ProfileField("Full Name", fullName, {}, Icons.Default.Badge)
            ProfileField("Email Address", email, {}, Icons.Default.Email)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick  = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
            ) { Text("Save Changes", fontWeight = FontWeight.Bold) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    DreamFundsTheme {
        ProfileScreenStateless("Juan dela Cruz", "juan@example.com", "JC", null, true, AuthState.Idle) {}
    }
}