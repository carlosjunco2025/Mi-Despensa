# Despensa

Aplicación Android del Laboratorio 11-12 de **Programación en Móviles**.

**Tema:** catálogo de productos de despensa (groceries) con lista de compras.
**Variante:** consumo de la API pública **DummyJSON**, categoría `groceries`.

| Dato | Valor |
| --- | --- |
| Paquete | `com.tecsup.despensa` |
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 (sin XML) |
| Arquitectura | MVVM + repositorios |
| Persistencia local | Room (fuente única de verdad) |
| Red | Retrofit + kotlinx.serialization |
| Autenticación | Firebase Authentication |
| Roles | Firebase Firestore (`usuarios/{uid}`) |
| SDK | minSdk 24, targetSdk 37, JDK 17 |

La app permite iniciar sesión con dos roles (`admin` y `usuario`), ver el catálogo de
productos, abrir el detalle de cada uno, guardar favoritos como “Mi lista de compras”,
mantener la sesión al reabrir la app y, solo para el rol `admin`, sincronizar y limpiar
la caché desde un panel de administración. Al terminar una sincronización se muestra una
notificación local.

> **Fase 2 (rama `mejora-ia`):** se sumó registro de cuentas nuevas, acceso con Google,
> buscador con filtros y orden, sección “Mi despensa” con productos propios guardados en
> Firestore, CRUD completo desde el panel de administración (crear, editar y eliminar) e
> ilustraciones animadas dibujadas con Compose. Todo el texto de la interfaz está en
> español.

---

## 0. Novedades de la Fase 2

| Función | Dónde se ve | Cómo funciona |
| --- | --- | --- |
| Crear cuenta | Login → pestaña “Crear cuenta” | `FirebaseAuthRepository.registrar()` crea el usuario, guarda `nombre` y `rol: usuario` en `usuarios/{uid}` |
| Acceso con Google | Login (si está configurado) | Credential Manager obtiene el `idToken` y Firebase lo canjea con `signInWithCredential` |
| Buscador | Catálogo, barra superior | Filtra por nombre, marca y categoría sin distinguir tildes ni mayúsculas |
| Filtros y orden | Chips del catálogo | Categoría, `A-Z`, menor precio y mayor precio |
| Mi despensa | Catálogo, sección superior | Productos propios creados por el admin, leídos en tiempo real desde Firestore |
| CRUD de productos | Panel Admin | Crear, editar y eliminar con confirmación; estadísticas de inventario |
| Ilustraciones animadas | Login, carga y listas vacías | Dibujadas con `Canvas` y `rememberInfiniteTransition`, sin imágenes externas |
| Todo en español | Toda la app | Textos, validaciones y mensajes de error |

---

## 1. Cómo compilar

### Requisitos

