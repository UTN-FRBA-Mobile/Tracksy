# Plan de integracion Firebase Auth en Tracksy

## Objetivo

Dejar la autenticacion de la app mobile funcionando con Firebase Auth, manteniendo Django/PostgreSQL para las funcionalidades de negocio: listas, favoritos, compras, productos, perfil extendido e historial.

La app esta en desarrollo y no hay usuarios reales, por lo que se puede migrar autenticacion sin plan de migracion de passwords existentes.

Arquitectura final:

```text
Android -> Firebase Auth
Android -> Django API -> PostgreSQL
Django -> valida Firebase ID token
```

## Estado actual

- Firebase ya esta configurado en Gradle.
- `google-services.json` esta ubicado en `tracksy-mobile/app/google-services.json`.
- `./gradlew :app:compileDebugKotlin` compila correctamente.
- El flujo actual de login/registro todavia usa Django:
  - `AuthViewModel.login()` llama `repo.login()`.
  - `AuthViewModel.registro()` llama `repo.registro()`.
- Listas, favoritos, compras, productos y perfil llaman al backend con Retrofit.
- La app no escribe directo en PostgreSQL.

## Fase 0 - Prueba temporal de Firebase Auth

Objetivo: validar que Firebase esta bien configurado antes de migrar todo el auth.

Cambios:

1. Agregar dependencia:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")
```

2. Modificar temporalmente `AuthViewModel.registro()` para:

```kotlin
FirebaseAuth.getInstance()
    .createUserWithEmailAndPassword(email, password)
    .await()

FirebaseAuth.getInstance()
    .currentUser
    ?.sendEmailVerification()
    ?.await()
