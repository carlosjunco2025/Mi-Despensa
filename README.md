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

Publicar estas reglas para que cada usuario solo pueda leer su propio documento:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /usuarios/{uid} {
      allow read: if request.auth != null && request.auth.uid == uid;
      allow write: if false;
    }
  }
}
```

El rol se lee **siempre** en `FirebaseAuthRepository` desde Firestore. Nunca se escribe
en la UI ni se hardcodea en el código.

### 2.5 Proyecto usado en esta entrega

- **Project ID:** `mi-despensa-1f57c`
- **Base de datos:** Firestore `(default)`
- **Colección:** `usuarios` (un documento por usuario, con ID = UID)
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
  - `AppNav` decide la pantalla inicial con la sesión y muestra el acceso a Admin solo
    al rol `ADMIN`.

- **`domain/model/` — modelos de dominio**
  - `Item`, `Sesion` y `Rol` son independientes de la red y de la base de datos.

- **`data/repository/` — contratos e implementación**
  - `ItemRepository` y `AuthRepository` son interfaces: los ViewModel dependen de la
    interfaz, nunca de la implementación concreta.
  - `ItemRepositoryImpl` usa la API para sincronizar y Room como **fuente única de
    verdad**; la UI siempre observa Room.
  - `FirebaseAuthRepository` autentica con Firebase Auth y lee el rol desde
    `usuarios/{uid}` en Firestore.

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

---

## 5. Pantallas y capturas

> Las capturas se agregan en esta sección. Reemplazar cada texto de apoyo por la imagen
> correspondiente (por ejemplo en `docs/capturas/`).

### Captura: Login

Pantalla de correo y contraseña, con validación de campos vacíos y mensajes de error.

<!-- Agregar aquí la captura de la pantalla de login -->

### Captura: Lista / catálogo

Catálogo de groceries con imagen, nombre, marca/categoría y precio; aviso cuando se
muestran datos guardados sin conexión.

<!-- Agregar aquí la captura de la pantalla de lista -->

### Captura: Detalle

Detalle del producto con el segundo endpoint, estados de carga/error con reintento y
botón para agregar o quitar de la lista.

<!-- Agregar aquí la captura de la pantalla de detalle -->

### Captura: Favoritos / Mi lista de compras

Productos marcados como favoritos, leídos desde Room, con opción de desmarcar y estado
vacío “Aún no tienes productos en tu lista”.

<!-- Agregar aquí la captura de la pantalla de favoritos -->

### Captura: Panel Admin

Visible solo para el rol `ADMIN`: total guardado, total de favoritos, Sincronizar y
Limpiar caché (con confirmación).

<!-- Agregar aquí la captura del panel Admin -->

### Captura: Notificación

Notificación local “Se guardaron N elementos” al terminar la sincronización; al tocarla
se abre la app.

<!-- Agregar aquí la captura de la notificación -->

---

## 6. Pruebas de aceptación

| Código | Caso | Resultado esperado |
| --- | --- | --- |
| PA-01 | Usuario normal entra | Ve el catálogo y **no** ve el panel Admin |
| PA-02 | Admin entra | Ve el catálogo y el acceso al panel Admin |
| PA-03 | Contraseña incorrecta | Mensaje claro y no entra |
| PA-04 | Correo o clave vacíos | Validación local, sin llamar a Firebase |
| PA-06 | Tocar un producto | Se abre el detalle |
| PA-07 | Marcar favorito y reabrir | El favorito sigue en “Mi lista” |
| PA-11 | Limpiar caché | Lista vacía y totales en 0; al sincronizar vuelven |
| PA-12 | Cerrar sesión y reabrir | Vuelve al login |
| PA-13 | Girar la pantalla | No se pierde el estado ni se vuelve a descargar |
| PA-14 | Negar permiso de notificaciones | La app funciona normal, sin avisos |

---

## 7. Notas de seguridad

- `google-services.json`, claves y contraseñas **no** se versionan (ver `.gitignore`).
- Las reglas de Firestore solo permiten leer el documento del propio usuario.
- El rol se determina en el repositorio a partir de Firestore, nunca en la UI.
