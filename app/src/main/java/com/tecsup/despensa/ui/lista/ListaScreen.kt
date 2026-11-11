package com.tecsup.despensa.ui.lista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as itemsFila
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.domain.model.Producto
import com.tecsup.despensa.ui.comoPrecio
import com.tecsup.despensa.ui.components.Cargando
import com.tecsup.despensa.ui.components.ErrorView
import com.tecsup.despensa.ui.components.IlustracionVacia

/**
 * Pantalla principal: buscador, filtros por categoria, orden, productos propios
 * del administrador y el catalogo en tarjetas.
 */
@Composable
fun ListaScreen(
    viewModel: ListaViewModel,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var consultaLocal by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbar.showSnackbar(aviso.orEmpty())
            viewModel.avisoMostrado()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Encabezado(
                consulta = consultaLocal,
                onConsulta = {
                    consultaLocal = it
                    viewModel.buscar(it)
                },
                onLimpiar = {
                    consultaLocal = ""
                    viewModel.buscar("")
                }
            )

            if (estado.categorias.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = estado.categoria == null,
                            onClick = { viewModel.filtrarPor(null) },
                            label = { Text("Todo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    itemsFila(estado.categorias) { categoria ->
                        FilterChip(
                            selected = estado.categoria == categoria,
                            onClick = { viewModel.filtrarPor(categoria) },
                            label = {
                                Text(
                                    text = categoria.replaceFirstChar { letra ->
                                        letra.uppercase()
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordenar:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OrdenLista.entries.forEach { orden ->
                    AssistChip(
                        onClick = { viewModel.ordenar(orden) },
                        label = { Text(orden.etiqueta) },
                        colors = if (estado.orden == orden) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        }
                    )
                }
            }

            when {
                estado.cargando && estado.totalCatalogo == 0 -> Cargando()

                estado.error != null && estado.totalCatalogo == 0 -> ErrorView(
                    mensaje = estado.error.orEmpty(),
                    onReintentar = viewModel::cargar
                )

                estado.sinResultados -> SinResultados(
                    consulta = estado.consulta,
                    onLimpiar = {
                        consultaLocal = ""
                        viewModel.buscar("")
                        viewModel.filtrarPor(null)
                    }
                )

                else -> Catalogo(
                    estado = estado,
                    onItemClick = onItemClick,
                    onFavorito = viewModel::alternarFavorito,
                    onProductoPropio = { productoSeleccionado = it }
                )
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    productoSeleccionado?.let { producto ->
        DialogoProducto(
            producto = producto,
            onCerrar = { productoSeleccionado = null }
        )
    }
}

@Composable
private fun Encabezado(
    consulta: String,
    onConsulta: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = consulta,
            onValueChange = onConsulta,
            placeholder = { Text("Buscar en tu despensa...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = consulta.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(onClick = onLimpiar) {
                        Text("Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Catalogo(
    estado: ListaUiState,
    onItemClick: (Int) -> Unit,
    onFavorito: (Int) -> Unit,
    onProductoPropio: (Producto) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (estado.propios.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SeccionPropios(
                    productos = estado.propios,
                    onProducto = onProductoPropio
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catálogo",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${estado.catalogo.size} productos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(estado.catalogo, key = { item -> item.id }) { item ->
            TarjetaItem(
                item = item,
                onClick = { onItemClick(item.id) },
                onFavorito = { onFavorito(item.id) }
            )
        }
    }
}

@Composable
private fun SeccionPropios(
    productos: List<Producto>,
    onProducto: (Producto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mi despensa",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Productos creados por el administrador",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "${productos.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsFila(productos, key = { producto -> producto.id }) { producto ->
                TarjetaPropia(
                    producto = producto,
                    onClick = { onProducto(producto) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun TarjetaPropia(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.width(152.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = producto.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = producto.precio.comoPrecio(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun TarjetaItem(
    item: Item,
    onClick: () -> Unit,
    onFavorito: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = item.imagenUrl,
                contentDescription = item.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            )

            IconButton(
                onClick = onFavorito,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (item.esFavorito) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (item.esFavorito) {
                        "Quitar de mi lista"
                    } else {
                        "Agregar a mi lista"
                    },
                    tint = if (item.esFavorito) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.precio.comoPrecio(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SinResultados(
    consulta: String,
    onLimpiar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IlustracionVacia()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (consulta.isBlank()) {
                "Tu despensa está vacía por ahora"
            } else {
                "No encontramos resultados para \"$consulta\""
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Prueba con otra palabra o cambia de categoría.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onLimpiar) {
            Text("Ver todo el catálogo")
        }
    }
}

@Composable
private fun DialogoProducto(
    producto: Producto,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        },
        icon = {
            Text(
                text = producto.emoji,
                style = MaterialTheme.typography.displaySmall
            )
        },
        title = {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = producto.categoria,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = producto.descripcion.ifBlank { "Sin descripción." },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Precio: ${producto.precio.comoPrecio()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Disponibles: ${producto.stock}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