- Android Studio reciente (o JDK 17 instalado y configurado en `JAVA_HOME`).
- Android SDK con `compileSdk 37`.
- Conexión a internet la primera vez (Gradle descarga dependencias).

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/carlosjunco2025/Mi-Despensa.git
   cd Mi-Despensa
   ```

2. (Opcional para compilar) Colocar el archivo **`google-services.json`** real dentro
   de la carpeta `app/` (ver sección 2). El archivo está en `.gitignore`: **nunca debe
   subirse al repositorio**. Si no está, el proyecto **igual compila**: la app arranca
   con un login local de prueba (cualquier correo; los que empiezan con `admin` entran
   como administrador).

3. Compilar el APK de depuración:

   ```bash
   ./gradlew clean assembleDebug
   ```

   En Windows:

   ```powershell
   .\gradlew.bat clean assembleDebug
   ```

4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.
   También se puede ejecutar desde Android Studio con **Run ▶**.

> Si aparece `Default FirebaseApp is not initialized`, revisar que
> `google-services.json` esté en `app/` y hacer **Sync Project with Gradle Files** +
> **Clean** + **Rebuild**. Sin ese archivo la app funciona en modo local, sin Firebase.

---

## 2. Configuración de Firebase

Estos pasos se hacen una sola vez en la consola de Firebase
(<https://console.firebase.google.com/>). **No se guardan contraseñas ni claves en el repositorio.**

### 2.1 Registrar la app

1. Crear un proyecto en Firebase (se puede desactivar Analytics).
2. Agregar una app Android con el paquete exacto **`com.tecsup.despensa`**.
3. Descargar `google-services.json` y colocarlo en la carpeta `app/` del proyecto.

### 2.2 Activar el inicio de sesión

1. Ir a **Authentication → Método de acceso**.
2. Activar **Correo electrónico/contraseña**.
3. En **Authentication → Usuarios**, crear dos usuarios de prueba, por ejemplo:
   - `admin@despensa.com`
   - `usuario@despensa.com`

   Las contraseñas se definen en la consola y **no se documentan ni se suben al repo**.

### 2.2.1 Activar el acceso con Google (opcional)

El botón “Continuar con Google” aparece solo cuando se pega el **ID de cliente web**.
Pasos:

1. En **Authentication → Método de acceso**, habilitar el proveedor **Google**.
2. Registrar la huella **SHA-1** del equipo donde se compila:

   ```powershell
   keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
     -alias androiddebugkey -storepass android -keypass android
   ```

   Copiar la línea `SHA1:` y agregarla en **Configuración del proyecto → Tus apps →
   Agregar huella digital** (app Android `com.tecsup.despensa`). Sin este paso Google
   devuelve el error `DEVELOPER_ERROR`.
3. En la misma pantalla de Google, copiar el **ID de cliente web** (termina en
   `.apps.googleusercontent.com`).
4. Pegarlo en `app/src/main/res/values/strings.xml`:

   ```xml
   <string name="google_web_client_id" translatable="false">TU_ID_DE_CLIENTE_WEB</string>
   ```

5. Volver a descargar `google-services.json` (Firebase lo actualiza al agregar la
   huella) y hacer **Sync + Clean + Run**.

> Si el ID queda vacío, la app compila y el botón de Google simplemente no se muestra;
> el correo/contraseña sigue funcionando normal.

### 2.3 Crear los documentos de rol en Firestore

1. Ir a **Firestore Database** y crear la base de datos.
2. Crear la colección **`usuarios`**.
3. Por cada usuario, crear un documento cuyo **ID sea el UID** que aparece en
   **Authentication → Usuarios** (no el correo). El documento debe tener:

   | Campo | Tipo | Valor |
   | --- | --- | --- |
   | `rol` | texto | `admin` o `usuario` |
   | `nombre` | texto | Nombre visible del usuario |

   Ejemplo de ruta: `usuarios/AbC123...XyZ`

   > Si el rol siempre sale como `usuario`, casi seguro el ID del documento no es el UID.

### 2.4 Reglas de Firestore

Publicar estas reglas: cada usuario solo puede leer su propio documento, la colección
`productos` la puede leer cualquier usuario autenticado y **solo un administrador** puede
crear, editar o eliminar productos.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function esAdmin() {
      return request.auth != null &&
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid))
          .data.rol.lower() == 'admin';
    }

    match /usuarios/{uid} {
      // Cada usuario lee y mantiene su propio perfil.
      allow read: if request.auth != null && request.auth.uid == uid;
      // Al registrarse solo puede crearse como "usuario"...
      allow create: if request.auth != null && request.auth.uid == uid
                    && request.resource.data.rol == 'usuario';
      // ...y no puede cambiarse el rol a sí mismo.
      allow update: if request.auth != null && request.auth.uid == uid
                    && request.resource.data.rol == resource.data.rol;
      allow delete: if false;
    }

    match /productos/{producto} {
      // Los productos propios se ven en el catálogo de todos los usuarios.
      allow read: if request.auth != null;
      // Solo el administrador gestiona el CRUD.
      allow create, update, delete: if esAdmin();
    }
  }
}
```

> **Importante para la Fase 2:** si no se agrega el bloque de `productos`, el panel
> Admin mostrará “Firestore rechazó la operación”. El rol se escribe a mano en Firestore
> (`admin`); `.lower()` permite aceptar `admin`, `Admin` o `ADMIN`.

