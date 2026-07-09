package com.estoyok.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object EstoyOk : Screen("estoyok", "Estoy ok", Icons.Default.Shield)
    object Mapa : Screen("mapa", "Mapa", Icons.Default.LocationOn)
    object Vehiculo : Screen("vehiculo", "Vehículo", Icons.Default.Directions)
    object Familia : Screen("familia", "Familia", Icons.Default.People)
    object Premium : Screen("premium", "Premium", Icons.Default.Star)
    object Ajustes : Screen("ajustes", "Ajustes", Icons.Default.Settings)
    
    // Auth screens (not in bottom bar)
    object Login : Screen("login", "Ingresar", Icons.Default.Home)
    object Register : Screen("register", "Registrarse", Icons.Default.Home)
    object VerifyEmail : Screen("verify_email/{email}", "Verificar Email", Icons.Default.Home) {
        fun createRoute(email: String) = "verify_email/$email"
    }
}
