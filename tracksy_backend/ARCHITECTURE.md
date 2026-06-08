# Tracksy Backend — Arquitectura y Guía Técnica

## Estructura del Proyecto

```
tracksy_backend/
├── config/
│   ├── settings/
│   │   ├── base.py          ← configuración común
│   │   ├── development.py   ← DEBUG=True, BrowsableAPI
│   │   └── production.py    ← HTTPS, headers de seguridad
│   ├── urls.py              ← enrutador raíz
│   └── wsgi.py
│
├── apps/
│   ├── common/              ← BaseModel, paginación, permisos, excepciones
│   ├── users/               ← User, UserProfile, Favoritos, JWT auth
│   ├── products/            ← Product, Category, Brand, Imágenes
│   ├── supermarkets/        ← SupermarketChain, Branch, Horarios
│   ├── prices/              ← ProductPrice, PriceHistory
│   ├── promotions/          ← Promotion, tipos, productos, condiciones
│   ├── shopping_lists/      ← ShoppingList, Items, Estimaciones
│   ├── comparisons/         ← Servicios de comparación (sin modelos propios)
│   ├── imports/             ← ImportBatch, ImportError, servicios CSV
│   └── audit/               ← AuditLog, AuditMiddleware
│
├── requirements.txt
├── manage.py
├── ARCHITECTURE.md          ← este archivo
└── API_PAYLOADS.md          ← ejemplos de JSON
```

---

## Base de Datos — Diagrama de Relaciones

### Módulo Usuarios
```
users ──1:1── user_profiles
users ──1:1── user_preferences
users ──1:N── favorite_products ──N:1── products
users ──1:N── favorite_supermarkets ──N:1── supermarket_chains
users ──1:N── shopping_lists
users ──1:N── audit_logs
```

### Módulo Productos
```
categories ──1:N── subcategories
categories ──1:N── products
subcategories ──1:N── products
brands ──1:N── products
measurement_units ──1:N── products
products ──1:N── product_images
```

### Módulo Supermercados
```
supermarket_chains ──1:N── supermarket_branches
supermarket_branches ──1:N── branch_opening_hours
```

### Módulo Precios
```
products ──1:N── product_prices ──N:1── supermarket_branches
products ──1:N── price_history ──N:1── supermarket_branches
```
Cada vez que se guarda un ProductPrice activo, se deja un registro inmutable en price_history.

### Módulo Promociones
```
promotion_types ──1:N── promotions
promotions ──1:N── promotion_products ──N:1── products
promotions ──1:N── promotion_categories ──N:1── categories
promotions ──1:N── promotion_supermarkets ──N:1── supermarket_chains
promotions ──1:N── promotion_payment_methods
promotions ──1:N── promotion_days_of_week
```

### Módulo Listas de Compra
```
users ──1:N── shopping_lists ──N:1── supermarket_branches (opcional)
shopping_lists ──1:N── shopping_list_items ──N:1── products
shopping_lists ──1:N── shopping_list_estimations ──N:1── supermarket_branches
```

### Módulo Importaciones
```
users ──1:N── import_batches
import_batches ──1:N── import_errors
```

---

## Roles y Permisos

| Rol             | Acceso                                                   |
|-----------------|----------------------------------------------------------|
| consumer        | Endpoints de lectura, listas propias, favoritos propios  |
| admin           | Acceso total (CRUD + backoffice + importaciones)         |
| backoffice      | CRUD de productos, precios, supermercados, promociones   |

Los permisos están implementados en `apps/common/permissions.py`:
- `IsConsumer` — solo consumidores
- `IsAdminOrBackoffice` — admin o backoffice
- `IsAdmin` — solo admin
- `IsOwnerOrAdmin` — propietario del recurso o admin

---

## Flujo de Autenticación JWT

```
Android App                     Backend
     │                              │
     │  POST /auth/login/           │
     │ ──────────────────────────► │
     │                              │  Valida email+password
     │  { access, refresh }         │
     │ ◄────────────────────────── │
     │                              │
     │  GET /users/me/              │
     │  Authorization: Bearer JWT   │
     │ ──────────────────────────► │
     │                              │  Verifica JWT
     │  { user profile }            │
     │ ◄────────────────────────── │
     │                              │
     │  POST /auth/refresh/         │  (cuando access expira)
     │ ──────────────────────────► │
     │  { new access }              │
     │ ◄────────────────────────── │
```

- `ACCESS_TOKEN_LIFETIME` = 60 minutos (configurable en .env)
- `REFRESH_TOKEN_LIFETIME` = 7 días
- `ROTATE_REFRESH_TOKENS = True` — cada refresh genera un nuevo refresh token
- `BLACKLIST_AFTER_ROTATION = True` — el refresh token anterior queda inválido

---

## Servicios de Negocio

### PriceComparisonService
Entrada: `product` (instancia de Product)
Salida: lista de precios ordenados por monto + cheapest/most_expensive + ahorro potencial

### ShoppingListEstimatorService
Entrada: `shopping_list`, `branch`
Salida: desglose ítem a ítem con precio y total estimado

Método `compare_all_branches`: recorre todas las sucursales con al menos un precio para los productos de la lista, ordena por total.

### SupermarketRecommendationService
Entrada: lista de UUIDs de productos
Salida: sucursal con menor suma total de precios activos

### PromotionApplicationService
Entrada: lista de UUIDs de productos + chain_id
Salida: promociones activas que aplican a esa combinación

---

## Importación CSV

El endpoint `POST /api/v1/admin-api/imports/` acepta:
- `import_type`: `products` | `prices` | `supermarkets` | `branches` | `promotions`
- `file`: archivo CSV