El rol se lee **siempre** en `FirebaseAuthRepository` desde Firestore. Nunca se escribe
en la UI ni se hardcodea en el código.

### 2.5 Proyecto usado en esta entrega

- **Project ID:** `mi-despensa-1f57c`
- **Base de datos:** Firestore `(default)`
- **Colecciones:** `usuarios` (un documento por usuario, con ID = UID) y `productos`
  (productos propios creados por el admin)
- **Paquete Android:** `com.tecsup.despensa`

El `google-services.json` de ese proyecto se descarga de la consola y se coloca en
`app/`. Está en `.gitignore`: **no se versiona**. Si el archivo no está, la app compila
igual y arranca con el login local de respaldo (`AuthRepositoryLocal`).

---

## 3. Arquitectura

```
UI (Compose)  →  ViewModel  →  Repository (interface)  →  Data
                                      │
                     ┌────────────────┼─────────────────┐
                  Retrofit          Room            Firebase
                (DummyJSON)     (cache local)   (Auth + Firestore)
```

### Capas y por qué

- **`ui/` — presentación (Compose + ViewModel)**
  - Las pantallas solo observan estado con `collectAsStateWithLifecycle()` y llaman
    funciones del ViewModel. No usan Retrofit, Room ni Firebase directamente.
  - Cada ViewModel expone `StateFlow<UiState<T>>` (`Cargando`, `Exito`, `Error`) con un
    `MutableStateFlow` privado y `asStateFlow()`.
  - Los ViewModel no reciben `Context`. La notificación se pide mediante la interfaz
    `Notificador`.
  - `AppNav` decide la pantalla inicial con la sesión, muestra el saludo con el avatar
    del usuario y el acceso a Admin solo al rol `ADMIN`.
  - `LoginScreen` tiene dos modos (ingresar / crear cuenta), validaciones locales y el
    botón de Google condicionado a que exista `google_web_client_id`.
  - `ListaScreen` combina buscador, chips de categoría, orden y la sección “Mi despensa”
    con los productos propios.
  - `AdminScreen` incluye estadísticas, formulario de alta/edición, eliminación con
    confirmación, sincronización y limpieza de caché.
  - `ui/components/Ilustraciones.kt` dibuja la canasta y la caja animadas con `Canvas` y
    `rememberInfiniteTransition`; no se usan imágenes externas.

- **`domain/model/` — modelos de dominio**
  - `Item`, `Sesion`, `Rol` y `Producto` son independientes de la red y de la base de
    datos. `Producto` representa lo que crea el administrador y vive en Firestore.

- **`data/repository/` — contratos e implementación**
  - `ItemRepository`, `AuthRepository` y `ProductosRepository` son interfaces: los
    ViewModel dependen de la interfaz, nunca de la implementación concreta.
  - `ItemRepositoryImpl` usa la API para sincronizar y Room como **fuente única de
    verdad**; la UI siempre observa Room.
  - `FirebaseAuthRepository` autentica con Firebase Auth (correo, registro y Google) y
    lee el rol desde `usuarios/{uid}` en Firestore.
  - `ProductosRepositoryFirestore` expone `observar()` como `callbackFlow` con
    `addSnapshotListener`, así el catálogo propio se actualiza en tiempo real.
  - `AuthRepositoryLocal` y `ProductosRepositoryLocal` son los respaldos sin Firebase:
    permiten compilar y navegar cuando no existe `google-services.json`.

- **`data/remote/` — Retrofit + DTO**
  - `ApiService` expone los dos endpoints de DummyJSON:
    `/products/category/groceries` y `/products/{id}`.
  - Los DTO se convierten a `ItemEntity`/`Item` con `Mappers.kt`.

- **`data/local/` — Room**
  - `ItemEntity`, `ItemDao` y `AppDatabase`. Guarda el catálogo y el campo
    `esFavorito`, por eso los favoritos sobreviven al cierre de la app y a la
    sincronización.

