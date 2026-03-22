package com.example.dreamfunds.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.dreamfunds.Screen
import androidx.compose.foundation.layout.padding

/**
 * Single source-of-truth for every primary nav destination.
 *
 * Add / remove entries here and the drawer + any other nav
 * surface will reflect the change automatically.
 */
data class NavItem(
    val screen : Screen,
    val label  : String,
    val icon   : ImageVector,
)

val primaryNavItems: List<NavItem> = listOf(
    NavItem(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
    NavItem(Screen.Reports,   "Reports",   Icons.Default.BarChart),
    NavItem(Screen.Profile,   "Profile",   Icons.Default.Person),
)

/**
 * Renders [primaryNavItems] as [NavigationDrawerItem]s.
 *
 * @param currentDestination  The currently active back-stack destination,
 *                            used to highlight the selected item.
 * @param onNavigate          Called with the chosen [Screen] when the user
 *                            taps an item.
 */
@Composable
fun DrawerNavItems(
    currentDestination : NavDestination?,
    onNavigate         : (Screen) -> Unit,
) {
    primaryNavItems.forEach { item ->
        NavigationDrawerItem(
            label    = { Text(item.label) },
            selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.screen.route } == true,
            icon     = { Icon(item.icon, contentDescription = item.label) },
            onClick  = { onNavigate(item.screen) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}
