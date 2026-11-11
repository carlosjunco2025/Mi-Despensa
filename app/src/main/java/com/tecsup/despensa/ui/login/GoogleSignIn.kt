package com.tecsup.despensa.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

/**
 * Obtiene un idToken de Google con Credential Manager para pasarselo a
 * Firebase Auth. Requiere:
 *  1. Habilitar el proveedor Google en Firebase Authentication.
 *  2. Registrar la huella SHA-1 del equipo en el proyecto de Firebase.
 *  3. Poner el ID de cliente web en R.string.google_web_client_id.
 */
suspend fun obtenerIdTokenGoogle(
    context: Context,
    serverClientId: String
): Result<String> = try {
    val manager = CredentialManager.create(context)

    val nonce = UUID.randomUUID().toString()
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(nonce.toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }

    val opcion = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setNonce(hash)
        .build()

    val peticion = GetCredentialRequest.Builder()
        .addCredentialOption(opcion)
        .build()

    val respuesta = manager.getCredential(context, peticion)
    val credencial = respuesta.credential

    if (credencial is CustomCredential &&
        credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleId = GoogleIdTokenCredential.createFrom(credencial.data)
        Result.success(googleId.idToken)
    } else {
        Result.failure(IllegalStateException("Credencial de Google no reconocida"))
    }
} catch (e: Exception) {
    Result.failure(e)
}
