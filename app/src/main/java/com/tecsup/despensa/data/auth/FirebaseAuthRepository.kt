package com.tecsup.despensa.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.despensa.domain.model.Rol
import com.tecsup.despensa.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Autenticacion con Firebase Auth y lectura del rol desde Firestore.
 * El rol nunca se decide en la UI: siempre se lee de usuarios/{uid}.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> = try {
        val resultado = auth.signInWithEmailAndPassword(correo.trim(), clave).await()
        val usuario = resultado.user
            ?: return Result.failure(IllegalStateException("No se pudo obtener el usuario autenticado"))
        Result.success(construirSesion(usuario))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        auth.signOut()
        Result.failure(e)
    }

    override suspend fun registrarUsuario(nombre: String, correo: String, clave: String): Result<Sesion> = try {
        val resultado = auth.createUserWithEmailAndPassword(correo.trim(), clave).await()
        val usuario = resultado.user
            ?: return Result.failure(IllegalStateException("No se pudo obtener el usuario registrado"))

        val datosUsuario = mapOf(
            "nombre" to nombre.trim(),
            "correo" to correo.trim(),
            "rol" to "usuario"
        )

        firestore.collection("usuarios")
            .document(usuario.uid)
            .set(datosUsuario)
            .await()

        Result.success(construirSesion(usuario))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        auth.signOut()
        Result.failure(e)
    }

    override suspend fun sesionActual(): Sesion? = try {
        auth.currentUser?.let { usuario -> construirSesion(usuario) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    private suspend fun construirSesion(usuario: FirebaseUser): Sesion {
        val documento = firestore.collection("usuarios")
            .document(usuario.uid)
            .get()
            .await()

        val nombre = documento.getString("nombre")
            ?: usuario.displayName
            ?: usuario.email?.substringBefore("@")
            ?: "Usuario"

        // Se acepta "admin", "Admin", "ADMIN" o "administrador": el valor se lee de Firestore.
        val rolStr = documento.getString("rol")?.trim()?.lowercase() ?: ""
        val rol = if (rolStr == "admin" || rolStr == "administrador") {
            Rol.ADMIN
        } else {
            Rol.USUARIO
        }
        return Sesion(
            uid = usuario.uid,
            correo = usuario.email ?: "",
            nombre = nombre,
            rol = rol
        )
    }
}
