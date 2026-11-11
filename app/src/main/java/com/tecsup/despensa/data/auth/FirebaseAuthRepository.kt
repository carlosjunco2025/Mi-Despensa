package com.tecsup.despensa.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    override suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): Result<Sesion> = try {
        val resultado = auth.createUserWithEmailAndPassword(correo.trim(), clave).await()
        val usuario = resultado.user
            ?: return Result.failure(IllegalStateException("No se pudo crear la cuenta"))

        val perfil = UserProfileChangeRequest.Builder()
            .setDisplayName(nombre.trim())
            .build()

        // El perfil y el rol son datos complementarios: si Firestore los rechaza
        // (por ejemplo por reglas estrictas), la cuenta de Authentication igual
        // queda creada y el rol se resuelve como "usuario" al iniciar sesión.
        try {
            usuario.updateProfile(perfil).await()
            firestore.collection("usuarios")
                .document(usuario.uid)
                .set(
                    mapOf("rol" to "usuario", "nombre" to nombre.trim()),
                    SetOptions.merge()
                )
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Se ignora: el perfil se puede completar despues.
        }

        Result.success(
            Sesion(
                uid = usuario.uid,
                correo = usuario.email ?: correo.trim(),
                rol = Rol.USUARIO,
                nombre = nombre.trim()
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        auth.signOut()
        Result.failure(e)
    }

    override suspend fun iniciarConGoogle(idToken: String): Result<Sesion> = try {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val resultado = auth.signInWithCredential(credencial).await()
        val usuario = resultado.user
            ?: return Result.failure(IllegalStateException("No se pudo iniciar con Google"))
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
        val referencia = firestore.collection("usuarios").document(usuario.uid)
        val documento = referencia.get().await()

        if (!documento.exists()) {
            // Cuenta creada con Google: se registra el perfil base como usuario.
            // Si las reglas no lo permiten, el acceso continua igual.
            try {
                referencia.set(
                    mapOf(
                        "rol" to "usuario",
                        "nombre" to (usuario.displayName ?: usuario.email ?: "Usuario")
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Perfil opcional: no se bloquea el inicio de sesion.
            }
        }

        // Se acepta "admin", "Admin" o "ADMIN": el valor se escribe a mano en Firestore.
        val rol = if (documento.getString("rol")?.trim()?.lowercase() == "admin") {
            Rol.ADMIN
        } else {
            Rol.USUARIO
        }
        val nombre = documento.getString("nombre")
            ?: usuario.displayName
            ?: usuario.email?.substringBefore("@")
            ?: ""

        return Sesion(
            uid = usuario.uid,
            correo = usuario.email ?: "",
            rol = rol,
            nombre = nombre
        )
    }
}
