package com.example.dreamfunds.ui.components.drawer


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.R
import com.example.dreamfunds.data.model.UserProfile

/**
 * The coloured header block inside the navigation drawer.
 * Shows the DreamFunds brand strip on top and the signed-in
 * user's avatar initials / name / email below.
 *
 * @param profile  The currently signed-in user's profile, or null while loading.
 */
@Composable
fun DrawerHeader(profile: UserProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        BrandStrip()

        HorizontalDivider(
            color     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )

        UserInfoStrip(profile = profile)
    }
}

// ── Brand strip ───────────────────────────────────────────────────────────────

@Composable
private fun BrandStrip() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter            = painterResource(id = R.drawable.dreamfunds_logo),
            contentDescription = "DreamFunds Logo",
            modifier           = Modifier.size(36.dp),
            contentScale       = ContentScale.Fit
        )
        Text(
            text       = "DreamFunds",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// ── User info strip ───────────────────────────────────────────────────────────

@Composable
private fun UserInfoStrip(profile: UserProfile?) {
    val initials = profile?.fullName
        ?.trim()
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercaseChar().toString() }
        ?.ifBlank { "?" }
        ?: "?"

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        UserAvatar(initials = initials)

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = profile?.fullName?.takeIf { it.isNotBlank() } ?: "DreamFunds User",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimary
            )
            if (!profile?.email.isNullOrBlank()) {
                Text(
                    text  = profile!!.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ── Avatar circle ─────────────────────────────────────────────────────────────

@Composable
private fun UserAvatar(initials: String) {
    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary
        )
    }
}