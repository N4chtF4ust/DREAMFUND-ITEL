package com.example.dreamfunds.ui.components.drawer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dreamfunds.R
import com.example.dreamfunds.data.model.UserProfile
import com.example.dreamfunds.viewmodel.AuthViewModel

@Composable
fun DrawerHeader(
    profile: UserProfile?,
    authViewModel: AuthViewModel,
    onProfileClick: () -> Unit = {}
) {
    val currentProfile by authViewModel.profile.collectAsState()

    // Using primaryContainer automatically applies your Mint (Light) or Dark Green (Dark)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
    ) {
        BrandStrip()

        Spacer(modifier = Modifier.height(24.dp))

        UserInfoBlock(
            profile = currentProfile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun BrandStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "DreamFunds"
            },
        verticalAlignment = Alignment.CenterVertically,
        // 1. Force the layout spacing to absolutely zero
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.dreamfunds_logo),
            contentDescription = null,
            modifier = Modifier
                // Match the height to your text, but let the width wrap so it doesn't take up extra space
                .height(28.dp)
                .wrapContentWidth(),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
            contentScale = ContentScale.Fit
        )
        Text(
            text = "reamFunds",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            // 2. The Magic Trick: Pull the text to the left by a few density pixels.
            // Adjust this number (e.g., -2.dp, -4.dp) until it looks exactly like one word.
            modifier = Modifier.offset(x = (-8).dp)
        )
    }
}

@Composable
private fun UserInfoBlock(profile: UserProfile?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (profile == null) {
                    "Loading user profile"
                } else {
                    "Signed in as ${profile.fullName}, ${profile.email}. Double tap to view profile."
                }
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (profile == null) {
            UserAvatarPlaceholder()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 140.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        } else {
            val initials = profile.fullName
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "?" }

            UserAvatar(
                avatarUrl = profile.avatarUrl,
                initials = initials
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = profile.fullName.takeIf { it.isNotBlank() } ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (!profile.email.isNullOrBlank()) {
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(avatarUrl: String?, initials: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            // Adds a faint ring around the avatar using the theme's text color
            .border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
            // Fallback background uses the strong primary color (Forest Green/Bright Green)
            .background(MaterialTheme.colorScheme.primary)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary // White in Light Mode, Dark Green in Dark Mode
            )
        }
    }
}

@Composable
private fun UserAvatarPlaceholder() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB8B5B5).copy(alpha = 0.2f),
                Color(0xFFB8B5B5).copy(alpha = 0.5f),
                Color(0xFFB8B5B5).copy(alpha = 0.2f)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}