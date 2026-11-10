package com.tecsup.despensa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.despensa.ui.admin.AdminScreen
import com.tecsup.despensa.ui.detalle.DetalleScreen
import com.tecsup.despensa.ui.favoritos.FavoritosScreen
import com.tecsup.despensa.ui.lista.ListaScreen
import com.tecsup.despensa.ui.login.LoginScreen

object Rutas {
    const val LISTA = "lista"
    const val LOGIN = "login"
    const val DETALLE = "detalle/{id}"
    const val FAVORITOS = "favoritos"
    const val ADMIN = "admin"

    fun detalle(id: Int) = "detalle/$id"
}

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA,
        modifier = modifier
    ) {
        composable(Rutas.LISTA) {
            ListaScreen()
        }

        composable(Rutas.LOGIN) {
            LoginScreen()
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetalleScreen(id = id)
        }

        composable(Rutas.FAVORITOS) {
            FavoritosScreen()
        }

        composable(Rutas.ADMIN) {
            AdminScreen()
        }
    }
}
