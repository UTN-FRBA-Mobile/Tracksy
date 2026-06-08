package com.example.tracksy.data.repository

import com.example.tracksy.data.api.ApiService
import com.example.tracksy.data.api.RetrofitClient
import com.example.tracksy.data.models.*

class TracksyRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    private fun bearer(token: String) = RetrofitClient.bearerToken(token)

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun registro(nombre: String, email: String, password: String) =
        api.registro(RegistroRequest(nombre, email, password))

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))

    suspend fun refreshToken(refresh: String) =
        api.refreshToken(RefreshRequest(refresh))

    // ── Usuarios ──────────────────────────────────────────────────────────────

    suspend fun getPerfil(token: String) = api.getPerfil(bearer(token))

    suspend fun updatePerfil(token: String, nombre: String) =
        api.updatePerfil(bearer(token), mapOf("nombre" to nombre))

    suspend fun cambiarPassword(token: String, passwordActual: String, passwordNuevo: String) =
        api.cambiarPassword(bearer(token), mapOf("password_actual" to passwordActual, "password_nuevo" to passwordNuevo))

    suspend fun getFavoritos(token: String) = api.getFavoritos(bearer(token))

    suspend fun addFavorito(token: String, productoId: Long) =
        api.addFavorito(bearer(token), AddFavoritoRequest(producto = productoId, favorito = true))

    suspend fun removeFavorito(token: String, id: Int) =
        api.removeFavorito(bearer(token), id)

    // ── Productos ─────────────────────────────────────────────────────────────

    suspend fun getProductos(token: String, search: String? = null, page: Int? = null) =
        api.getProductos(bearer(token), search, page)

    suspend fun getProducto(token: String, id: Long) = api.getProducto(bearer(token), id)

    suspend fun getMarcas(token: String) = api.getMarcas(bearer(token))

    // ── Supermercados ─────────────────────────────────────────────────────────

    suspend fun getSupermercados(token: String, search: String? = null) =
        api.getSupermercados(bearer(token), search)

    suspend fun getSupermercado(token: String, id: Int) = api.getSupermercado(bearer(token), id)

    suspend fun getListados(
        token: String,
        supermercadoId: Int? = null,
        productoId: Long? = null,
        disponible: Boolean? = null
    ) = api.getListados(bearer(token), supermercadoId, productoId, disponible)

    // ── Listas de compra ──────────────────────────────────────────────────────

    suspend fun getListas(token: String) = api.getListas(bearer(token))

    suspend fun getLista(token: String, id: Int) = api.getLista(bearer(token), id)

    suspend fun crearLista(token: String, nombre: String, supermercadoId: Int? = null) =
        api.crearLista(bearer(token), ListaCompraRequest(nombre, supermercadoId))

    suspend fun updateLista(token: String, id: Int, campos: Map<String, Any?>) =
        api.updateLista(bearer(token), id, campos)

    suspend fun deleteLista(token: String, id: Int) = api.deleteLista(bearer(token), id)

    suspend fun agregarItem(
        token: String,
        listaId: Int,
        productoId: Long,
        cantidad: Int,
        estadoId: Int,
        precioUnitario: Double
    ) = api.agregarItem(
        bearer(token), listaId,
        ItemProductoRequest(productoId, cantidad, estadoId, precioUnitario)
    )

    suspend fun updateItem(token: String, listaId: Int, itemId: Int, campos: Map<String, Any>) =
        api.updateItem(bearer(token), listaId, itemId, campos)

    suspend fun eliminarItem(token: String, listaId: Int, itemId: Int) =
        api.eliminarItem(bearer(token), listaId, itemId)

    suspend fun getEstadosProducto(token: String) = api.getEstadosProducto(bearer(token))

    // ── Compras ───────────────────────────────────────────────────────────────

    suspend fun getCompras(token: String) = api.getCompras(bearer(token))

    suspend fun getCompra(token: String, id: Int) = api.getCompra(bearer(token), id)

    suspend fun crearCompra(token: String, request: CompraRequest) =
        api.crearCompra(bearer(token), request)

    // ── Sugerencias ───────────────────────────────────────────────────────────

    suspend fun getSugerencias(token: String) = api.getSugerencias(bearer(token))

    suspend fun crearSugerencia(token: String, productoId: Long, estadoId: Int, motivo: String) =
        api.crearSugerencia(bearer(token), SugerenciaRequest(productoId, estadoId, motivo))

    suspend fun agregarFeedback(token: String, sugerenciaId: Int, fueUtil: Boolean) =
        api.agregarFeedback(bearer(token), sugerenciaId, FeedbackRequest(fueUtil))

    suspend fun getEstados(token: String) = api.getEstados(bearer(token))
}
