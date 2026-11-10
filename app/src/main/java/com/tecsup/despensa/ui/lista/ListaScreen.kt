package com.tecsup.despensa.ui.lista

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tecsup.despensa.ui.UiState
import com.tecsup.despensa.ui.components.Cargando
import com.tecsup.despensa.ui.components.ErrorView

@Composable
fun ListaScreen(
    viewModel: ListaViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is UiState.Cargando -> Cargando(modifier = modifier)

        is UiState.Error -> ErrorView(
            mensaje = actual.mensaje,
            onReintentar = {},
            modifier = modifier
        )

        is UiState.Exito -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("${actual.datos.size} productos")
        }
    }
}
