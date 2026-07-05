package com.example.tracksy.data.repository

import com.example.tracksy.data.models.*
import retrofit2.Response

interface TracksyRepositoryInterface {

    suspend fun firebaseSync(): Response<FirebaseSyncResponse>

    suspend fun getPerfil(): Response<Usuario>
    suspend fun updatePerfil(nombre: String): Response<Usuario>
    suspend fun getFavoritos(): Response<PaginatedResponse<ProductoUsuario>>
    suspend fun addFavorito(productoId: Long): Response<ProductoUsuario>
    suspend fun removeFavorito(id: Int): Response<Unit>

    suspend fun getProductos(
        search: String? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): Response<PaginatedResponse<Producto>>
    suspend fun getProducto(id: Long): Response<ProductoDetalle>
    suspend fun getMarcas(): Response<PaginatedResponse<Marca>>

    suspend fun getSupermercados(search: String? = null): Response<PaginatedResponse<Supermercado>>
    suspend fun getSupermercado(id: Int): Response<Supermercado>
    suspend fun getListados(
        supermercadoId: Int? = null,
        productoId: Long? = null,
        disponible: Boolean? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): Response<PaginatedResponse<ProductoListado>>

    suspend fun getListas(): Response<PaginatedResponse<ListaCompra>>
    suspend fun getLista(id: Int): Response<ListaCompra>
    suspend fun crearLista(nombre: String, supermercadoId: Int? = null): Response<ListaCompra>
    suspend fun updateLista(id: Int, campos: Map<String, Any?>): Response<ListaCompra>
    suspend fun deleteLista(id: Int): Response<Unit>
    suspend fun agregarItem(
        listaId: Int,
        productoId: Long,
        cantidad: Int,
        estadoId: Int,
        precioUnitario: Double
    ): Response<ItemProducto>
    suspend fun updateItem(listaId: Int, itemId: Int, campos: Map<String, Any>): Response<ItemProducto>
    suspend fun eliminarItem(listaId: Int, itemId: Int): Response<Unit>
    suspend fun getEstadosProducto(): Response<PaginatedResponse<EstadoProducto>>

    suspend fun getCompras(): Response<PaginatedResponse<Compra>>
    suspend fun getCompra(id: Int): Response<Compra>
    suspend fun crearCompra(request: CompraRequest): Response<Compra>

    suspend fun getSugerencias(): Response<PaginatedResponse<Sugerencia>>
    suspend fun crearSugerencia(productoId: Long, estadoId: Int, motivo: String): Response<Sugerencia>
    suspend fun agregarFeedback(sugerenciaId: Int, fueUtil: Boolean): Response<FeedbackSugerencia>
    suspend fun getEstados(): Response<PaginatedResponse<Estado>>
}
