package com.tecsup.despensa

import android.app.Application
import android.content.Context
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.data.auth.FirebaseAuthRepository
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.data.repository.ItemRepositoryImpl
import com.tecsup.despensa.notification.Notificador
import com.tecsup.despensa.notification.NotificadorAndroid

/**
 * Contenedor de dependencias de la aplicacion.
 * Construye una sola vez los repositorios y el notificador que consumen los ViewModels.
 */
class AppContainer(context: Context) {
    val notificador: Notificador = NotificadorAndroid(context)
    val authRepository: AuthRepository = FirebaseAuthRepository()
    val itemRepository: ItemRepository = ItemRepositoryImpl()
}

/**
 * Application que expone el contenedor al resto de la app.
 */
class DespensaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
