package com.estoyok.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.estoyok.app.features.auth.presentation.AuthViewModel
import com.estoyok.app.features.auth.presentation.login.LoginScreen
import com.estoyok.app.features.auth.presentation.register.RegisterScreen
import com.estoyok.app.features.auth.presentation.verify.VerifyEmailScreen
import com.estoyok.app.features.tracking.presentation.FamiliaScreen
import com.estoyok.app.features.tracking.presentation.MapaScreen
import com.estoyok.app.features.wellbeing.presentation.AjustesScreen
import com.estoyok.app.features.wellbeing.presentation.PanelScreen

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    val items = listOf(
        Screen.Panel,
        Screen.Mapa,
        Screen.Familia,
        Screen.Ajustes
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be visible (only for authenticated, main tab screens)
    val showBottomBar = isAuthenticated && items.any { it.route == currentRoute }

    // Redirect logic: whenever auth state changes, enforce correct routes
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            // If logged out and not already on auth screens, route to Login
            val isAuthScreen = currentRoute == Screen.Login.route || 
                               currentRoute == Screen.Register.route || 
                               currentRoute?.startsWith("verify_email") == true
            if (!isAuthScreen) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // If authenticated and on login/register/verify-email, route to Panel
            val isAuthScreen = currentRoute == null ||
                               currentRoute == Screen.Login.route || 
                               currentRoute == Screen.Register.route || 
                               currentRoute.startsWith("verify_email")
            if (isAuthScreen) {
                navController.navigate(Screen.Panel.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route, // Default starting point is Login
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Screens
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        // Managed by LaunchedEffect observing isAuthenticated
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = { email ->
                        navController.navigate(Screen.VerifyEmail.createRoute(email))
                    }
                )
            }
            
            composable(Screen.VerifyEmail.route) {
                VerifyEmailScreen(
                    onVerificationSuccess = {
                        // Managed by LaunchedEffect observing isAuthenticated
                    }
                )
            }

            // Main Screens
            composable(Screen.Panel.route) { PanelScreen() }
            composable(Screen.Mapa.route) { MapaScreen() }
            composable(Screen.Familia.route) { FamiliaScreen() }
            composable(Screen.Ajustes.route) { AjustesScreen() }
        }
    }
}
