package com.example.tracksy.data.models

import com.google.gson.annotations.SerializedName

// ── Auth ─────────────────────────────────────────────────────────────────────

data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class RegistroResponse(
    val message: String,
    val id: Int
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class RefreshRequest(
    val refresh: String
)

// ── Usuario ───────────────────────────────────────────────────────────────────

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String
)

data class ProductoUsuario(
    val id: Int,
    val usuario: Int,
    val producto: Long,   // EAN-13 barcode = product PK
    val favorito: Boolean
)

// Request body para POST /api/v1/usuarios/favoritos/
// Usar data class en lugar de Map<String, Any> evita el problema de wildcards
// que Kotlin genera para Map<K, out V> en el bytecode JVM.
data class AddFavoritoRequest(
    val producto: Long,
    val favorito: Boolean = true
)

// ── Productos ─────────────────────────────────────────────────────────────────

data class Marca(
    val id: Int,
    val nombre: String
)

data class MarcaDetalle(
    val id: Int,
    val nombre: String
)

// Formato del endpoint LIST /api/v1/productos/ → marca como Int + marca_nombre como String
data class Producto(
    val id: Long,
    val nombre: String,
    val marca: Int?,
    @SerializedName("marca_nombre") val marcaNombre: String?
)

// Formato del endpoint DETAIL /api/v1/productos/{id}/ → marca como objeto anidado
data class ProductoDetalle(
    val id: Long,
    val nombre: String,
    val marca: MarcaDetalle?
) {
    val marcaNombre: String? get() = marca?.nombre
}

// ── Supermercados ─────────────────────────────────────────────────────────────

data class Supermercado(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

data class ProductoListado(
    val id: Int,
    val producto: Long,   // EAN-13 barcode
    @SerializedName("producto_nombre") val productoNombre: String,
    val supermercado: Int,
    @SerializedName("supermercado_nombre") val supermercadoNombre: String,
    val precio: Double,
    val disponible: Boolean,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: String
)

// ── Listas de compra ──────────────────────────────────────────────────────────

data class EstadoProducto(
    val id: Int,
    val nombre: String
)

data class ItemProducto(
    val id: Int,
    val lista: Int,
    val producto: Long,   // EAN-13 barcode
    @SerializedName("producto_nombre") val productoNombre: String,
    val cantidad: Int,
    val estado: Int,
    @SerializedName("estado_nombre") val estadoNombre: String,
    @SerializedName("precio_unitario") val precioUnitario: Double
)

data class ListaCompra(
    val id: Int,
    val usuario: Int,
    @SerializedName("usuario_email") val usuarioEmail: String,
    val supermercado: Int?,
    val nombre: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("total_estimado") val totalEstimado: Double,
    val items: List<ItemProducto>
)

data class ListaCompraRequest(
    val nombre: String,
    val supermercado: Int? = null,
    @SerializedName("total_estimado") val totalEstimado: Double = 0.0
)

data class ItemProductoRequest(
    val producto: Long,   // EAN-13 barcode
    val cantidad: Int,
    val estado: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double
)

// ── Compras ───────────────────────────────────────────────────────────────────

data class ProductoComprado(
    val id: Int,
    val compra: Int,
    val producto: Long,   // EAN-13 barcode
    @SerializedName("producto_nombre") val productoNombre: String,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double
)

data class Compra(
    val id: Int,
    val usuario: Int,
    val supermercado: Int,
    @SerializedName("supermercado_nombre") val supermercadoNombre: String,
    val fecha: String,
    val total: Double,
    val productos: List<ProductoComprado>
)

data class CompraRequest(
    val supermercado: Int,
    val total: Double,
    val productos: List<ProductoCompradoRequest>
)

data class ProductoCompradoRequest(
    val producto: Long,   // EAN-13 barcode
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double
)

// ── Sugerencias ───────────────────────────────────────────────────────────────

data class Estado(
    val id: Int,
    val nombre: String
)

data class FeedbackSugerencia(
    val id: Int,
    val sugerencia: Int,
    @SerializedName("fue_util") val fueUtil: Boolean,
    val fecha: String
)

data class Sugerencia(
    val id: Int,
    val usuario: Int,
    val producto: Long,   // EAN-13 barcode
    @SerializedName("producto_nombre") val productoNombre: String,
    val fecha: String,
    val estado: Int,
    @SerializedName("estado_nombre") val estadoNombre: String,
    val motivo: String,
    val feedbacks: List<FeedbackSugerencia>
)

data class SugerenciaRequest(
    val producto: Long,   // EAN-13 barcode
    val estado: Int,
    val motivo: String
)

data class FeedbackRequest(
    @SerializedName("fue_util") val fueUtil: Boolean
)

// ── Paginación ────────────────────────────────────────────────────────────────

data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
