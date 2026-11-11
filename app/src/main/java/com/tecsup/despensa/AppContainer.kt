package com.tecsup.despensa

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.data.auth.AuthRepositoryLocal
import com.tecsup.despensa.data.auth.FirebaseAuthRepository
import com.tecsup.despensa.data.local.AppDatabase
import com.tecsup.despensa.data.local.ItemDao
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.data.repository.ItemRepositoryImpl
import com.tecsup.despensa.notification.Notificador
import com.tecsup.despensa.notification.NotificadorAndroid

/**
 * Contenedor de dependencias de la aplicacion.
 * Construye una sola vez los repositorios y el notificador que consumen los ViewModels.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.obtener(appContext)
    val itemDao: ItemDao = database.itemDao()
    val notificador: Notificador = NotificadorAndroid(appContext)
    val authRepository: AuthRepository =
        if (firebaseConfigurado()) FirebaseAuthRepository() else AuthRepositoryLocal()
    val itemRepository: ItemRepository = ItemRepositoryImpl(dao = itemDao)

    /**
     * Firebase queda activo solo si google-services.json genero la configuracion.
     * Sin ese archivo (no se versiona por seguridad) la app usa el login local
     * para poder compilar y navegar.
     */
    private fun firebaseConfigurado(): Boolean =
        runCatching { FirebaseApp.getApps(appContext).isNotEmpty() }.getOrDefault(false)
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
