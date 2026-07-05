# Tracksy - Spec de autenticacion mobile con Firebase Auth

## Contexto

Tracksy esta en desarrollo y todavia no tiene usuarios reales. Es un proyecto academico para la materia Desarrollo de Aplicaciones Mobiles. Esto permite elegir una arquitectura de autenticacion simple, segura y centrada en mobile sin tener que migrar usuarios existentes.

Hoy el backend Django de la rama `backend` guarda usuarios y contrasenas en PostgreSQL. Si se mantiene ese esquema, los emails de recuperacion/verificacion deberian salir desde backend. Pero como el objetivo principal es mobile y no hay usuarios productivos, se propone mover la autenticacion a Firebase Auth y dejar Django/PostgreSQL para las funcionalidades de negocio.

## Decision propuesta

Usar Firebase Authentication como fuente de verdad para autenticacion.

Django/PostgreSQL queda como fuente de verdad para datos de negocio:

- Listas de compra.
- Favoritos.
- Productos asociados al usuario.
- Historial.
- Preferencias.
- Perfil extendido si hace falta.

Arquitectura objetivo:

```text
Android app -> Firebase Auth
Android app -> Django API -> PostgreSQL
Django API -> Firebase Admin SDK para validar ID tokens
```

Firebase maneja:

- Registro.
- Login.
- Logout.
- Verificacion de email.
- Recuperacion de contrasena.
- Cambio de contrasena.
- Sesion del usuario en mobile.

Django maneja:

- API de funcionalidades.
- Persistencia de datos de app.
- Asociacion de datos a un usuario mediante `firebase_uid`.

## Por que esta decision es razonable

- No hay usuarios reales que migrar.
- Evita guardar y mantener contrasenas propias en Django.
- Evita implementar tokens de reset/verificacion propios.
- Evita integrar Mailgun/Brevo/Resend y proteger API keys.
- Resuelve emails desde mobile mediante SDK oficial.
- Mantiene Django para todo lo que ya sirve a la app.

Firebase Pricing indica que Authentication tiene `50K MAUs` sin costo para otros servicios de autenticacion no telefonicos en el plan Spark. Para un proyecto academico, esto es suficiente.

Fuente: https://firebase.google.com/pricing

## Fuera de alcance

- Migrar usuarios existentes desde PostgreSQL.
- Mantener passwords en Django para usuarios mobile.
- Enviar emails con Mailgun directo desde Android.
- Implementar recuperacion de contrasena propia en Django.
- Usar SMS/Phone Auth, porque puede tener costo.

## Implicancias principales

### Usuarios

PostgreSQL ya no debe guardar contrasenas de usuarios mobile.

Debe guardar una representacion local del usuario para relacionar datos de negocio:

```text
id interno
firebase_uid
email
nombre/display_name
email_verified
created_at
updated_at
```

`firebase_uid` debe ser unico.

### Tokens

Mobile ya no debe enviar JWT de Django para autenticarse contra la API.

Mobile debe enviar Firebase ID Token:

```http
Authorization: Bearer <firebase_id_token>
```

Django debe validar ese token con Firebase Admin SDK. Si es valido:

1. Obtiene `uid`, `email`, `email_verified`.
2. Busca o crea el usuario local por `firebase_uid`.
3. Asocia `request.user` o un equivalente interno a ese usuario local.

### Passwords

Django no cambia ni valida contrasenas de usuarios mobile.

Firebase maneja:

- Password hashing.
- Reset por email.
- Cambio de contrasena autenticado.
- Reautenticacion cuando una accion sensible lo requiere.

### Admin Django

Se puede mantener Django Auth para administradores/backoffice.

Separacion recomendada:

- Admin/backoffice: usuarios Django tradicionales.
- Consumidores mobile: Firebase Auth.

Esto evita romper el admin de Django.

## Flujos mobile

### 1. Crear cuenta

Pantalla actual:

- `CreateAccountScreen` en `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/auth/AuthScreens.kt`.

Flujo objetivo:

1. Usuario ingresa nombre, email, contrasena y confirmacion.
2. Mobile valida formato de email y requisitos de contrasena.
3. Mobile llama a Firebase `createUserWithEmailAndPassword`.
4. Mobile actualiza `displayName` con el nombre.
5. Mobile llama `sendEmailVerification`.
6. Mobile obtiene Firebase ID Token.
7. Mobile llama a Django para sincronizar usuario local.
8. Mobile navega a pantalla "Revisa tu correo".

Endpoint Django propuesto:

```http
POST /api/v1/auth/firebase/sync/
Authorization: Bearer <firebase_id_token>
```

Respuesta:

```json
{
  "id": "...",
  "firebase_uid": "...",
  "email": "usuario@example.com",
  "nombre": "Usuario",
  "email_verified": false
}
```

### 2. Login

Pantalla actual:

- `LoginScreen` en `AuthScreens.kt`.

Flujo objetivo:

