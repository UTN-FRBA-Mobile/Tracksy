# Tracksy API — Ejemplos de Payloads JSON

## Auth

### POST /api/v1/auth/register/
**Request:**
```json
{
  "email": "usuario@example.com",
  "username": "usuario123",
  "password": "MiPassword123!",
  "password_confirm": "MiPassword123!"
}
```
**Response 201:**
```json
{
  "message": "Registro exitoso.",
  "user_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### POST /api/v1/auth/login/
**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "MiPassword123!"
}
```
**Response 200:**
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### POST /api/v1/auth/refresh/
**Request:**
```json
{
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
**Response 200:**
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## Users

### GET /api/v1/users/me/
**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@example.com",
  "username": "usuario123",
  "role": "consumer",
  "is_email_verified": false,
  "profile": {
    "first_name": "Juan",
    "last_name": "Pérez",
    "phone": "+54911234567",
    "avatar": null,
    "city": "Buenos Aires",
    "province": "CABA"
  },
  "preferences": {
    "notify_promotions": true,
    "notify_price_drops": true,
    "default_search_radius_km": 10,
    "currency": "ARS"
  },
  "created_at": "2026-04-26T10:00:00Z"
}
```

### PATCH /api/v1/users/me/
**Request:**
```json
{
  "profile": {
    "first_name": "Juan",
    "city": "Rosario"
  },
  "preferences": {
    "default_search_radius_km": 5
  }
}
```

---

## Products

### GET /api/v1/products/?search=leche&category_slug=lacteos&page=1
**Response 200:**
```json
{
  "count": 42,
  "total_pages": 3,
  "next": "http://localhost:8000/api/v1/products/?page=2",
  "previous": null,
  "results": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "barcode": "7790040015040",
      "name": "Leche Entera La Serenísima 1L",
      "brand_name": "La Serenísima",
      "category_name": "Lácteos",
      "presentation": "Caja",
      "net_content": "1 L",
      "thumbnail_url": "http://localhost:8000/media/products/leche.jpg"
    }
  ]
}
```

### GET /api/v1/products/{id}/
**Response 200:**
```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "barcode": "7790040015040",
  "name": "Leche Entera La Serenísima 1L",
  "description": "Leche entera larga vida en envase Tetra Pak.",
  "brand": { "id": "...", "name": "La Serenísima", "slug": "la-serenisima", "logo": null },
  "category": { "id": "...", "name": "Lácteos", "slug": "lacteos", "icon": "milk" },
  "subcategory": { "id": "...", "name": "Leches", "slug": "leches" },
  "measurement_unit": { "id": "...", "name": "Litro", "abbreviation": "L" },
  "presentation": "Caja",
  "net_content": "1 L",
  "thumbnail": "http://localhost:8000/media/products/leche.jpg",
  "images": [],
  "is_active": true,
  "created_at": "2026-04-01T00:00:00Z",
  "updated_at": "2026-04-20T00:00:00Z"
}
```

### GET /api/v1/products/barcode/7790040015040/
_Mismo formato que detalle del producto._

---

## Supermarkets

### GET /api/v1/supermarkets/
**Response 200 (paginado):**
```json
{
  "results": [
    {
      "id": "aabb1234-...",
      "name": "Carrefour",
      "slug": "carrefour",
      "logo": "http://localhost:8000/media/supermarkets/logos/carrefour.png",
      "website": "https://www.carrefour.com.ar",
      "branches_count": 87
    }
  ]
}
```

### GET /api/v1/supermarkets/branches/nearby/?lat=-34.6037&lon=-58.3816&radius_km=3
**Response 200:**
```json
[
  {
    "id": "ccdd5678-...",
    "chain_name": "Carrefour",
    "name": "Carrefour Palermo",
    "address": "Av. Santa Fe 3401",
    "city": "Buenos Aires",
    "province": "CABA",
    "latitude": "-34.5953",
    "longitude": "-58.4101",
    "distance_km": 1.23
  }
]
```

---

## Prices

### GET /api/v1/products/{id}/prices/
**Response 200:**
```json
[
  {
    "id": "...",
    "branch_id": "ccdd5678-...",
    "branch_name": "Carrefour Palermo",
    "chain_name": "Carrefour",
    "chain_logo": "http://...",
    "city": "Buenos Aires",
    "province": "CABA",
    "price": "249.90",
    "currency": "ARS",
    "valid_from": "2026-04-15",
    "updated_at": "2026-04-20T14:30:00Z"
  },
  {
    "branch_name": "Disco Belgrano",
    "chain_name": "Disco",
    "price": "269.00",
    "currency": "ARS"
  }
]
```

### GET /api/v1/products/{id}/price-comparison/
**Response 200:**
```json
{
  "product_id": "7c9e6679-...",
  "product_name": "Leche Entera La Serenísima 1L",
  "prices": [
    { "chain_name": "Carrefour", "price": 249.90 },
    { "chain_name": "Coto", "price": 259.00 },
    { "chain_name": "Disco", "price": 269.00 }
  ],
  "cheapest": { "chain_name": "Carrefour", "price": 249.90 },
  "most_expensive": { "chain_name": "Disco", "price": 269.00 },
  "potential_saving": 19.10,
  "currency": "ARS"
}
```

---

## Promotions

### GET /api/v1/promotions/
**Response 200 (paginado):**
```json
{
  "results": [
    {
      "id": "promo-uuid-...",
      "name": "25% OFF con Visa los martes",
      "promotion_type_label": "Promoción bancaria / medio de pago",
      "discount_percentage": "25.00",
      "discount_amount": null,
      "start_date": "2026-04-01",
      "end_date": "2026-06-30",
      "banner_image": "http://...",
      "is_currently_active": true
    }
  ]
}
```

