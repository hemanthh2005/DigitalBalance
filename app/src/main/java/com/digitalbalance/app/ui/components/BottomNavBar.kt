package com.digitalbalance.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.digitalbalance.app.ui.navigation.bottomNavScreens
import com.digitalbalance.app.ui.theme.DarkNavySurface
import com.digitalbalance.app.ui.theme.DarkNavySurfaceVariant
import com.digitalbalance.app.ui.theme.StitchPrimaryIndigo
import com.digitalbalance.app.ui.theme.TextMutedSecondary
import com.digitalbalance.app.ui.theme.TextWhitePrimary

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = com.digitalbalance.app.ui.theme.StitchDarkBackground
    ) {
        bottomNavScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    screen.icon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = screen.title)
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.digitalbalance.app.ui.theme.StitchCyanTeal,
                    selectedTextColor = com.digitalbalance.app.ui.theme.StitchCyanTeal,
                    indicatorColor = Color(0xFF162E35),
                    unselectedIconColor = TextMutedSecondary,
                    unselectedTextColor = TextMutedSecondary
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