```

3. No tocar todavia listas/favoritos/backend.
4. Crear cuenta desde la app.
5. Verificar:
   - Usuario aparece en Firebase Console > Authentication > Users.
   - Llega email de verificacion.

Criterio de salida:

- Firebase crea usuario correctamente desde la app.
- El mail de verificacion se envia.
- Se documenta cualquier error de Firebase Console, SHA/package o Auth provider.

Nota:

- Esta fase es temporal. No debe quedar como solucion final si rompe el login actual contra Django.

## Fase 1 - Definir modelo final de usuario

Objetivo: separar autenticacion de datos de negocio.

Decisiones:

- Firebase Auth es fuente de verdad para:
  - email
  - password
  - email verification
  - reset password
  - sesion mobile

- Django/PostgreSQL es fuente de verdad para:
  - listas
  - favoritos
  - compras
  - historial
  - preferencias
  - perfil extendido

Cambios backend esperados:

1. Agregar `firebase_uid` al usuario local Django.
2. Mantener `email` como dato sincronizado.
3. Para usuarios mobile, no usar password Django.
4. Mantener usuarios Django tradicionales para admin/backoffice si hace falta.

Modelo sugerido:

```python
firebase_uid = models.CharField(max_length=128, unique=True, null=True, blank=True)
```

Criterio de salida:

- Existe forma de asociar datos PostgreSQL con un usuario Firebase.

## Fase 2 - Adaptar backend Django para Firebase ID tokens

Objetivo: que Django acepte requests autenticadas desde mobile con Firebase ID Token.

Cambios:

1. Agregar dependencia backend:

```text
firebase-admin
```

2. Configurar credenciales de Firebase Admin.

Variables sugeridas:

```env
FIREBASE_PROJECT_ID=...
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json
```

3. Crear authentication class para DRF:

```text
FirebaseAuthentication
```

Responsabilidades:

- Leer `Authorization: Bearer <firebase_id_token>`.
- Validar token con Firebase Admin SDK.
- Extraer `uid`, `email`, `email_verified`, `name`.
- Buscar o crear usuario local por `firebase_uid`.
- Setear `request.user`.

4. Crear endpoint de sincronizacion:

```http
POST /api/v1/auth/firebase/sync/
Authorization: Bearer <firebase_id_token>
```

5. Revisar permisos de endpoints protegidos.

Criterio de salida:

- Un token Firebase valido permite llamar endpoints Django protegidos.
- Un token invalido/expirado devuelve 401.
- Las listas se asocian al usuario local correcto.

## Fase 3 - Migrar auth mobile a Firebase

Objetivo: reemplazar login/registro Django por Firebase en la app.

Cambios mobile:

1. Crear servicio:

```text
tracksy-mobile/app/src/main/java/com/example/tracksy/data/auth/FirebaseAuthService.kt
```

Responsabilidades:

- `register(name, email, password)`
- `login(email, password)`
- `logout()`
- `sendEmailVerification()`
- `sendPasswordReset(email)`
- `changePassword(currentPassword, newPassword)`
- `getIdToken(forceRefresh: Boolean = false)`
- `isAuthenticated()`
- `isEmailVerified()`

2. Actualizar `AuthViewModel`:

- `login()` usa Firebase.
- `registro()` usa Firebase.
- Estado de autenticacion se basa en `FirebaseAuth.currentUser`.
- Luego de login/registro llama sync con backend.

3. Dejar obsoletos para mobile:

- `repo.login()`
- `repo.registro()`
- `repo.refreshToken()`
- `TokenManager.accessToken`
- `TokenManager.refreshToken`

Criterio de salida:

- Login y registro funcionan sin endpoints Django de auth tradicional.
- Usuarios aparecen en Firebase Console.
- La app navega correctamente al home luego de login.

## Fase 4 - Autorizacion Retrofit con Firebase token

Objetivo: que todas las llamadas al backend usen Firebase ID Token.

Cambios:

1. Crear interceptor OkHttp que agregue:

```http
Authorization: Bearer <firebase_id_token>
```

2. Refrescar token cuando haga falta.
3. Eliminar dependencia de `TokenManager.accessToken` en viewmodels.
4. Centralizar token en repository/interceptor, no pasarlo manualmente desde cada ViewModel.

Estado actual a modificar:

- `ListaViewModel` usa `tokenManager.accessToken`.
- `ProductoViewModel` usa `tokenManager.accessToken`.
- `CompraViewModel` usa `tokenManager.accessToken`.
- `PerfilViewModel` usa `tokenManager.accessToken`.
- `SugerenciaViewModel` usa `tokenManager.accessToken`.
- `TracksyRepository` recibe `token` en casi todos los metodos.

Refactor recomendado:

- `TracksyRepository` no recibe token por parametro.
- El interceptor agrega el header automaticamente.

Criterio de salida:

- Listas/favoritos/compras siguen funcionando usando token Firebase.
- No se pasan tokens manualmente por todos los ViewModels.

## Fase 5 - Emails de auth

Objetivo: completar los flujos de email desde mobile usando Firebase Auth.

### Verificacion de email

Flujo:

1. Registro exitoso.
2. App llama `sendEmailVerification()`.
3. App muestra `CheckEmailScreen`.
4. Boton "Reenviar instrucciones" llama de nuevo a `sendEmailVerification()`.
5. App permite refrescar estado con `currentUser.reload()`.

Decisiones:

- Para MVP, permitir login aunque `emailVerified == false`, pero mostrar aviso.
- Opcional: bloquear funcionalidades sensibles hasta verificar.

### Recuperacion de contrasena

Flujo:

1. `RecoverPasswordScreen`.
2. App llama `sendPasswordResetEmail(email)`.
3. App muestra `CheckEmailScreen`.
4. Boton "Reenviar instrucciones" repite envio con cooldown.

Reglas:

- No revelar si el email existe.
- Mensajes genericos.
- Cooldown local 30-60 segundos.

### Cambio de contrasena

Flujo:

1. Usuario autenticado entra desde perfil.
2. App pide contrasena actual.
3. App reautentica contra Firebase.
4. App llama `updatePassword(newPassword)`.
5. App muestra exito local.

Cambios UI:

- `ChangePasswordScreen` debe agregar campo "Contrasena actual".
- Cambiar CTA de "Enviar confirmacion por correo" a "Cambiar contrasena".
- Reemplazar pantalla de "Revisa tu correo" por pantalla de exito.

Criterio de salida:

- Verificacion, reset y cambio de contrasena funcionan con Firebase.

## Fase 6 - Perfil y email

Objetivo: evitar inconsistencias entre Firebase y Django.

Reglas:

- Email se lee desde Firebase.
- Perfil extendido se lee desde Django.
- Para MVP, email debe ser solo lectura en `ProfileEditScreen`.
- Nombre puede sincronizarse:
  - Firebase `displayName`, o
  - perfil Django.

Decision recomendada:

- Usar Firebase para email.
- Usar Django para nombre/perfil extendido.
- En sync backend, copiar email desde Firebase al usuario local.

Criterio de salida:

- Perfil muestra email correcto.
- Editar perfil no intenta cambiar email directamente en Django.

## Fase 7 - Limpieza

Objetivo: remover caminos viejos y reducir confusion.

Tareas:

- Eliminar o marcar como deprecated endpoints mobile de login Django.
- Revisar nombres de endpoints mobile:
  - hoy mobile usa `/api/v1/auth/registro/`
  - backend usa `/api/v1/auth/register/`
- Eliminar uso de access/refresh token Django en mobile.
- Actualizar `specs.md` si cambia alguna decision.
- Documentar setup Firebase en README.

Criterio de salida:

- No quedan dos flujos de auth activos para usuarios mobile.

## Validaciones finales

Checklist:

- Crear cuenta desde mobile.
- Ver usuario en Firebase Console.
- Recibir email de verificacion.
- Login desde mobile.
- Recuperar contrasena.
- Cambiar contrasena.
- Crear lista.
- Editar lista.
- Eliminar lista.
- Agregar favorito.
- Finalizar compra.
- Cerrar sesion.
- Reabrir app y mantener estado correcto.

## Riesgos

- Si Django no valida Firebase tokens, las funcionalidades de negocio quedan inaccesibles.
- Si se mantiene auth Django y Firebase a la vez para usuarios mobile, aparecen inconsistencias.
- Si no se sincroniza usuario local por `firebase_uid`, las relaciones PostgreSQL no tienen owner confiable.
- Si se intenta enviar emails con Mailgun directo desde Android, se exponen credenciales privadas.

## Orden recomendado de implementacion

1. Prueba temporal Firebase Auth en registro.
2. Backend valida Firebase ID Token.
3. Mobile obtiene y envia Firebase ID Token.
4. Migrar login/registro definitivo.
5. Recuperacion/verificacion/cambio de contrasena.
6. Refactor de tokens en repositories/viewmodels.
7. Limpieza de auth vieja.
