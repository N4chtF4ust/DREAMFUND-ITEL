package com.example.dreamfunds.ui.components.drawer

import androidx.compose.foundation.layout.*
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

/**
 * The full [ModalDrawerSheet] content: header, primary nav items,
 * a divider, and the logout action.
 *
 * Intentionally stateless — callers supply every value and callback
 * so this composable can be previewed and tested in isolation.
 *
 * @param profile              Currently signed-in user (null while loading).
 * @param currentDestination   Active back-stack destination for selection state.
 * @param onNavigate           Called when the user picks a nav destination.
 * @param onLogout             Called when the user taps "Logout".
 */
@Composable
fun AppDrawer(
    profile             : UserProfile?,
    currentDestination  : NavDestination?,
    onNavigate          : (Screen) -> Unit,
    onLogout            : () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier             = Modifier.width(300.dp),
    ) {
        DrawerHeader(profile = profile)

        Spacer(modifier = Modifier.height(12.dp))

        DrawerNavItems(
            currentDestination = currentDestination,
            onNavigate         = onNavigate,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label    = { Text("Logout") },
            selected = false,
            icon     = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
            onClick  = onLogout,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}