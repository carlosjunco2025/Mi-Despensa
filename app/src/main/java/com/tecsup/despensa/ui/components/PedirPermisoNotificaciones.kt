package com.tecsup.despensa.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Pide POST_NOTIFICATIONS en Android 13 o superior la primera vez que se usa
 * la app. Si el usuario niega el permiso, la app continua sin notificaciones.
 */
@Composable
fun PedirPermisoNotificaciones() {
    val context = LocalContext.current

    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Se acepte o no, la app sigue funcionando. */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect

        val permiso = Manifest.permission.POST_NOTIFICATIONS
        val concedido = ContextCompat.checkSelfPermission(
            context,
            permiso
        ) == PackageManager.PERMISSION_GRANTED

        if (!concedido) {
            lanzador.launch(permiso)
        }
    }
}
