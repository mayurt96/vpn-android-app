package com.bunnyvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bunnyvpn.navigation.*
import com.bunnyvpn.ui.screens.*
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel
import com.bunnyvpn.service.BunnyVpnService
import android.content.Intent
import android.net.VpnService

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.toggleVpn()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
            val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()

            // Handle VPN Permission request
            LaunchedEffect(vpnState) {
                if (vpnState == com.bunnyvpn.model.VpnState.CONNECTING) {
                    val intent = VpnService.prepare(this@MainActivity)
                    if (intent != null) {
                        vpnPermissionLauncher.launch(intent)
                    }
                }
            }

            BunnyVPNTheme(appTheme = appTheme) {
                BunnyVPNApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BunnyVPNApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val mainScreens = listOf(
        Screen.Home.route,
        Screen.Servers.route,
        Screen.Stats.route,
        Screen.Browser.route,
        Screen.Profile.route,
        Screen.Settings.route
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BunnyBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 }
            },
            popEnterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
            },
            popExitTransition = {
                fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 }
            }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = isLoggedIn,
                    onFinished = { loggedIn ->
                        val destination = if (loggedIn) Screen.Home.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateSignup = {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = viewModel,
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }

            composable(Screen.Browser.route) {
                BrowserScreen()
            }

            composable(Screen.Servers.route) {
                ServersScreen(viewModel = viewModel)
            }

            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BunnyBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val appTheme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                )
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (appTheme == AppTheme.LIGHT) MaterialTheme.colorScheme.surface
                    else GlassWhite
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(Cyan400.copy(0.3f), Purple400.copy(0.3f))),
                    RoundedCornerShape(28.dp)
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true

                val iconColor by animateColorAsState(
                    targetValue = if (selected) Cyan400 else TextMuted,
                    animationSpec = tween(300),
                    label = "icon_color"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "icon_scale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Selection glow
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Brush.radialGradient(listOf(Cyan400.copy(0.2f), Color.Transparent)),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                        )
                    }
                    Text(
                        text = item.label,
                        color = iconColor,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
