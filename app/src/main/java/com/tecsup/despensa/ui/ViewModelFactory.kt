package com.tecsup.despensa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.despensa.AppContainer
import com.tecsup.despensa.ui.lista.ListaViewModel

/**
 * Crea los ViewModels inyectando las dependencias del AppContainer.
 * Ningun ViewModel recibe un Context.
 */
class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ListaViewModel::class.java) ->
                ListaViewModel(container.itemRepository, container.notificador) as T

            else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        }
    }
}
