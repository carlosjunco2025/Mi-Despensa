package com.tecsup.despensa.notification

import android.content.Context

/**
 * Stub no-op. La notificacion real la implementara el companero.
 */
class NotificadorAndroid(
    @Suppress("unused") private val context: Context
) : Notificador {
    override fun mostrar(titulo: String, texto: String) {
        // Intencionalmente vacio en esta parte del laboratorio.
    }
}
