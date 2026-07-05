package com.example.tracksy.data.repository

import com.example.tracksy.data.api.ApiService
import com.example.tracksy.data.api.RetrofitClient
import com.example.tracksy.data.models.*

class TracksyRepository(
    private val api: ApiService = RetrofitClient.apiService
) : TracksyRepositoryInterface {
    // ── Auth ──────────────────────────────────────────────────────────────────

    override suspend fun firebaseSync() =
        api.firebaseSync()

    // ── Usuarios ──────────────────────────────────────────────────────────────

    override suspend fun getPerfil() = api.getPerfil()

    override suspend fun updatePerfil(nombre: String) =
        api.updatePerfil(mapOf("nombre" to nombre))

    override suspend fun getFavoritos() = api.getFavoritos()

    override suspend fun addFavorito(productoId: Long) =
        api.addFavorito(AddFavoritoRequest(producto = productoId, favorito = true))

    override suspend fun removeFavorito(id: Int) =
        api.removeFavorito(id)

    // ── Productos ─────────────────────────────────────────────────────────────

    override suspend fun getProductos(search: String?, page: Int?, pageSize: Int?) =
        api.getProductos(search, page, pageSize)

    override suspend fun getProducto(id: Long) = api.getProducto(id)

    override suspend fun getMarcas() = api.getMarcas()

    // ── Supermercados ─────────────────────────────────────────────────────────

    override suspend fun getSupermercados(search: String?) =
        api.getSupermercados(search)

    override suspend fun getSupermercado(id: Int) = api.getSupermercado(id)

    override suspend fun getListados(
        supermercadoId: Int?,
        productoId: Long?,
        disponible: Boolean?,
        page: Int?,
        pageSize: Int?
    ) = api.getListados(supermercadoId, productoId, disponible, page, pageSize)

    // ── Listas de compra ──────────────────────────────────────────────────────

    override suspend fun getListas() = api.getListas()

    override suspend fun getLista(id: Int) = api.getLista(id)

    override suspend fun crearLista(nombre: String, supermercadoId: Int?) =
        api.crearLista(ListaCompraRequest(nombre, supermercadoId))

    override suspend fun updateLista(id: Int, campos: Map<String, Any?>) =
        api.updateLista(id, campos)

    override suspend fun deleteLista(id: Int) = api.deleteLista(id)

    override suspend fun agregarItem(
        listaId: Int,
        productoId: Long,
        cantidad: Int,
        estadoId: Int,
        precioUnitario: Double
    ) = api.agregarItem(
        listaId,
        ItemProductoRequest(productoId, cantidad, estadoId, precioUnitario)
    )

    override suspend fun updateItem(listaId: Int, itemId: Int, campos: Map<String, Any>) =
        api.updateItem(listaId, itemId, campos)

    override suspend fun eliminarItem(listaId: Int, itemId: Int) =
        api.eliminarItem(listaId, itemId)

    override suspend fun getEstadosProducto() = api.getEstadosProducto()

    // ── Compras ───────────────────────────────────────────────────────────────

    override suspend fun getCompras() = api.getCompras()

    override suspend fun getCompra(id: Int) = api.getCompra(id)

    override suspend fun crearCompra(request: CompraRequest) =
        api.crearCompra(request)

    // ── Sugerencias ───────────────────────────────────────────────────────────

    override suspend fun getSugerencias() = api.getSugerencias()

    override suspend fun crearSugerencia(productoId: Long, estadoId: Int, motivo: String) =
        api.crearSugerencia(SugerenciaRequest(productoId, estadoId, motivo))

    override suspend fun agregarFeedback(sugerenciaId: Int, fueUtil: Boolean) =
        api.agregarFeedback(sugerenciaId, FeedbackRequest(fueUtil))

    override suspend fun getEstados() = api.getEstados()
}
