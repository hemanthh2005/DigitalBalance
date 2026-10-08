package com.digitalbalance.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.digitalbalance.app.ui.screens.auth.LoginScreen
import com.digitalbalance.app.ui.screens.auth.SignUpScreen
import com.digitalbalance.app.ui.screens.childmode.ChildModeScreen
import com.digitalbalance.app.ui.screens.home.HomeScreen
import com.digitalbalance.app.ui.screens.insights.InsightsScreen
import com.digitalbalance.app.ui.screens.reports.ReportsScreen
import com.digitalbalance.app.ui.screens.settings.SettingsScreen
import com.digitalbalance.app.ui.screens.verification.AdultVerificationScreen

@Composable
fun DigitalBalanceNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController)
        }
        composable(Screen.ChildMode.route) {
            ChildModeScreen(navController = navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.AdultVerification.route) {
            AdultVerificationScreen(navController = navController)
        }
    }
}
