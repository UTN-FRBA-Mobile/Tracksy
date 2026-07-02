<div align="center">

# 🛒 Tracksy

### Tu lista de compras inteligente

**Compará precios, escaneá productos y ahorrá en cada supermercado.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://play.google.com/store)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-29%20(Android%2010)-blue)](#requisitos)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)](#requisitos)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](#stack-tecnológico)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](#stack-tecnológico)
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey)](#licencia)

</div>

---

## 📱 Sobre la aplicación

**Tracksy** es una app de Android nativa que ayuda a planificar las compras del supermercado de punta a punta: arma tu lista, escaneá los códigos de barras de los productos, compará precios entre supermercados cercanos y llevá un historial completo de cuánto gastás y en qué.

Pensada para el uso diario, Tracksy combina geolocalización, escaneo de códigos EAN‑13 con reconocimiento óptico y un motor de recomendaciones propio para que nunca te olvides de comprar lo que necesitás — y para que siempre elijas el supermercado más conveniente.

> 💡 **¿Por qué Tracksy?** Porque comparar precios a mano entre changuitos de papel o notas sueltas es tedioso. Tracksy centraliza tus listas, tu historial de compras y los precios de cada producto por supermercado en un solo lugar, con sugerencias automáticas basadas en tus hábitos.

---

## ✨ Funcionalidades principales

### 🔐 Autenticación segura
- Registro e inicio de sesión con **email y contraseña** (Firebase Authentication).
- Inicio de sesión con **Google (One Tap / Google Sign-In)**.
- Verificación de email obligatoria antes de acceder a la app, con reenvío de verificación.
- Recuperación de contraseña por email.
- Cambio de contraseña desde el perfil, con validación de la contraseña actual.

### 🏠 Inicio (Home)
- Resumen de tus listas activas.
- **Sugerencias inteligentes** generadas por el motor de recomendaciones local (ver más abajo), que podés aceptar (agregándolas directo a una lista) o descartar.
- Pull-to-refresh para sincronizar datos al instante.

### 📋 Listas de compras
- Creación y edición de listas de compras ilimitadas, asociadas o no a un supermercado.
- Agregado de productos por **búsqueda en catálogo** o por **escaneo de código de barras**.
- Marcado de ítems como comprados/pendientes dentro del detalle de la lista.
- Asignación de supermercado a una lista y comparación de precios entre locales.
- **Finalizar compra**: cierra la lista, calcula el total gastado según precios reales del supermercado elegido, guarda el resultado en el historial y permite recrear una nueva lista con los productos pendientes.

### 📷 Escáner de código de barras
- Escaneo en vivo con **CameraX + ML Kit Barcode Scanning** (estándar EAN‑13).
- Flujo contextual: si escaneás desde dentro de una lista, el producto se agrega directo a esa lista; si escaneás desde el catálogo, te lleva al detalle del producto.
- Manejo de "producto no encontrado" con opción de reintentar el escaneo.

### 🛍️ Catálogo de productos
- Listado y búsqueda de productos por nombre o marca.
- Marcado de **favoritos** para acceso rápido y para alimentar las recomendaciones.
- Detalle de producto con la posibilidad de agregarlo a una o varias listas existentes, o crear una lista nueva al vuelo.

### 🏪 Comparador de supermercados
- Compará el precio de los productos de tu lista entre los distintos supermercados relevados.
- Elegí el supermercado más conveniente y asignalo directamente a la lista.

### 🕓 Historial de compras
- Registro histórico de todas las compras finalizadas, con fecha, supermercado y total gastado.
- Detalle por compra: productos comprados, cantidades y precios unitarios.
- **"Reutilizar lista"**: recreá una lista nueva a partir de una compra anterior en un solo toque.

### 📍 Alertas de proximidad
- Servicio de ubicación en primer plano (`FusedLocationProviderClient`) que detecta cuándo estás cerca de un supermercado donde tenés una lista pendiente.
- Notificaciones push configurables (activar/desactivar) con distancia de aviso ajustable por el usuario.

### 🔔 Motor de recomendaciones
- Sistema de criterios extensible que analiza tu comportamiento de compra:
  - **Favoritos no planificados**: productos marcados como favoritos que no están en ninguna lista activa.
  - **Productos frecuentes**: productos que comprás recurrentemente y podrías estar por necesitar de nuevo.
- Ejecución periódica en background con **WorkManager**, con notificación al usuario cuando hay sugerencias nuevas.

### 🧩 Widget de pantalla de inicio
- Widget nativo de Android ("Mi lista activa") que muestra tu lista de compras vigente sin abrir la app.
- Permite tildar productos como comprados y actualizar la lista directamente desde el widget.
- Se sincroniza automáticamente con los cambios hechos dentro de la app.

### 👤 Perfil de usuario
- Edición de datos personales (nombre, foto de perfil).
- Preferencias de notificaciones y alertas de proximidad.
- Selector de **modo oscuro / claro**.
- Sección de ayuda y soporte.
- Cierre de sesión seguro con limpieza de estado local.

---

## 🖼️ Recorrido por la app

> Capturas de pantalla ilustrativas de los flujos principales — reemplazar por los assets finales antes de publicar en la ficha de Play Store.

| Pantalla | Descripción |
|---|---|
| **Login / Registro** | Acceso con email o Google, con validaciones en tiempo real y verificación de email. |
| **Home** | Vista general con listas activas y sugerencias personalizadas. |
| **Mis Listas** | Todas las listas de compra, con creación rápida y borrado. |
| **Detalle de Lista** | Ítems de la lista, estado de compra, acceso a comparar precios y finalizar compra. |
| **Escáner** | Cámara en vivo para leer códigos de barra y sumar productos al instante. |
| **Catálogo de Productos** | Búsqueda y favoritos de productos disponibles. |
| **Comparar Supermercados** | Tabla comparativa de precios por supermercado para la lista activa. |
| **Finalizar Compra** | Resumen de gasto total, productos comprados y pendientes. |
| **Historial** | Compras pasadas con detalle y opción de reutilizar la lista. |
| **Perfil** | Datos personales, preferencias, modo oscuro y soporte. |
| **Widget de inicio** | Lista activa accesible desde el escritorio del teléfono. |

*(Las imágenes reales de cada pantalla deben incorporarse aquí como `docs/screenshots/<pantalla>.png` una vez generadas desde builds de referencia, respetando el diseño real de la app.)*

---

## 🏗️ Arquitectura

Tracksy sigue una arquitectura **MVVM** sobre **Jetpack Compose**, con separación clara entre capas:

```
com.example.tracksy
├── data/
│   ├── api/            → ApiService (Retrofit) y RetrofitClient
│   ├── local/           → TokenManager, UserPreferencesRepository, RecommendationStorage (SharedPreferences)
│   ├── models/          → DTOs y modelos de dominio (Models.kt)
│   └── repository/      → TracksyRepository / TracksyRepositoryInterface
├── viewmodel/            → AuthViewModel, PerfilViewModel, ProductoViewModel,
│                           ListaViewModel, CompraViewModel, RecommendationViewModel
├── ui/
│   ├── auth/             → Login, registro, recuperación de contraseña
│   ├── home/             → Home y escáner de código de barras
│   ├── lists/             → Mis listas, detalle y edición de listas
│   ├── products/          → Catálogo y detalle de producto
│   ├── supermarket/       → Comparador de supermercados
│   ├── checkout/          → Finalización de compra
│   ├── history/           → Historial de compras
│   ├── profile/            → Perfil, edición, cambio de contraseña, ayuda
│   ├── components/        → Componentes reutilizables (botones, campos)
│   └── theme/              → Theming Material 3 (claro/oscuro)
├── recommendations/       → Motor de recomendaciones (criterios + engine + worker)
├── location/                → Servicio de geolocalización y alertas de proximidad
├── widget/                   → Widget de pantalla de inicio (Glance/RemoteViews)
└── MainActivity.kt            → Composición de navegación y orquestación de estado global
```

- **UI declarativa** 100% Jetpack Compose (sin XML de layouts).
- **Estado unidireccional**: los `ViewModel` exponen `StateFlow` que la UI colecciona con `collectAsState`.
- **Backend remoto** vía API REST en Django, consumida con Retrofit + OkHttp + Gson.
- **Autenticación** delegada a Firebase Auth (y sincronizada contra el backend propio).
- **Persistencia local** liviana para tokens, preferencias del usuario y caché de recomendaciones/widget.
- **Tareas en background** con WorkManager (recomendaciones periódicas) y un `Foreground Service` dedicado para geolocalización.

---

## 🧰 Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI Toolkit | Jetpack Compose (BOM 2026.02.01) + Material 3 |
| Arquitectura | MVVM + Repository Pattern |
| Navegación / Estado | Compose state hoisting (`ViewModel` + `StateFlow`) |
| Networking | Retrofit 2.11.0, OkHttp 4.12.0, Gson 2.11.0 |
| Autenticación | Firebase Authentication (BOM 34.15.0), Google Sign-In (Play Services Auth 21.3.0) |
| Cámara / Escaneo | CameraX 1.4.1 + ML Kit Barcode Scanning 17.3.0 |
| Geolocalización | Google Play Services Location (FusedLocationProviderClient) |
| Tareas en background | WorkManager 2.9.1 |
| Concurrencia | Kotlin Coroutines 1.9.0 |
| Testing unitario | JUnit 4, MockK, Turbine, kotlinx-coroutines-test |
| Testing instrumentado | Espresso, Compose UI Test |
| Build system | Gradle (Kotlin DSL) + Android Gradle Plugin 9.2.1 |

---

## 🔌 Backend

Tracksy consume una **API REST propia (Django)** para el catálogo de productos, listas, compras, supermercados y sugerencias, mientras delega el login/registro/verificación de identidad en **Firebase Authentication**. Al autenticarse, la app sincroniza el usuario de Firebase con el backend Django (`FirebaseSyncResponse`).

Endpoints consumidos (prefijo `/api/v1/`), entre otros:
- `usuarios/` — perfil, favoritos, cambio de contraseña
- `productos/` — catálogo, búsqueda, detalle
- `supermercados/` — locales relevados con geolocalización
- `listados/` — precios de productos por supermercado
- `listas/` — listas de compra e ítems
- `compras/` — historial de compras finalizadas
- `sugerencias/` — feedback sobre recomendaciones

---

## 🚀 Requisitos

- **Android Studio** (última versión estable recomendada, con AGP 9.2.1 y JDK 17+).
- **SDK mínimo:** Android 10 (API 29).
- **SDK objetivo:** API 36.
- Backend Django corriendo (local o remoto) para las funcionalidades de datos.
- Proyecto de **Firebase** configurado (`google-services.json`) con Authentication (Email/Password + Google) habilitado.

## ⚙️ Configuración del entorno

1. Cloná el repositorio y abrilo en Android Studio.
2. Colocá tu archivo `google-services.json` en `app/`.
3. Creá (o editá) el archivo `local.properties` en la raíz del proyecto y agregá:

   ```properties
   API_BASE_URL=http://10.0.2.2:8000/
   GOOGLE_WEB_CLIENT_ID=<tu_client_id_de_google_cloud>
   ```

   > `10.0.2.2` apunta al `localhost` de tu máquina cuando corrés el backend Django localmente y probás en el emulador de Android Studio. Reemplazá `API_BASE_URL` por la URL del backend desplegado para builds de staging/producción.

4. Sincronizá el proyecto con Gradle (`Sync Project with Gradle Files`).
5. Ejecutá la app sobre un emulador (API 29+) o dispositivo físico con Google Play Services.

## 🔑 Permisos utilizados

| Permiso | Uso |
|---|---|
| `CAMERA` | Escaneo de códigos de barra de productos. |
| `INTERNET` | Comunicación con el backend y Firebase. |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Detección de cercanía a supermercados. |
| `POST_NOTIFICATIONS` | Notificaciones de recomendaciones y alertas de proximidad (Android 13+). |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_LOCATION` | Servicio de ubicación en primer plano para alertas de proximidad. |

Todos los permisos sensibles (cámara, ubicación, notificaciones) se solicitan en tiempo de ejecución y son opcionales para el uso básico de la app: sin ellos, simplemente no se activan el escáner o las alertas de proximidad.

## 🧪 Testing

El proyecto incluye:
- **Tests unitarios** (`app/src/test`) para los `ViewModel` de autenticación, compras, sugerencias y listas, usando MockK y Turbine sobre `StateFlow`.
- **Tests instrumentados** (`app/src/androidTest`) con Compose UI Test para pantallas clave: login, mis listas, detalle de lista y edición de lista con escáner.

Para correrlos:

```bash
# Tests unitarios
./gradlew testDebugUnitTest

# Tests instrumentados (requiere emulador o dispositivo conectado)
./gradlew connectedDebugAndroidTest
```

## 📦 Generar el APK / Bundle

```bash
# APK de debug
./gradlew assembleDebug

# Android App Bundle firmado para publicar en Play Store
./gradlew bundleRelease
```

---

## 🗺️ Roadmap sugerido

- [ ] Sincronización offline-first para listas y compras.
- [ ] Comparación de precios históricos (evolución de precio por producto).
- [ ] Compartir listas de compra entre usuarios.
- [ ] Soporte multi-idioma (actualmente la app está en español rioplatense).
- [ ] Widget con soporte para múltiples listas activas simultáneas.

---

## 📄 Licencia

Proyecto desarrollado con fines académicos en el marco de la materia **Desarrollo de Aplicaciones Mobile — UTN FRBA**. Todos los derechos reservados a sus autores.

---

<div align="center">

Hecho con 💙 por el equipo de **Tracksy**

</div>
