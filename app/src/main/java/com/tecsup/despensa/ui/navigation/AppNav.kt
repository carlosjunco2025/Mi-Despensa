package com.tecsup.despensa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.despensa.AppContainer
import com.tecsup.despensa.ui.ViewModelFactory
import com.tecsup.despensa.ui.admin.AdminScreen
import com.tecsup.despensa.ui.detalle.DetalleScreen
import com.tecsup.despensa.ui.favoritos.FavoritosScreen
import com.tecsup.despensa.ui.lista.ListaScreen
import com.tecsup.despensa.ui.lista.ListaViewModel
import com.tecsup.despensa.ui.login.LoginScreen
import com.tecsup.despensa.ui.login.LoginViewModel

object Rutas {
    const val LISTA = "lista"
    const val LOGIN = "login"
    const val DETALLE = "detalle/{id}"
    const val FAVORITOS = "favoritos"
    const val ADMIN = "admin"

    fun detalle(id: Int) = "detalle/$id"
}

@Composable
fun AppNav(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val viewModelFactory = remember(container) { ViewModelFactory(container) }

    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA,
        modifier = modifier
    ) {
        composable(Rutas.LISTA) {
            val listaViewModel: ListaViewModel = viewModel(factory = viewModelFactory)
            ListaScreen(viewModel = listaViewModel)
        }

        composable(Rutas.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {}
            )
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
