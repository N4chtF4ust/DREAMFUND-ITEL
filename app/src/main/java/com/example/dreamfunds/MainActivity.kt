package com.example.dreamfunds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dreamfunds.ui.theme.DreamFundsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DreamFundsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DreamFundsApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Reports : Screen("reports", "Reports")
    object Profile : Screen("profile", "Profile")
}

@Composable
fun DreamFundsApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Shared state for the whole app
    var transactions by remember { 
        mutableStateOf(mutableListOf(
            Transaction("Salary Deposit", "March 1, 2025", "45,000.00", true, "Income"),
            Transaction("Grocery Shop", "March 3, 2025", "2,500.00", false, "Food"),
            Transaction("Internet Bill", "March 2, 2025", "1,500.00", false, "Bills"),
            Transaction("Coffee", "March 3, 2025", "250.00", false, "Food")
        ))
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentDestination?.route != Screen.Login.route && currentDestination?.route != Screen.Register.route,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dreamfunds),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val items = listOf(
                    Triple(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
                    Triple(Screen.Reports, "Reports", Icons.Default.BarChart),
                    Triple(Screen.Profile, "Profile", Icons.Default.Person)
                )

                items.forEach { (screen, label, icon) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        icon = { Icon(icon, contentDescription = null) },
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    onClick = {
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = Screen.Login.route) {
            composable(Screen.Login.route) { 
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onRegisterClick = { navController.navigate(Screen.Register.route) }
                ) 
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    transactions = transactions,
                    onAddTransaction = { new -> 
                        val updated = transactions.toMutableList()
                        updated.add(new)
                        transactions = updated
                    },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                ) 
            }
            composable(Screen.Reports.route) { 
                ReportsScreen(
                    transactions = transactions,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                ) 
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(onBack = { navController.popBackStack() }) 
            }
        }
    }
}
