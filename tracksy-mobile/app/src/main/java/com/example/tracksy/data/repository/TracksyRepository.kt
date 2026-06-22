package com.example.tracksy.data.repository

import com.example.tracksy.data.api.ApiService
import com.example.tracksy.data.api.RetrofitClient
import com.example.tracksy.data.models.*

class TracksyRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun firebaseSync() =
        api.firebaseSync()

    // ── Usuarios ──────────────────────────────────────────────────────────────

    suspend fun getPerfil() = api.getPerfil()

    suspend fun updatePerfil(nombre: String) =
        api.updatePerfil(mapOf("nombre" to nombre))

    suspend fun cambiarPassword(passwordActual: String, passwordNuevo: String) =
        api.cambiarPassword(mapOf("password_actual" to passwordActual, "password_nuevo" to passwordNuevo))

    suspend fun getFavoritos() = api.getFavoritos()

    suspend fun addFavorito(productoId: Long) =
        api.addFavorito(AddFavoritoRequest(producto = productoId, favorito = true))

    suspend fun removeFavorito(id: Int) =
        api.removeFavorito(id)

    // ── Productos ─────────────────────────────────────────────────────────────

    suspend fun getProductos(search: String? = null, page: Int? = null, pageSize: Int? = null) =
        api.getProductos(search, page, pageSize)

    suspend fun getProducto(id: Long) = api.getProducto(id)

    suspend fun getMarcas() = api.getMarcas()

    // ── Supermercados ─────────────────────────────────────────────────────────

    suspend fun getSupermercados(search: String? = null) =
        api.getSupermercados(search)

    suspend fun getSupermercado(id: Int) = api.getSupermercado(id)

    suspend fun getListados(
        supermercadoId: Int? = null,
        productoId: Long? = null,
        disponible: Boolean? = null,
        page: Int? = null,
        pageSize: Int? = null
    ) = api.getListados(supermercadoId, productoId, disponible, page, pageSize)

    // ── Listas de compra ──────────────────────────────────────────────────────

    suspend fun getListas() = api.getListas()

    suspend fun getLista(id: Int) = api.getLista(id)

    suspend fun crearLista(nombre: String, supermercadoId: Int? = null) =
        api.crearLista(ListaCompraRequest(nombre, supermercadoId))

    suspend fun updateLista(id: Int, campos: Map<String, Any?>) =
        api.updateLista(id, campos)

    suspend fun deleteLista(id: Int) = api.deleteLista(id)

    suspend fun agregarItem(
        listaId: Int,
        productoId: Long,
        cantidad: Int,
        estadoId: Int,
        precioUnitario: Double
    ) = api.agregarItem(
        listaId,
        ItemProductoRequest(productoId, cantidad, estadoId, precioUnitario)
    )

    suspend fun updateItem(listaId: Int, itemId: Int, campos: Map<String, Any>) =
        api.updateItem(listaId, itemId, campos)

    suspend fun eliminarItem(listaId: Int, itemId: Int) =
        api.eliminarItem(listaId, itemId)

    suspend fun getEstadosProducto() = api.getEstadosProducto()

    // ── Compras ───────────────────────────────────────────────────────────────

    suspend fun getCompras() = api.getCompras()

    suspend fun getCompra(id: Int) = api.getCompra(id)

    suspend fun crearCompra(request: CompraRequest) =
        api.crearCompra(request)

    // ── Sugerencias ───────────────────────────────────────────────────────────

    suspend fun getSugerencias() = api.getSugerencias()

    suspend fun crearSugerencia(productoId: Long, estadoId: Int, motivo: String) =
        api.crearSugerencia(SugerenciaRequest(productoId, estadoId, motivo))

    suspend fun agregarFeedback(sugerenciaId: Int, fueUtil: Boolean) =
        api.agregarFeedback(sugerenciaId, FeedbackRequest(fueUtil))

    suspend fun getEstados() = api.getEstados()
}
