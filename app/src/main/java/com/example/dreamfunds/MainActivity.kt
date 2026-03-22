package com.example.dreamfunds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dreamfunds.ui.components.drawer.AppDrawer
import com.example.dreamfunds.ui.navigation.AppNavHost
import com.example.dreamfunds.ui.theme.DreamFundsTheme
import com.example.dreamfunds.viewmodel.AuthViewModel
import com.example.dreamfunds.viewmodel.SessionState
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.dreamfunds.ui.theme.DreamFundsTheme



sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Register  : Screen("register")
    object Dashboard : Screen("dashboard")
    object Reports   : Screen("reports")
    object Profile   : Screen("profile")
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Log.d("DeepLink", "onCreate called")
        handleSupabaseDeepLink(intent)
        setContent {
            DreamFundsTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DreamFundsApp(authViewModel = authViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("DeepLink", "onNewIntent called")
        handleSupabaseDeepLink(intent)
    }

    private fun handleSupabaseDeepLink(intent: Intent?) {
        val uri: Uri = intent?.data ?: run {
            Log.d("DeepLink", "No data URI in intent")
            return
        }
        Log.d("DeepLink", "URI: $uri")
        Log.d("DeepLink", "Scheme: ${uri.scheme}, Host: ${uri.host}, Path: ${uri.path}, Fragment: ${uri.fragment}")

        if (uri.scheme != "dreamfunds") {
            Log.d("DeepLink", "Scheme is not dreamfunds, ignoring")
            return
        }

        val fragment = uri.fragment
        if (fragment == null || !fragment.contains("access_token")) {
            Log.d("DeepLink", "Fragment does not contain access_token, ignoring")
            return
        }

        Log.d("DeepLink", "Processing deep link...")
        try {
            SupabaseClientProvider.client.handleDeeplinks(intent)
            Log.d("DeepLink", "handleDeeplinks succeeded")
            // After processing, refresh session and profile
            authViewModel.refreshSessionAndProfile()
        } catch (e: Exception) {
            Log.e("DeepLink", "handleDeeplinks failed: ${e.message}", e)
        }
    }
}

@Composable
fun DreamFundsApp(authViewModel: AuthViewModel) {
    val navController                = rememberNavController()
    val drawerState                  = rememberDrawerState(DrawerValue.Closed)
    val scope                        = rememberCoroutineScope()

    val sessionState by authViewModel.sessionState.collectAsState()
    val profile      by authViewModel.profile.collectAsState()

    val navBackStackEntry  by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAuthScreen = currentDestination?.route in setOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.Splash.route,
    )

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.LoggedIn  -> {
                authViewModel.loadProfile()
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            SessionState.LoggedOut -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            SessionState.Loading   -> Unit
        }
    }

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = !isAuthScreen,
        drawerContent   = {

            AppDrawer(
                profile            = profile,
                currentDestination = currentDestination,
                authViewModel      = authViewModel,
                onNavigate         = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        },
    ) {
        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            onOpenDrawer  = { scope.launch { drawerState.open() } },
        )
    }
}