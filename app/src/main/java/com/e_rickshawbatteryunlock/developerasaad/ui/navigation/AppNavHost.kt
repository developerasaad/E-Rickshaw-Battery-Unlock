package com.e_rickshawbatteryunlock.developerasaad.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.e_rickshawbatteryunlock.developerasaad.presentation.dashboard.DashboardScreen
import com.e_rickshawbatteryunlock.developerasaad.presentation.password.PasswordScreen
import com.e_rickshawbatteryunlock.developerasaad.presentation.recovery.RecoveryScreen
import com.e_rickshawbatteryunlock.developerasaad.presentation.saved.SavedBatteriesScreen
import com.e_rickshawbatteryunlock.developerasaad.presentation.scan.ScanScreen

/**
 * Root navigation host for the application.
 *
 * The [ScanScreen] is the start destination. All other screens are reachable
 * from there via the navigation callbacks provided to each composable.
 *
 * Navigation is one-directional (no deep links required at this stage).
 * Back navigation is handled automatically by the system back button via
 * the NavController back stack.
 *
 * @param navController Optional controller — injectable for testing.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scan.route,
    ) {
        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        // Pop scan off stack — user should use Disconnect to return
                        popUpTo(Screen.Scan.route) { inclusive = false }
                    }
                },
                onNavigateToSaved = {
                    navController.navigate(Screen.SavedBatteries.route)
                },
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToRecovery = {
                    navController.navigate(Screen.Recovery.route)
                },
                onNavigateToPassword = {
                    navController.navigate(Screen.Password.route)
                },
                onDisconnected = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Recovery.route) {
            RecoveryScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.SavedBatteries.route) {
            SavedBatteriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Scan.route) { inclusive = false }
                    }
                },
            )
        }

        composable(Screen.Password.route) {
            PasswordScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