- **`notification/` — notificación local**
  - `Notificador` es la interfaz que consumen los ViewModel.
  - `NotificadorAndroid` crea el canal, verifica `POST_NOTIFICATIONS` en Android 13+,
    usa `NotificationCompat` con icono pequeño, `PendingIntent` a `MainActivity` y
    `setAutoCancel(true)`.

- **`AppContainer.kt` + `ui/ViewModelFactory.kt` — inyección de dependencias manual**
  - `AppContainer` construye una sola vez la base de datos, los repositorios y el
    notificador. `ViewModelFactory` crea los ViewModel con esas dependencias.

### Flujo de datos

1. La pantalla pide datos al ViewModel.
2. El ViewModel llama a `ItemRepository.refrescar()`.
3. El repositorio descarga de DummyJSON, conserva los favoritos y guarda en Room.
4. Room emite el nuevo listado y la UI se actualiza sola.
5. Si no hay internet pero hay datos guardados, se muestra el aviso
   “Sin conexión: mostrando datos guardados”.

---

## 4. Reparto de tareas

El laboratorio se desarrolló entre dos integrantes. Completar los nombres al entregar.

### Integrante 1 — Capa de datos y catálogo

- Proyecto base, estructura MVVM y contratos (`Item`, `Sesion`, `Rol`,
  `ItemRepository`, `Notificador`, `UiState`).
- Retrofit, `ApiService`, DTO y mappers.
- `AppContainer` y `ViewModelFactory`.
- `ListaViewModel` y `ListaScreen` con estados `Cargando`, `Exito`, `Error` y
  reintento.
- Room (`ItemEntity`, `ItemDao`, `AppDatabase`) como fuente única de verdad.
- Favoritos persistentes y modo sin conexión.

### Integrante 2 — Firebase, pantallas restantes y documentación

- Configuración de Firebase en Gradle (`google-services`, BoM, Auth, Firestore,
  `coroutines-play-services`) y permiso `POST_NOTIFICATIONS`.
- `FirebaseAuthRepository` con lectura del rol desde Firestore.
- `LoginViewModel` y `LoginScreen` con validación de campos vacíos y mensajes de error.
- `SesionViewModel`: sesión persistente con `sesionActual()` y cierre de sesión.
- `DetalleViewModel` y `DetalleScreen` con el segundo endpoint, reintento y favorito.
- `FavoritosViewModel` y `FavoritosScreen` (“Mi lista de compras”).
- `AppNav` con navegación por rol y bloqueo del panel Admin para `USUARIO`.
- `AdminViewModel` y `AdminScreen` con totales, sincronizar y limpiar caché.
- `NotificadorAndroid` y solicitud del permiso de notificaciones en runtime.
- Este `README.md`.

### Fase 2 — mejoras con IA (rama `mejora-ia`)

- Registro de cuentas con nombre, validación de correo y confirmación de contraseña.
- Acceso con Google mediante Credential Manager + Firebase (`GoogleSignIn.kt`).
- `ProductosRepository` con implementación Firestore y respaldo local.
- Buscador tolerante a tildes, filtros por categoría y tres órdenes en el catálogo.
- Sección “Mi despensa” con los productos propios y su detalle en un diálogo.
- Panel Admin con estadísticas, alta, edición, eliminación con confirmación y avisos.
- Tema nuevo (verde bosque, menta y naranja), tipografía y formas redondeadas.
- Ilustraciones animadas, transiciones entre pantallas y estados de carga/error.
- Reglas de Firestore para `productos` y documentación del acceso con Google.

---

## 5. Pantallas y capturas

> Las capturas se agregan en esta sección. Reemplazar cada texto de apoyo por la imagen
> correspondiente (por ejemplo en `docs/capturas/`).

### Captura: Login

Pantalla con la canasta animada, dos pestañas (Ingresar / Crear cuenta), validaciones en
español y botón de Google cuando está configurado.

<!-- Agregar aquí la captura de la pantalla de login -->

### Captura: Lista / catálogo

