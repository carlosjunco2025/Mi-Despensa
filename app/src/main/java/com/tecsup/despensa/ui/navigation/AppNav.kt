package com.tecsup.despensa.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.despensa.AppContainer
import com.tecsup.despensa.domain.model.Rol
import com.tecsup.despensa.domain.model.Sesion
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

@Composable
fun AppNav(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val viewModelFactory = remember(container) { ViewModelFactory(container) }
    val sesionViewModel: SesionViewModel = viewModel(factory = viewModelFactory)
    val sesionEstado by sesionViewModel.estado.collectAsStateWithLifecycle()

    when (val estadoSesion = sesionEstado) {
        UiState.Cargando -> Cargando(modifier = modifier)

        is UiState.Error -> ErrorView(
            mensaje = estadoSesion.mensaje,
            onReintentar = sesionViewModel::cargarSesion,
            modifier = modifier
        )

        is UiState.Exito -> ContenidoSesion(
            sesion = estadoSesion.datos,
            viewModelFactory = viewModelFactory,
            sesionViewModel = sesionViewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoSesion(
    sesion: Sesion?,
    viewModelFactory: ViewModelFactory,
    sesionViewModel: SesionViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val inicio = remember { if (sesion != null) Rutas.LISTA else Rutas.LOGIN }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val esAdmin = sesion?.rol == Rol.ADMIN
    val mostrarBarras = sesion != null && rutaActual in setOf(
        Rutas.LISTA,
        Rutas.FAVORITOS,
        Rutas.ADMIN
    )

    fun irA(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(Rutas.LISTA) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = { Text(tituloDe(rutaActual)) },
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
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (mostrarBarras) {
                NavigationBar {
                    NavigationBarItem(
                        selected = rutaActual == Rutas.LISTA,
                        onClick = { irA(Rutas.LISTA) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Catálogo"
                            )
                        },
                        label = { Text("Catálogo") }
                    )
                    NavigationBarItem(
                        selected = rutaActual == Rutas.FAVORITOS,
                        onClick = { irA(Rutas.FAVORITOS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Mi lista"
                            )
                        },
                        label = { Text("Mi lista") }
                    )
                    if (esAdmin) {
                        NavigationBarItem(
                            selected = rutaActual == Rutas.ADMIN,
                            onClick = { irA(Rutas.ADMIN) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Admin"
                                )
                            },
                            label = { Text("Admin") }
                        )
                    }
                }
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
                val favoritosViewModel: FavoritosViewModel = viewModel(factory = viewModelFactory)
                FavoritosScreen(
                    viewModel = favoritosViewModel,
                    onItemClick = { id -> navController.navigate(Rutas.detalle(id)) }
                )
            }

            composable(Rutas.ADMIN) {
                if (esAdmin) {
                    AdminScreen()
                } else {
                    AccesoDenegado(
                        onVolver = {
                            navController.navigate(Rutas.LISTA) {
                                popUpTo(Rutas.LISTA) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun tituloDe(ruta: String?): String = when (ruta) {
    Rutas.FAVORITOS -> "Mi lista de compras"
    Rutas.ADMIN -> "Panel Admin"
    else -> "Despensa"
}

@Composable
private fun AccesoDenegado(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No tienes permisos para entrar al panel Admin",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVolver) {
            Text("Volver al catálogo")
        }
    }
}