1. Usuario ingresa email y contrasena.
2. Mobile llama a Firebase `signInWithEmailAndPassword`.
3. Mobile obtiene Firebase ID Token.
4. Mobile llama a Django `/api/v1/auth/firebase/sync/` para asegurar usuario local.
5. Mobile usa ese ID Token para todas las llamadas a Django.

Regla:

- Si `emailVerified == false`, decidir si la app bloquea funcionalidades o solo muestra aviso.

Decision recomendada para MVP academico:

- Permitir login aunque no este verificado.
- Mostrar aviso y boton "Reenviar verificacion".

### 3. Verificacion de email

Pantallas actuales:

- `CheckEmailScreen`.
- Puede reutilizarse luego de registro.

Flujo:

1. Mobile llama `sendEmailVerification`.
2. Firebase envia email.
3. Usuario abre link.
4. Mobile puede refrescar usuario con `currentUser.reload()`.
5. Mobile actualiza estado local y sincroniza con Django.

Acciones mobile:

- Boton "Reenviar instrucciones" llama de nuevo a `sendEmailVerification`.
- Agregar cooldown local de 30 a 60 segundos.

### 4. Recuperar contrasena

Pantallas actuales:

- `RecoverPasswordScreen`.
- `CheckEmailScreen`.

Flujo:

1. Usuario toca "Olvidaste tu contrasena?".
2. Ingresa email.
3. Mobile llama a Firebase `sendPasswordResetEmail(email)`.
4. Mobile navega a `CheckEmailScreen`.
5. Usuario cambia contrasena desde el link enviado por Firebase.

Reglas:

- No mostrar si el email existe o no.
- Mantener copy generico: "Si existe una cuenta asociada...".
- Reenvio con cooldown.

### 5. Cambiar contrasena autenticado

Pantalla actual:

- `CambiarContrasenaScreen` en `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/profile/ChangePasswordScreen.kt`.

Flujo objetivo:

1. Usuario entra desde Perfil > Cambiar contrasena.
2. Mobile pide contrasena actual, nueva contrasena y confirmacion.
3. Mobile reautentica con Firebase usando email + contrasena actual.
4. Mobile llama `currentUser.updatePassword(newPassword)`.
5. Mobile muestra exito.

Cambio de UX recomendado:

- Cambiar CTA de "Enviar confirmacion por correo" a "Cambiar contrasena".
- Cambiar pantalla de "Revisa tu correo" por una pantalla de exito local.
- No enviar email propio para confirmar el cambio.

Nota:

- Firebase puede requerir login reciente para cambios sensibles.
- Si falla por reautenticacion, pedir contrasena actual de nuevo.

### 6. Logout

Flujo:

1. Mobile llama `FirebaseAuth.signOut()`.
2. Mobile limpia estado local.
3. No hay refresh token Django que invalidar para mobile.

## Cambios mobile requeridos

### Gradle

Agregar Firebase:

```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:<version>"))
    implementation("com.google.firebase:firebase-auth")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")
}
```

Agregar el plugin en version catalog o build raíz.

Agregar:

```text
tracksy-mobile/app/google-services.json
```

### AuthViewModel

Archivo actual:

- `tracksy-mobile/app/src/main/java/com/example/tracksy/viewmodel/AuthViewModel.kt`

Cambios:

- Login y registro usan `FirebaseAuthService`.
- Recuperacion, verificacion y cambio de contrasena usan Firebase Auth.
- El estado de autenticacion se basa en `FirebaseAuth.currentUser`.
- Luego de login/registro se llama a `/api/v1/auth/firebase/sync/`.

### TokenManager

Archivo actual:

- `tracksy-mobile/app/src/main/java/com/example/tracksy/data/local/TokenManager.kt`

Cambios:

- Ya no guarda access/refresh JWT de Django para mobile.
- Guarda solo metadata local no sensible, como modo oscuro o verificacion pendiente.
- El Firebase ID Token se pide a Firebase desde el interceptor porque expira.

### Retrofit/Repository

Cambios:

- Interceptor de OkHttp que agregue:

```http
Authorization: Bearer <firebase_id_token>
```

- Si el token expira, pedir token fresco con Firebase.
- Los ViewModels no deben pasar tokens manualmente.
- `TracksyRepository` no debe recibir token por parametro.

### ApiService

Eliminados del mobile:

- `POST /api/v1/auth/login/`
- `POST /api/v1/auth/registro/`
- `POST /api/v1/auth/refresh/`

Sync Firebase:

```kotlin
@POST("api/v1/auth/firebase/sync/")
suspend fun firebaseSync(): Response<FirebaseSyncResponse>
```

El header `Authorization` lo agrega OkHttp, no cada metodo Retrofit.

Mantener endpoints de negocio:

- Productos.
- Supermercados.
- Listas.
- Favoritos.
- Compras.
- Perfil extendido.

## Cambios backend requeridos

Aunque el foco sea mobile, Django debe poder validar usuarios Firebase para proteger las APIs.

### Dependencias

Agregar:

