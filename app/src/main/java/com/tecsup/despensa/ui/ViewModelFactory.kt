package com.tecsup.despensa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.despensa.AppContainer
import com.tecsup.despensa.ui.admin.AdminViewModel
import com.tecsup.despensa.ui.detalle.DetalleViewModel
import com.tecsup.despensa.ui.favoritos.FavoritosViewModel
import com.tecsup.despensa.ui.lista.ListaViewModel
import com.tecsup.despensa.ui.login.LoginViewModel
import com.tecsup.despensa.ui.sesion.SesionViewModel

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

            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(container.authRepository) as T

            modelClass.isAssignableFrom(SesionViewModel::class.java) ->
                SesionViewModel(container.authRepository) as T

            modelClass.isAssignableFrom(DetalleViewModel::class.java) ->
                DetalleViewModel(container.itemRepository) as T

            modelClass.isAssignableFrom(FavoritosViewModel::class.java) ->
                FavoritosViewModel(container.itemRepository) as T

            modelClass.isAssignableFrom(AdminViewModel::class.java) ->
                AdminViewModel(container.itemRepository, container.notificador) as T

            else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        }
    }
}
