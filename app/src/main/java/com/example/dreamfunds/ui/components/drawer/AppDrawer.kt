// app/src/main/java/com/example/dreamfunds/ui/components/drawer/AppDrawer.kt
package com.example.dreamfunds.ui.components.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.example.dreamfunds.Screen
import com.example.dreamfunds.data.model.UserProfile
import com.example.dreamfunds.ui.navigation.DrawerNavItems
import com.example.dreamfunds.viewmodel.AuthViewModel
import com.example.dreamfunds.viewmodel.ThemeMode
import com.example.dreamfunds.viewmodel.ThemeViewModel

@Composable
fun AppDrawer(
    profile            : UserProfile?,
    currentDestination : NavDestination?,
    authViewModel      : AuthViewModel,
    themeViewModel     : ThemeViewModel,          // ← new
    onNavigate         : (Screen) -> Unit,
    onLogout           : () -> Unit,
) {
    val themeMode by themeViewModel.themeMode.collectAsState()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            DrawerHeader(
                profile = profile,
                authViewModel = authViewModel
            )



            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Theme selector ──────────────────────────────────────────
            ThemeModeSelector(
                current  = themeMode,
                onSelect = { themeViewModel.setTheme(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(12.dp))

            DrawerNavItems(
                currentDestination = currentDestination,
                onNavigate         = onNavigate,
            )



            NavigationDrawerItem(
                label    = { Text("Logout") },
                selected = false,
                icon     = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                onClick  = onLogout,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Segmented button row for System / Light / Dark ──────────────────────────
@Composable
private fun ThemeModeSelector(
    current  : ThemeMode,
    onSelect : (ThemeMode) -> Unit,
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, "System", Icons.Filled.BrightnessAuto),
        Triple(ThemeMode.LIGHT,  "Light",  Icons.Filled.LightMode),
        Triple(ThemeMode.DARK,   "Dark",   Icons.Filled.DarkMode),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = "Appearance",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Material 3 segmented button
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (mode, label, icon) ->
                SegmentedButton(
                    shape    = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size,
                    ),
                    selected = current == mode,
                    onClick  = { onSelect(mode) },
                    icon     = {
                        SegmentedButtonDefaults.Icon(active = current == mode) {
                            Icon(
                                imageVector    = icon,
                                contentDescription = null,
                                modifier       = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        }
                    },
                    label    = { Text(label) },
                )
            }
        }
    }
}