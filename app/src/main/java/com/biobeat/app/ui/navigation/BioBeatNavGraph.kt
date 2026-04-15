package com.biobeat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biobeat.app.ui.dashboard.DashboardScreen
import com.biobeat.app.ui.ecg.EcgScreen
import com.biobeat.app.ui.history.HistoryScreen
import com.biobeat.app.ui.notifications.NotificationsScreen
import com.biobeat.app.ui.scan.ScanScreen
import com.biobeat.app.ui.settings.DeviceSettingsScreen

object Routes {
    const val SCAN = "scan"
    const val DASHBOARD = "dashboard/{macAddress}"
    const val ECG = "ecg/{macAddress}"
    const val HISTORY = "history/{macAddress}"
    const val NOTIFICATIONS = "notifications/{macAddress}"
    const val SETTINGS = "settings/{macAddress}"

    fun dashboard(mac: String) = "dashboard/$mac"
    fun ecg(mac: String) = "ecg/$mac"
    fun history(mac: String) = "history/$mac"
    fun notifications(mac: String) = "notifications/$mac"
    fun settings(mac: String) = "settings/$mac"
}

@Composable
fun BioBeatNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SCAN) {
        composable(Routes.SCAN) {
            ScanScreen(
                onDeviceSelected = { macAddress ->
                    navController.navigate(Routes.dashboard(macAddress))
                }
            )
        }

        composable(
            route = Routes.DASHBOARD,
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType }),
        ) {
            DashboardScreen(
                onNavigateToEcg = { mac -> navController.navigate(Routes.ecg(mac)) },
                onNavigateToHistory = { mac -> navController.navigate(Routes.history(mac)) },
                onNavigateToNotifications = { mac -> navController.navigate(Routes.notifications(mac)) },
                onNavigateToSettings = { mac -> navController.navigate(Routes.settings(mac)) },
                onDisconnect = { navController.popBackStack(Routes.SCAN, false) },
            )
        }

        composable(
            route = Routes.ECG,
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType }),
        ) {
            EcgScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.HISTORY,
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType }),
        ) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.NOTIFICATIONS,
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType }),
        ) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.SETTINGS,
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType }),
        ) {
            DeviceSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
