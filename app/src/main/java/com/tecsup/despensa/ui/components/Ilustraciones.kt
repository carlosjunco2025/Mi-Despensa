package com.tecsup.despensa.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Canasta de compras dibujada con Canvas y animada: las frutas flotan y la
 * canasta respira. No usa imagenes ni librerias externas.
 */
@Composable
fun CanastaAnimada(
    modifier: Modifier = Modifier,
    tamano: Dp = 168.dp
) {
    val colorCanasta = MaterialTheme.colorScheme.primary
    val colorBorde = MaterialTheme.colorScheme.onPrimaryContainer
    val colorFondo = MaterialTheme.colorScheme.primaryContainer
    val colorNaranja = MaterialTheme.colorScheme.secondary
    val colorRojo = MaterialTheme.colorScheme.error
    val colorVerde = MaterialTheme.colorScheme.tertiary

    val transicion = rememberInfiniteTransition(label = "canasta")
    val flotar by transicion.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flotar"
    )
    val respirar by transicion.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "respirar"
    )

    Canvas(modifier = modifier.size(tamano)) {
        val w = size.width
        val h = size.height

        // Fondo circular suave
        drawCircle(
            color = colorFondo,
            radius = w * 0.46f,
            center = Offset(w / 2f, h / 2f)
        )

        // Hojas decorativas girando
        rotate(degrees = flotar * 2f, pivot = Offset(w * 0.18f, h * 0.2f)) {
            val hoja = Path().apply {
                moveTo(w * 0.18f, h * 0.2f)
                quadraticTo(w * 0.28f, h * 0.08f, w * 0.34f, h * 0.18f)
                quadraticTo(w * 0.24f, h * 0.24f, w * 0.18f, h * 0.2f)
                close()
            }
            drawPath(hoja, color = colorVerde.copy(alpha = 0.85f))
        }
        rotate(degrees = -flotar * 1.6f, pivot = Offset(w * 0.84f, h * 0.26f)) {
            val hoja = Path().apply {
                moveTo(w * 0.84f, h * 0.26f)
                quadraticTo(w * 0.92f, h * 0.14f, w * 0.98f, h * 0.24f)
                quadraticTo(w * 0.9f, h * 0.32f, w * 0.84f, h * 0.26f)
                close()
            }
            drawPath(hoja, color = colorVerde.copy(alpha = 0.6f))
        }

        // Frutas flotando sobre la canasta
        val desplazamiento = flotar / 100f * h
        drawCircle(
            color = colorNaranja,
            radius = w * 0.085f,
            center = Offset(w * 0.38f, h * 0.42f + desplazamiento)
        )
        drawCircle(
            color = colorRojo,
            radius = w * 0.07f,
            center = Offset(w * 0.56f, h * 0.36f - desplazamiento)
        )
        drawCircle(
            color = colorVerde,
            radius = w * 0.065f,
            center = Offset(w * 0.67f, h * 0.44f + desplazamiento * 0.6f)
        )
        // Tallos
        drawLine(
            color = colorBorde.copy(alpha = 0.5f),
            start = Offset(w * 0.38f, h * 0.33f + desplazamiento),
            end = Offset(w * 0.40f, h * 0.28f + desplazamiento),
            strokeWidth = w * 0.012f
        )

        // Cuerpo de la canasta
        val cuerpoAncho = w * 0.62f * respirar
        val cuerpoAlto = h * 0.26f
        val cuerpoX = (w - cuerpoAncho) / 2f
        val cuerpoY = h * 0.56f
        drawRoundRect(
            color = colorCanasta,
            topLeft = Offset(cuerpoX, cuerpoY),
            size = Size(cuerpoAncho, cuerpoAlto),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Asa
        drawArc(
            color = colorBorde.copy(alpha = 0.75f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.30f, h * 0.38f),
            size = Size(w * 0.40f, h * 0.36f),
            style = Stroke(width = w * 0.022f)
        )

        // Borde superior
        drawRoundRect(
            color = colorBorde.copy(alpha = 0.35f),
            topLeft = Offset(cuerpoX - w * 0.015f, cuerpoY - h * 0.02f),
            size = Size(cuerpoAncho + w * 0.03f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // Lineas de la canasta
        for (i in 1..3) {
            val x = cuerpoX + cuerpoAncho * i / 4f
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(x, cuerpoY + h * 0.03f),
                end = Offset(x, cuerpoY + cuerpoAlto - h * 0.03f),
                strokeWidth = w * 0.008f
            )
        }
    }
}

/**
 * Ilustracion animada para listas vacias: una caja abierta con dos hojas
 * que se mueven suavemente.
 */
@Composable
fun IlustracionVacia(
    modifier: Modifier = Modifier,
    tamano: Dp = 140.dp
) {
    val colorCaja = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    val colorTapa = MaterialTheme.colorScheme.secondary
    val colorHoja = MaterialTheme.colorScheme.tertiary

    val transicion = rememberInfiniteTransition(label = "vacia")
    val mecer by transicion.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mecer"
    )

    Canvas(modifier = modifier.size(tamano)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = colorCaja.copy(alpha = 0.12f),
            radius = w * 0.44f,
            center = Offset(w / 2f, h / 2f)
        )

        // Caja
        drawRoundRect(
            color = colorCaja,
            topLeft = Offset(w * 0.24f, h * 0.52f),
            size = Size(w * 0.52f, h * 0.30f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        // Tapas abiertas
        drawRoundRect(
            color = colorTapa.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.18f, h * 0.44f),
            size = Size(w * 0.24f, h * 0.07f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = colorTapa.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.58f, h * 0.44f),
            size = Size(w * 0.24f, h * 0.07f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // Hojas que se mecen
        rotate(degrees = mecer, pivot = Offset(w * 0.5f, h * 0.42f)) {
            val hojaIzquierda = Path().apply {
                moveTo(w * 0.46f, h * 0.44f)
                quadraticTo(w * 0.40f, h * 0.24f, w * 0.50f, h * 0.18f)
                quadraticTo(w * 0.50f, h * 0.34f, w * 0.46f, h * 0.44f)
                close()
            }
            drawPath(hojaIzquierda, color = colorHoja)
            val hojaDerecha = Path().apply {
                moveTo(w * 0.54f, h * 0.44f)
                quadraticTo(w * 0.62f, h * 0.26f, w * 0.54f, h * 0.16f)
                quadraticTo(w * 0.50f, h * 0.32f, w * 0.54f, h * 0.44f)
                close()
            }
            drawPath(hojaDerecha, color = colorHoja.copy(alpha = 0.7f))
        }
    }
}
