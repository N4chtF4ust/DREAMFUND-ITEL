package com.example.dreamfunds.ui.components.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.example.dreamfunds.Screen
import com.example.dreamfunds.data.model.UserProfile
import com.example.dreamfunds.ui.navigation.DrawerNavItems
import com.example.dreamfunds.viewmodel.AuthViewModel

@Composable
fun AppDrawer(
    profile            : UserProfile?,
    currentDestination : NavDestination?,
    authViewModel      : AuthViewModel,
    onNavigate         : (Screen) -> Unit,
    onLogout           : () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(300.dp)
    ) {
        // We wrap everything in a Column with a verticalScroll state.
        // This makes the entire drawer scrollable in landscape mode!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding() // Moved here so it scrolls smoothly with the content
                .navigationBarsPadding() // Prevents the logout button from hiding behind system nav
        ) {

            DrawerHeader(
                profile = profile,
                authViewModel = authViewModel
            )

            Spacer(modifier = Modifier.height(12.dp))

            DrawerNavItems(
                currentDestination = currentDestination,
                onNavigate         = onNavigate,
            )

            // If you want to push the logout button to the very bottom in portrait mode,
            // you can use a fixed Spacer or let the scrollable column wrap it naturally.
            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                label    = { Text("Logout") },
                selected = false,
                icon     = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                onClick  = onLogout,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            // Add a small bottom spacer for breathing room at the very end of the scroll
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}