Catálogo con buscador, chips de categoría, orden, sección “Mi despensa” y tarjetas con
imagen, nombre, marca/categoría y precio.

<!-- Agregar aquí la captura de la pantalla de lista -->

### Captura: Detalle

Detalle del producto con imagen grande, precio, ficha de información y botón para
agregar o quitar de la lista.

<!-- Agregar aquí la captura de la pantalla de detalle -->

### Captura: Favoritos / Mi lista de compras

Productos marcados como favoritos con el total estimado de la compra y estado vacío
ilustrado.

<!-- Agregar aquí la captura de la pantalla de favoritos -->

### Captura: Panel Admin

Visible solo para el rol `ADMIN`: estadísticas, formulario para crear/editar, lista de
productos propios con editar/eliminar, sincronizar y limpiar caché.

<!-- Agregar aquí la captura del panel Admin -->

### Captura: Notificación

Notificación local “Despensa actualizada” al terminar la sincronización; al tocarla se
abre la app.

<!-- Agregar aquí la captura de la notificación -->

---

## 6. Pruebas de aceptación

| Código | Caso | Resultado esperado |
| --- | --- | --- |
| PA-01 | Usuario normal entra | Ve el catálogo y **no** ve el panel Admin |
| PA-02 | Admin entra | Ve el catálogo y el acceso al panel Admin |
| PA-03 | Contraseña incorrecta | Mensaje claro y no entra |
| PA-04 | Correo o clave vacíos | Validación local, sin llamar a Firebase |
| PA-05 | Crear cuenta nueva | Entra como `usuario` y se crea `usuarios/{uid}` con su nombre |
| PA-06 | Tocar un producto | Se abre el detalle |
| PA-07 | Marcar favorito y reabrir | El favorito sigue en “Mi lista” |
| PA-08 | Buscar “leche” | El catálogo se filtra al instante |
| PA-09 | Filtrar por categoría y ordenar | La lista respeta categoría y orden elegidos |
| PA-10 | Admin crea un producto | Aparece en “Mi despensa” y en Firestore |
| PA-11 | Limpiar caché | Lista vacía y totales en 0; al sincronizar vuelven |
| PA-12 | Cerrar sesión y reabrir | Vuelve al login |
| PA-13 | Girar la pantalla | No se pierde el estado ni se vuelve a descargar |
| PA-14 | Negar permiso de notificaciones | La app funciona normal, sin avisos |
| PA-15 | Admin edita un producto | El cambio se refleja al instante en “Mi despensa” |
| PA-16 | Admin elimina un producto | Pide confirmación y desaparece de la lista |
| PA-17 | Usuario sin rol admin | No puede escribir en `productos` (regla de Firestore) |
| PA-18 | Contraseñas distintas al registrarse | Mensaje “Las contraseñas no coinciden” |
| PA-19 | Acceso con Google sin SHA-1 | Muestra un mensaje claro y no cierra la app |
| PA-20 | Sin internet | Se muestran los datos guardados y un aviso |

---

## 7. Notas de seguridad

- `google-services.json`, claves y contraseñas **no** se versionan (ver `.gitignore`).
- Las reglas de Firestore solo permiten leer el documento del propio usuario y escribir
  `productos` al rol `admin`.
- El rol se determina en el repositorio a partir de Firestore, nunca en la UI.
- Un usuario no puede cambiarse el rol a sí mismo (regla `update` de `usuarios`).
- El `idToken` de Google se obtiene con Credential Manager y se canjea en Firebase; la
  app nunca guarda tokens ni contraseñas.

---

## 8. Entrega de la Fase 2

- Rama: **`mejora-ia`** (mismo repositorio, no se mezcló con `main`).
- En la otra máquina: `git fetch origin`, `git checkout mejora-ia`, copiar el
  `google-services.json` real a `app/`, **Sync Project with Gradle Files**, **Clean** y
  **Run**.
- Publicar las reglas de Firestore de la sección 2.4 antes de probar el panel Admin.
- Para Google: agregar la huella SHA-1 del equipo y pegar el ID de cliente web (2.2.1).
