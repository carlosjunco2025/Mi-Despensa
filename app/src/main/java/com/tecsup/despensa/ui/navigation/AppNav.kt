package com.tecsup.despensa.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import com.tecsup.despensa.ui.ViewModelFactory
import com.tecsup.despensa.ui.admin.AdminScreen
import com.tecsup.despensa.ui.admin.AdminViewModel
import com.tecsup.despensa.ui.components.Cargando
import com.tecsup.despensa.ui.components.ErrorView
import com.tecsup.despensa.ui.components.PedirPermisoNotificaciones
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

    val esAdmin = sesion?.esAdmin == true

    if (sesion != null) {
        PedirPermisoNotificaciones()
    }

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

    fun salir() {
        sesionViewModel.cerrarSesion()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            val sesionVisible = if (mostrarBarras) sesion else null
            if (sesionVisible != null) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarSesion(sesion = sesionVisible)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = saludoDe(rutaActual, sesionVisible),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = subtituloDe(rutaActual, sesionVisible),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                        alpha = 0.75f
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { salir() }) {
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
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(220)) + slideInHorizontally(tween(220)) { ancho -> ancho / 12 }
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = {
                fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { ancho -> ancho / 12 }
            }
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
                    val adminViewModel: AdminViewModel = viewModel(factory = viewModelFactory)
                    AdminScreen(viewModel = adminViewModel)
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

@Composable
private fun AvatarSesion(sesion: Sesion) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(38.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sesion.inicial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private fun saludoDe(ruta: String?, sesion: Sesion): String {
    val nombre = sesion.nombre.trim().substringBefore(" ").ifBlank { "vecino" }
    return when (ruta) {
        Rutas.FAVORITOS -> "Mi lista de compras"
        Rutas.ADMIN -> "Panel de administración"
        else -> "Hola, $nombre"
    }
}

private fun subtituloDe(ruta: String?, sesion: Sesion): String = when (ruta) {
    Rutas.FAVORITOS -> "Lo que planeas comprar"
    Rutas.ADMIN -> sesion.correo
    else -> "Tu despensa, siempre lista"
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
