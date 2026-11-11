package com.tecsup.despensa.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.despensa.R
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import com.tecsup.despensa.ui.components.CanastaAnimada
import kotlinx.coroutines.launch

private enum class ModoAcceso { INGRESAR, REGISTRAR }

/**
 * Pantalla de acceso con dos modos (ingresar / crear cuenta) y acceso con
 * Google cuando el proyecto tiene configurado el ID de cliente web.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (Sesion) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var modo by rememberSaveable { mutableStateOf(ModoAcceso.INGRESAR) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var verClave by rememberSaveable { mutableStateOf(false) }
    var verConfirmacion by rememberSaveable { mutableStateOf(false) }

    val cargando = estado is UiState.Cargando
    val mensajeError = (estado as? UiState.Error)?.mensaje
    val clientIdGoogle = stringResource(R.string.google_web_client_id).trim()

    LaunchedEffect(estado) {
        val sesion = (estado as? UiState.Exito)?.datos
        if (sesion != null) {
            onLoginExitoso(sesion)
            viewModel.sesionConsumida()
        }
    }

    fun acceder() {
        if (modo == ModoAcceso.INGRESAR) {
            viewModel.iniciarSesion(correo, clave)
        } else {
            viewModel.registrar(nombre, correo, clave, confirmacion)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CanastaAnimada(tamano = 150.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Despensa",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Tu despensa inteligente, siempre lista",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SelectorModo(
                        modo = modo,
                        onCambiar = { nuevo ->
                            modo = nuevo
                            viewModel.limpiarError()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = modo,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 6 })
                                .togetherWith(
                                    fadeOut(tween(140)) + slideOutVertically(tween(140)) { -it / 6 }
                                )
                        },
                        label = "campos"
                    ) { modoActual ->
                        Column {
                            AnimatedVisibility(visible = modoActual == ModoAcceso.REGISTRAR) {
                                Column {
                                    OutlinedTextField(
                                        value = nombre,
                                        onValueChange = { nombre = it },
                                        label = { Text("Nombre completo") },
                                        singleLine = true,
                                        enabled = !cargando,
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            OutlinedTextField(
                                value = correo,
                                onValueChange = { correo = it },
                                label = { Text("Correo electrónico") },
                                singleLine = true,
                                enabled = !cargando,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = clave,
                                onValueChange = { clave = it },
                                label = { Text("Contraseña") },
                                singleLine = true,
                                enabled = !cargando,
                                shape = RoundedCornerShape(16.dp),
                                visualTransformation = if (verClave) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    Text(
                                        text = if (verClave) "Ocultar" else "Ver",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable { verClave = !verClave }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (modoActual == ModoAcceso.REGISTRAR) {
                                        ImeAction.Next
                                    } else {
                                        ImeAction.Done
                                    }
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { if (!cargando) acceder() }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            AnimatedVisibility(visible = modoActual == ModoAcceso.REGISTRAR) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = confirmacion,
                                        onValueChange = { confirmacion = it },
                                        label = { Text("Repite la contraseña") },
                                        singleLine = true,
                                        enabled = !cargando,
                                        shape = RoundedCornerShape(16.dp),
                                        visualTransformation = if (verConfirmacion) {
                                            VisualTransformation.None
                                        } else {
                                            PasswordVisualTransformation()
                                        },
                                        trailingIcon = {
                                            Text(
                                                text = if (verConfirmacion) "Ocultar" else "Ver",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable { verConfirmacion = !verConfirmacion }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { if (!cargando) acceder() }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = mensajeError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                        ) {
                            Text(
                                text = mensajeError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { acceder() },
                        enabled = !cargando,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (modo == ModoAcceso.INGRESAR) {
                                    "Ingresar"
                                } else {
                                    "Crear mi cuenta"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    if (clientIdGoogle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  o  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    obtenerIdTokenGoogle(context, clientIdGoogle)
                                        .onSuccess { token -> viewModel.iniciarConGoogle(token) }
                                        .onFailure {
                                            viewModel.reportarError(
                                                "No se pudo iniciar con Google. " +
                                                    "Revisa tu conexión e inténtalo de nuevo."
                                            )
                                        }
                                }
                            },
                            enabled = !cargando,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "Continuar con Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = {
                    modo = if (modo == ModoAcceso.INGRESAR) {
                        ModoAcceso.REGISTRAR
                    } else {
                        ModoAcceso.INGRESAR
                    }
                    viewModel.limpiarError()
                },
                enabled = !cargando
            ) {
                Text(
                    text = if (modo == ModoAcceso.INGRESAR) {
                        "¿No tienes cuenta? Crear una"
                    } else {
                        "¿Ya tienes cuenta? Ingresar"
                    },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Al continuar aceptas cuidar tu despensa con cariño 🌿",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SelectorModo(
    modo: ModoAcceso,
    onCambiar: (ModoAcceso) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(5.dp)) {
            OpcionModo(
                texto = "Ingresar",
                seleccionada = modo == ModoAcceso.INGRESAR,
                onClick = { onCambiar(ModoAcceso.INGRESAR) },
                modifier = Modifier.weight(1f)
            )
            OpcionModo(
                texto = "Crear cuenta",
                seleccionada = modo == ModoAcceso.REGISTRAR,
                onClick = { onCambiar(ModoAcceso.REGISTRAR) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OpcionModo(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionada) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall,
            color = if (seleccionada) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}
