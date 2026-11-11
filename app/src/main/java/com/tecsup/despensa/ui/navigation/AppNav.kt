package com.tecsup.despensa.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.despensa.AppContainer
import com.tecsup.despensa.ui.UiState
import com.tecsup.despensa.ui.ViewModelFactory
import com.tecsup.despensa.ui.admin.AdminScreen
import com.tecsup.despensa.ui.components.Cargando
import com.tecsup.despensa.ui.components.ErrorView
import com.tecsup.despensa.ui.detalle.DetalleScreen
import com.tecsup.despensa.ui.detalle.DetalleViewModel
import com.tecsup.despensa.ui.favoritos.FavoritosScreen
import com.tecsup.despensa.ui.favoritos.FavoritosViewModel
import com.tecsup.despensa.ui.lista.ListaScreen
import com.tecsup.despensa.ui.lista.ListaViewModel
import com.tecsup.despensa.ui.login.LoginScreen
import com.tecsup.despensa.ui.login.LoginViewModel
import com.tecsup.despensa.ui.sesion.SesionViewModel

object Rutas {
    const val LISTA = "lista"
    const val LOGIN = "login"
    const val DETALLE = "detalle/{id}"
    const val FAVORITOS = "favoritos"
    const val ADMIN = "admin"

    fun detalle(id: Int) = "detalle/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val viewModelFactory = remember(container) { ViewModelFactory(container) }
    val sesionViewModel: SesionViewModel = viewModel(factory = viewModelFactory)
    val sesionEstado by sesionViewModel.estado.collectAsStateWithLifecycle()

    when (val estadoSesion = sesionEstado) {
        is UiState.Cargando -> Cargando(modifier = modifier)

        is UiState.Error -> ErrorView(
            mensaje = estadoSesion.mensaje,
            onReintentar = sesionViewModel::cargarSesion,
            modifier = modifier
        )

        is UiState.Exito -> {
            val sesion = estadoSesion.datos
            val navController = rememberNavController()
            val inicio = remember { if (sesion != null) Rutas.LISTA else Rutas.LOGIN }
            val backStackEntry by navController.currentBackStackEntryAsState()
            val rutaActual = backStackEntry?.destination?.route
            val mostrarBarra = sesion != null && rutaActual in setOf(
                Rutas.LISTA,
                Rutas.FAVORITOS,
                Rutas.ADMIN
            )

            Scaffold(
                modifier = modifier,
                topBar = {
                    if (mostrarBarra) {
                        TopAppBar(
                            title = { Text("Despensa") },
                            actions = {
                                IconButton(
                                    onClick = {
                                        sesionViewModel.cerrarSesion()
                                        navController.navigate(Rutas.LOGIN) {
                                            popUpTo(Rutas.LISTA) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Cerrar sesión"
                                    )
                                }
                            }
                        )
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = inicio,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(Rutas.LISTA) {
                        val listaViewModel: ListaViewModel = viewModel(factory = viewModelFactory)
                        ListaScreen(
                            viewModel = listaViewModel,
                            onItemClick = { id -> navController.navigate(Rutas.detalle(id)) }
                        )
                    }

                    composable(Rutas.LOGIN) {
                        val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginExitoso = { sesionIniciada ->
                                sesionViewModel.establecerSesion(sesionIniciada)
                                navController.navigate(Rutas.LISTA) {
                                    popUpTo(Rutas.LOGIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(
                        route = Rutas.DETALLE,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: 0
                        val detalleViewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                        DetalleScreen(
                            id = id,
                            viewModel = detalleViewModel,
                            onVolver = { navController.popBackStack() }
                        )
                    }

                    composable(Rutas.FAVORITOS) {
                        val favoritosViewModel: FavoritosViewModel =
                            viewModel(factory = viewModelFactory)
                        FavoritosScreen(
                            viewModel = favoritosViewModel,
                            onItemClick = { id -> navController.navigate(Rutas.detalle(id)) }
                        )
                    }

                    composable(Rutas.ADMIN) {
                        AdminScreen()
                    }
                }
            }
        }
    }
}
