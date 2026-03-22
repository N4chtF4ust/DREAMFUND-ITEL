package com.example.dreamfunds.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dreamfunds.Screen
import com.example.dreamfunds.ui.screens.splash.*
import com.example.dreamfunds.ui.screens.*
import com.example.dreamfunds.viewmodel.AuthViewModel

/**
 * Central route table for the app.
 *
 * Every [composable] entry lives here. To add a new screen:
 *  1. Declare a new [Screen] object in Screen.kt.
 *  2. Add a `composable(Screen.MyNew.route) { … }` block below.
 *  3. Optionally add it to [primaryNavItems] in NavigationItems.kt.
 *
 * @param navController   The app-level nav controller.
 * @param authViewModel   Shared auth state passed to auth screens.
 * @param onOpenDrawer    Callback wired to the hamburger menu on main screens.
 */
@Composable
fun AppNavHost(
    navController : NavHostController,
    authViewModel : AuthViewModel,
    onOpenDrawer  : () -> Unit,
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
    ) {

        composable(Screen.Splash.route) {
            SplashScreen()
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel   = authViewModel,
                onLoginSuccess  = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel     = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() },
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(onOpenDrawer = onOpenDrawer)
        }

        composable(Screen.Reports.route) {
            ReportsScreen(onOpenDrawer = onOpenDrawer)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack        = { navController.popBackStack() },
            )
        }
    }
}