### GET /api/v1/promotions/{id}/
**Response 200:**
```json
{
  "id": "promo-uuid-...",
  "name": "25% OFF con Visa los martes",
  "description": "Descuento en productos seleccionados.",
  "promotion_type": { "code": "bank", "label": "Promoción bancaria / medio de pago" },
  "discount_percentage": "25.00",
  "max_refund_amount": "1000.00",
  "start_date": "2026-04-01",
  "end_date": "2026-06-30",
  "supermarkets": [{ "id": "...", "name": "Carrefour" }],
  "products": [],
  "categories": [{ "id": "...", "name": "Lácteos" }],
  "payment_methods": [{ "method": "visa", "bank_name": "Santander" }],
  "days_of_week": [{ "weekday": 1, "weekday_label": "Martes" }],
  "terms": "Válido solo en cajas habilitadas.",
  "is_currently_active": true
}
```

---

## Shopping Lists

### POST /api/v1/shopping-lists/
**Request:**
```json
{
  "name": "Compras del viernes",
  "preferred_branch": "ccdd5678-...",
  "notes": "Llevar bolsas reutilizables"
}
```
**Response 201:**
```json
{
  "id": "list-uuid-...",
  "name": "Compras del viernes",
  "preferred_branch": "ccdd5678-...",
  "preferred_branch_name": "Carrefour — Carrefour Palermo",
  "status": "active",
  "notes": "Llevar bolsas reutilizables",
  "total_items": 0,
  "purchased_items": 0,
  "items": [],
  "created_at": "2026-04-26T12:00:00Z"
}
```

### POST /api/v1/shopping-lists/{id}/items/
**Request:**
```json
{
  "product_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "quantity": 2,
  "notes": "Preferir marca blanca si hay"
}
```
**Response 201:**
```json
{
  "id": "item-uuid-...",
  "product_id": "7c9e6679-...",
  "product_name": "Leche Entera La Serenísima 1L",
  "product_brand": "La Serenísima",
  "quantity": 2,
  "is_purchased": false,
  "notes": "Preferir marca blanca si hay"
}
```

### GET /api/v1/shopping-lists/{id}/estimate/?branch_id=ccdd5678-...
**Response 200:**
```json
{
  "branch_id": "ccdd5678-...",
  "branch_name": "Carrefour Palermo",
  "chain_name": "Carrefour",
  "total_estimated": 1247.80,
  "items_with_price": 8,
  "items_without_price": 1,
  "currency": "ARS",
  "breakdown": [
    {
      "product_id": "7c9e6679-...",
      "product_name": "Leche Entera La Serenísima 1L",
      "quantity": 2,
      "unit_price": 249.90,
      "line_total": 499.80
    }
  ]
}
```

### GET /api/v1/shopping-lists/{id}/compare-supermarkets/
**Response 200:**
```json
{
  "list_id": "list-uuid-...",
  "list_name": "Compras del viernes",
  "comparisons": [
    {
      "chain_name": "Carrefour",
      "branch_name": "Carrefour Palermo",
      "total_estimated": 1247.80,
      "items_with_price": 8,
      "items_without_price": 1
    },
    {
      "chain_name": "Coto",
      "branch_name": "Coto Belgrano",
      "total_estimated": 1289.50,
      "items_with_price": 7,
      "items_without_price": 2
    }
  ],
  "best_option": {
    "chain_name": "Carrefour",
    "total_estimated": 1247.80
  }
}
```

---

## Favorites

### POST /api/v1/users/favorites/products/
**Request:**
```json
{ "product_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7" }
```
**Response 201:**
```json
{
  "id": "fav-uuid-...",
  "product": {
    "id": "7c9e6679-...",
    "name": "Leche Entera La Serenísima 1L",
    "brand_name": "La Serenísima"
  },
  "created_at": "2026-04-26T13:00:00Z"
}
```

---

## Admin — Backoffice

### POST /api/v1/admin-api/imports/
**Request (multipart/form-data):**
```
import_type=products
file=<CSV file>
```
**CSV products format:**
```
barcode,name,brand_name,category_name,presentation,net_content,description
7790040015040,Leche Entera La Serenísima 1L,La Serenísima,Lácteos,Caja,1 L,Leche entera larga vida
```

**CSV prices format:**
```
barcode,branch_code,price,valid_from
7790040015040,CAR-PAL-001,249.90,2026-04-15
```

**Response 201:**
```json
{
  "id": "batch-uuid-...",
  "import_type": "products",
  "status": "completed",
  "total_rows": 150,
  "success_rows": 148,
  "error_rows": 2,
  "errors": [
    {
      "row_number": 34,
      "field": "barcode",
      "error_message": "Código de barras inválido (solo alfanumérico).",
      "raw_data": { "barcode": "779-004-001" }
    }
  ]
}
```

---

## Comparisons

### GET /api/v1/comparisons/recommend/?product_ids=uuid1,uuid2,uuid3
**Response 200:**
```json
{
  "recommendation": {
    "branch_id": "ccdd5678-...",
    "branch_name": "Carrefour Palermo",
    "chain_name": "Carrefour",
    "total": 748.70,
    "products_found": 3
  },
  "alternatives": [
    { "chain_name": "Coto", "total": 789.20 },
    { "chain_name": "Disco", "total": 812.00 }
  ],
  "total_products_searched": 3
}
```

---

## Error Responses

All errors follow a standard envelope:

```json
{
  "success": false,
  "error": {
    "status_code": 400,
    "message": "email: Este campo es requerido.",
    "detail": { "email": ["Este campo es requerido."] }
  }
}
```

## HTTP Headers for Android Client

```
Authorization: Bearer <access_token>
Content-Type: application/json
Accept: application/json
Accept-Language: es
```
