package com.bunnyvpn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Servers : Screen("servers")
    object Stats : Screen("stats")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Browser : Screen("browser")
    object Chat : Screen("chat")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Shield),
    BottomNavItem(Screen.Servers, "Servers", Icons.Filled.Public),
    BottomNavItem(Screen.Stats, "Stats", Icons.Filled.BarChart),
    BottomNavItem(Screen.Browser, "Browser", Icons.Filled.Language),
    BottomNavItem(Screen.Chat, "AI Chat", Icons.Filled.AutoAwesome),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)
