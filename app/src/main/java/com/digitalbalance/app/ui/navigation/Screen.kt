package com.digitalbalance.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "Sign Up")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Insights : Screen("insights", "Insights", Icons.Default.Insights)
    object ChildMode : Screen("child_mode", "Child Mode", Icons.Default.ChildCare)
    object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object ManageProtectedApps : Screen("manage_protected_apps", "Protected Apps")
    object AdultVerification : Screen("adult_verification", "Adult Verification")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Insights,
    Screen.ChildMode,
    Screen.Reports,
    Screen.Settings
)
