package com.example.dreamfunds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dreamfunds.ui.components.drawer.AppDrawer
import com.example.dreamfunds.ui.navigation.AppNavHost
import com.example.dreamfunds.ui.screens.NoConnectionScreen
import com.example.dreamfunds.ui.theme.DreamFundsTheme
import com.example.dreamfunds.utils.observeNetworkConnectivity
import com.example.dreamfunds.viewmodel.AuthViewModel
import com.example.dreamfunds.viewmodel.SessionState
import com.example.dreamfunds.viewmodel.ThemeMode
import com.example.dreamfunds.viewmodel.ThemeViewModel
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Screens
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Register  : Screen("register")
    object Dashboard : Screen("dashboard")
    object Reports   : Screen("reports")
    object Profile   : Screen("profile")
}

// ---------------------------------------------------------------------------
// Activity
// ---------------------------------------------------------------------------

class MainActivity : ComponentActivity() {

    private val authViewModel:  AuthViewModel  by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Log.d("DeepLink", "onCreate called")
        handleSupabaseDeepLink(intent)

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val useDark = when (themeMode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DreamFundsTheme(darkTheme = useDark) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DreamFundsApp(
                        authViewModel  = authViewModel,
                        themeViewModel = themeViewModel,
                    )
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
            authViewModel.refreshSessionAndProfile()
        } catch (e: Exception) {
            Log.e("DeepLink", "handleDeeplinks failed: ${e.message}", e)
        }
    }
}

// ---------------------------------------------------------------------------
// Root composable
// ---------------------------------------------------------------------------

@Composable
fun DreamFundsApp(
    authViewModel  : AuthViewModel,
    themeViewModel : ThemeViewModel,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sessionState by authViewModel.sessionState.collectAsState()
    val profile by authViewModel.profile.collectAsState()

    // Real-time network state — emits instantly on connectivity changes
    val isOnline by observeNetworkConnectivity(context)
        .collectAsStateWithLifecycle(initialValue = true)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Only block with the no-connection overlay AFTER the splash screen is done.
    // During splash / auth screens we let the flow continue normally.
    val isSplashOrAuth = currentRoute in setOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
    )

    // Show the overlay when offline AND past the splash/auth phase
    val showNoConnection = !isOnline && !isSplashOrAuth

    val isAuthScreen = isSplashOrAuth || showNoConnection

    // Navigate based on session state (connectivity handled separately via overlay)
    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.LoggedIn -> {
                authViewModel.loadProfile()
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            SessionState.LoggedOut -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            SessionState.Loading -> Unit
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthScreen,
        drawerContent = {
            AppDrawer(
                profile = profile,
                currentDestination = currentDestination,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
        // The nav host always stays in the composition so nav state is preserved.
        // The no-connection screen is layered on top via AnimatedVisibility.
        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            onOpenDrawer = { scope.launch { drawerState.open() } },
        )

        // Overlay that covers everything when offline (post-splash)
        AnimatedVisibility(
            visible = showNoConnection,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            NoConnectionScreen(
                onRetry = { /* network flow will auto-dismiss when online */ }
            )
        }
    }
}