package com.tecsup.despensa.ui

import java.util.Locale

/** Precio con dos decimales y simbolo de soles, sin depender del idioma del equipo. */
fun Double.comoPrecio(): String = String.format(Locale.US, "S/ %.2f", this)