```text
firebase-admin
```

### Configuracion

Agregar credenciales Firebase Admin en backend.

No commitear el JSON de service account.

Variables sugeridas:

```env
FIREBASE_PROJECT_ID=...
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json
```

### Middleware o authentication class

Crear una clase de autenticacion DRF:

```text
FirebaseAuthentication
```

Responsabilidades:

1. Leer header `Authorization`.
2. Validar Firebase ID Token con Firebase Admin SDK.
3. Extraer `uid`, `email`, `email_verified`, `name`.
4. Buscar o crear usuario local por `firebase_uid`.
5. Adjuntar usuario local a `request.user`.

### Modelo local

Opcion A: adaptar `User` actual.

Agregar:

```python
firebase_uid = models.CharField(max_length=128, unique=True, null=True, blank=True)
```

Para usuarios mobile:

- `password` puede quedar unusable con `set_unusable_password()`.
- `email` sigue siendo unico.

Opcion B: crear modelo separado `MobileUser`.

Recomendacion:

- Para menor cambio en relaciones existentes, usar Opcion A y agregar `firebase_uid`.

### Sync endpoint

Endpoint:

```http
POST /api/v1/auth/firebase/sync/
```

Comportamiento:

- Requiere token Firebase valido.
- Crea usuario local si no existe.
- Actualiza email/display name/email_verified.
- Retorna perfil local.

## Datos y relaciones

Las relaciones actuales de listas/favoritos/compras deben seguir apuntando a un usuario local de Django/PostgreSQL.

El usuario local representa al usuario Firebase mediante `firebase_uid`.

Ejemplo:

```text
shopping_lists.user_id -> users.id
users.firebase_uid -> Firebase UID
```

## Manejo de usuarios existentes

Como no hay usuarios reales, no hace falta migracion compleja.

Pasos sugeridos:

1. Limpiar usuarios de desarrollo si hace falta.
2. Agregar campo `firebase_uid`.
3. Crear nuevos usuarios desde Firebase Auth.
4. Sincronizar automaticamente al primer login.

## Pantallas afectadas

### AuthScreens.kt

Afecta:

- `CreateAccountScreen`.
- `LoginScreen`.
- `RecoverPasswordScreen`.
- `CheckEmailScreen`.

Cambios:

- Los callbacks deben llamar Firebase Auth en lugar de endpoints Django.
- `RecoverPasswordScreen` debe llamar `sendPasswordResetEmail`.
- `CheckEmailScreen` debe soportar modo verificacion y modo reset.

### ChangePasswordScreen.kt

Afecta:

- `CambiarContrasenaScreen`.
- `NuevaContrasenaStep`.
- `CambioContrasenaExitoStep`.

Cambios:

- Agregar contrasena actual.
- Reautenticar con Firebase.
- Actualizar contrasena con Firebase.
- Reemplazar confirmacion por email por exito local.

### ProfileScreen/ProfileEditScreen.kt

Cambios:

- Email viene de Firebase.
- Nombre puede venir de Firebase displayName o del perfil Django.
- Si se permite cambiar email, usar flujo Firebase de verificacion de nuevo email.
- Para MVP, mantener email como solo lectura.

## Seguridad

Requisitos:

- No guardar API keys privadas en Android.
- `google-services.json` no debe tratarse como una API key privada, pero debe configurarse con restricciones apropiadas.
- Django debe validar ID Tokens en cada request protegida.
- Mobile no debe confiar solo en `uid` enviado en body.
- No usar email como identificador principal de relaciones; usar `firebase_uid` o usuario local.
- Phone Auth queda fuera por costo potencial.

## Criterios de aceptacion

- Usuario puede registrarse desde mobile usando Firebase Auth.
- Usuario recibe email de verificacion enviado por Firebase.
- Usuario puede iniciar sesion desde mobile.
- Usuario puede recuperar contrasena desde mobile.
- Usuario puede cambiar contrasena desde mobile.
- Las llamadas a Django para listas/favoritos/productos protegidos usan Firebase ID Token.
- Django valida el token y asocia la request al usuario local.
- Los datos de listas/favoritos quedan guardados en PostgreSQL.
- No se guardan contrasenas mobile en PostgreSQL.
- No se usa Mailgun/Brevo/Resend para auth mobile.

## Riesgos

- Requiere tocar backend para validar Firebase ID Tokens.
- Requiere configurar Firebase Console y agregar `google-services.json`.
- Si no se sincroniza usuario local, las APIs Django no van a saber a quien pertenecen las listas/favoritos.
- Si se mezclan login Django y Firebase login para usuarios mobile, aparecen inconsistencias.

## Decision final recomendada

Para este proyecto academico mobile sin usuarios reales:

- Firebase Auth para autenticacion y emails.
- Django/PostgreSQL para funcionalidades de negocio.
- Usuario local Django con `firebase_uid`.
- No usar Mailgun para auth.
- No enviar emails transaccionales directo desde Android con API keys privadas.