**Flujo:**
1. Se crea `ImportBatch` con status `pending`
2. El servicio correspondiente abre el archivo, parsea filas
3. Cada fila válida hace `update_or_create` (idempotente)
4. Los errores se acumulan en `ImportError`
5. Al finalizar, el batch queda en `completed` o `partial`

**Para producción:** conectar el método `process()` a una tarea Celery para no bloquear el request.

---

## Índices de Base de Datos Relevantes

```sql
-- Búsqueda de productos
idx_product_name       ON products(name)
idx_product_barcode    ON products(barcode)
idx_product_active     ON products(is_active)

-- Consulta de precios
idx_price_lookup       ON product_prices(product_id, branch_id, is_active)
idx_price_comparison   ON product_prices(product_id, price)
idx_price_history      ON price_history(product_id, branch_id, created_at)

-- Supermercados cercanos
idx_branch_geo         ON supermarket_branches(latitude, longitude)
idx_branch_location    ON supermarket_branches(city, province)

-- Promociones vigentes
idx_promo_active       ON promotions(is_active, start_date, end_date)

-- Listas de compra
idx_list_user_status   ON shopping_lists(user_id, status)

-- Auditoría
idx_audit_entity       ON audit_logs(entity_type, entity_id)
idx_audit_user         ON audit_logs(user_id, created_at)
```

---

## Buenas Prácticas para Consumo desde Android/Kotlin

### 1. Retrofit + Kotlinx Serialization
```kotlin
// build.gradle.kts
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
```

### 2. Interceptor JWT con renovación automática
```kotlin
class AuthInterceptor(private val tokenRepo: TokenRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("Authorization", "Bearer ${tokenRepo.accessToken}")
            .header("Accept", "application/json")
            .build()
        val response = chain.proceed(req)
        if (response.code == 401) {
            tokenRepo.refresh()  // POST /auth/refresh/
            return chain.proceed(req.newBuilder()
                .header("Authorization", "Bearer ${tokenRepo.accessToken}")
                .build())
        }
        return response
    }
}
```

### 3. Manejo del envelope de errores
```kotlin
@Serializable
data class ApiError(
    val success: Boolean,
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val status_code: Int,
    val message: String
)

// Uso:
if (!response.isSuccessful) {
    val err = json.decodeFromString<ApiError>(response.errorBody()!!.string())
    showError(err.error.message)
}
```

### 4. Paginación
La API devuelve:
```json
{ "count": 42, "total_pages": 3, "next": "url?page=2", "previous": null, "results": [...] }
```
Usar `count` / `total_pages` para implementar scroll infinito o paginación discreta.

### 5. Endpoints clave para la app

| Pantalla                  | Endpoint                                                 |
|---------------------------|----------------------------------------------------------|
| Búsqueda de productos     | `GET /products/?search=leche`                           |
| Escaneo de barcode        | `GET /products/barcode/{code}/`                         |
| Comparar precios          | `GET /products/{id}/price-comparison/`                  |
| Supermercados cercanos    | `GET /supermarkets/branches/nearby/?lat=X&lon=Y`        |
| Lista de compra           | `GET /shopping-lists/{id}/compare-supermarkets/`        |
| Promociones del super     | `GET /supermarkets/{id}/promotions/`                    |
| Recomendación             | `GET /comparisons/recommend/?product_ids=a,b,c`         |

### 6. Permisos de Android necesarios
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>  <!-- sucursales cercanas -->
<uses-permission android:name="android.permission.CAMERA"/>               <!-- escaneo de barcode -->
```

### 7. Almacenamiento seguro del JWT
```kotlin
// Usar EncryptedSharedPreferences (Jetpack Security)
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()
val prefs = EncryptedSharedPreferences.create(
    context, "tracksy_tokens", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## Variables de Entorno (.env)

| Variable                             | Descripción                         | Default          |
|--------------------------------------|-------------------------------------|------------------|
| SECRET_KEY                           | Clave secreta Django                | —                |
| DEBUG                                | Modo debug                          | False            |
| DB_NAME / DB_USER / DB_PASSWORD      | Credenciales PostgreSQL             | —                |
| DB_HOST / DB_PORT                    | Host y puerto PostgreSQL            | localhost / 5432 |
| JWT_ACCESS_TOKEN_LIFETIME_MINUTES    | Vida del access token               | 60               |
| JWT_REFRESH_TOKEN_LIFETIME_DAYS      | Vida del refresh token              | 7                |
| REDIS_URL                            | URL de Redis para Celery            | redis://...      |
| CORS_ALLOWED_ORIGINS                 | Origines CORS permitidos            | localhost:3000   |

---

## Comandos de Instalación

```bash
# 1. Crear entorno virtual
python -m venv venv
source venv/bin/activate        # Linux/Mac
.\venv\Scripts\activate         # Windows

# 2. Instalar dependencias
pip install -r requirements.txt

# 3. Configurar variables de entorno
cp .env.example .env
# Editar .env con los valores reales

# 4. Crear base de datos PostgreSQL
createdb tracksy_db

python manage.py makemigrations users
python manage.py makemigrations products
python manage.py makemigrations supermarkets
python manage.py makemigrations shopping_lists
python manage.py makemigrations compras
python manage.py makemigrations sugerencias
python manage.py makemigrations imports

# 5. Aplicar migraciones
python manage.py migrate

# 6. Crear superusuario
python manage.py createsuperuser

# 7. Ejecutar servidor de desarrollo
python manage.py runserver

# 8. Acceder a la documentación
# Swagger: http://localhost:8000/api/docs/
# ReDoc:   http://localhost:8000/api/redoc/
# Admin:   http://localhost:8000/tracksy-admin/
```
