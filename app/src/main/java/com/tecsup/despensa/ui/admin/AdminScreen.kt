package com.tecsup.despensa.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.despensa.domain.model.Producto
import com.tecsup.despensa.ui.comoPrecio

private val EMOJIS = listOf(
    "🧺", "🍎", "🥛", "🍞", "🧀", "🥕", "🍚", "🥚",
    "🧴", "🧻", "🍫", "☕", "🍌", "🥑", "🍅", "🧼"
)

private val CATEGORIAS = listOf(
    "Frutas", "Verduras", "Lácteos", "Panadería",
    "Carnes", "Bebidas", "Limpieza", "General"
)

/**
 * Panel del administrador: estadisticas, alta/edicion/borrado de productos
 * propios y mantenimiento del catalogo.
 */
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var editandoId by rememberSaveable { mutableStateOf<String?>(null) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("General") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("🧺") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var porEliminar by remember { mutableStateOf<Producto?>(null) }

    fun limpiarFormulario() {
        editandoId = null
        nombre = ""
        categoria = "General"
        precio = ""
        stock = ""
        emoji = "🧺"
        descripcion = ""
    }

    fun cargarEnFormulario(producto: Producto) {
        editandoId = producto.id
        nombre = producto.nombre
        categoria = producto.categoria
        precio = if (producto.precio == 0.0) "" else producto.precio.toString()
        stock = producto.stock.toString()
        emoji = producto.emoji
        descripcion = producto.descripcion
    }

    val precioNumero = precio.replace(",", ".").toDoubleOrNull()
    val stockNumero = stock.toIntOrNull()
    val formularioValido = nombre.isNotBlank() &&
        (precioNumero ?: -1.0) >= 0.0 &&
        (stockNumero ?: -1) >= 0

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbar.showSnackbar(aviso.orEmpty())
            viewModel.avisoMostrado()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Panel de administración",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Gestiona tu despensa: crea, edita y elimina productos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(
                    emoji = "📦",
                    titulo = "Catálogo",
                    valor = estado.resumen.totalCatalogo.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaEstadistica(
                    emoji = "❤️",
                    titulo = "Favoritos",
                    valor = estado.resumen.totalFavoritos.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(
                    emoji = "🧺",
                    titulo = "Propios",
                    valor = estado.resumen.totalPropios.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaEstadistica(
                    emoji = "💰",
                    titulo = "Inventario",
                    valor = estado.resumen.valorInventario.comoPrecio(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = viewModel::sincronizar,
                    enabled = !estado.cargando,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Sincronizar")
                }
                OutlinedButton(
                    onClick = viewModel::limpiarCache,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Limpiar caché")
                }
            }

            if (estado.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = estado.error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FormularioProducto(
                editando = editandoId != null,
                nombre = nombre,
                onNombre = { nombre = it },
                categoria = categoria,
                onCategoria = { categoria = it },
                precio = precio,
                onPrecio = { precio = it },
                stock = stock,
                onStock = { stock = it },
                emoji = emoji,
                onEmoji = { emoji = it },
                descripcion = descripcion,
                onDescripcion = { descripcion = it },
                valido = formularioValido,
                guardando = estado.guardando,
                onGuardar = {
                    viewModel.guardar(
                        Producto(
                            id = editandoId.orEmpty(),
                            nombre = nombre,
                            categoria = categoria,
                            precio = precioNumero ?: 0.0,
                            stock = stockNumero ?: 0,
                            emoji = emoji,
                            descripcion = descripcion
                        )
                    ) { limpiarFormulario() }
                },
                onCancelar = { limpiarFormulario() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos propios",
                    style = MaterialTheme.typography.titleLarge
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${estado.productos.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                estado.cargando && estado.productos.isEmpty() -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Cargando productos...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                estado.productos.isEmpty() -> Text(
                    text = "Aún no hay productos propios. Crea el primero con el formulario de arriba.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> estado.productos.forEach { producto ->
                    FilaProducto(
                        producto = producto,
                        onEditar = { cargarEnFormulario(producto) },
                        onEliminar = { porEliminar = producto }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    porEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar producto") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${producto.nombre}\"? " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(producto)
                        porEliminar = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    emoji: String,
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormularioProducto(
    editando: Boolean,
    nombre: String,
    onNombre: (String) -> Unit,
    categoria: String,
    onCategoria: (String) -> Unit,
    precio: String,
    onPrecio: (String) -> Unit,
    stock: String,
    onStock: (String) -> Unit,
    emoji: String,
    onEmoji: (String) -> Unit,
    descripcion: String,
    onDescripcion: (String) -> Unit,
    valido: Boolean,
    guardando: Boolean,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (editando) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(
                        text = if (editando) "Editar producto" else "Nuevo producto",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Se guarda en la nube de tu despensa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombre,
                label = { Text("Nombre del producto") },
                singleLine = true,
                enabled = !guardando,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = onPrecio,
                    label = { Text("Precio (S/)") },
                    singleLine = true,
                    enabled = !guardando,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = onStock,
                    label = { Text("Stock") },
                    singleLine = true,
                    enabled = !guardando,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Categoría",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORIAS) { opcion ->
                    FilterChip(
                        selected = categoria == opcion,
                        onClick = { onCategoria(opcion) },
                        label = { Text(opcion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ícono",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(EMOJIS) { opcion ->
                    Surface(
                        shape = CircleShape,
                        color = if (emoji == opcion) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        onClick = { onEmoji(opcion) }
                    ) {
                        Text(
                            text = opcion,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcion,
                label = { Text("Descripción (opcional)") },
                enabled = !guardando,
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onGuardar,
                    enabled = valido && !guardando,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editando) "Guardar cambios" else "Agregar producto")
                }
                if (editando) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !guardando,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaProducto(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = producto.emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${producto.categoria} · ${producto.precio.comoPrecio()} · Stock ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar ${producto.nombre}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${producto.nombre}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
