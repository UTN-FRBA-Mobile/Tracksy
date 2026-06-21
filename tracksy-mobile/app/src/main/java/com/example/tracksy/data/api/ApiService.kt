package com.example.tracksy.data.api

import com.example.tracksy.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/v1/auth/registro/")
    suspend fun registro(@Body body: RegistroRequest): Response<RegistroResponse>

    @POST("api/v1/auth/login/")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("api/v1/auth/refresh/")
    suspend fun refreshToken(@Body body: RefreshRequest): Response<TokenResponse>

    @POST("api/v1/auth/firebase/sync/")
    suspend fun firebaseSync(@Header("Authorization") token: String): Response<FirebaseSyncResponse>

    // ── Usuarios ──────────────────────────────────────────────────────────────

    @GET("api/v1/usuarios/perfil/")
    suspend fun getPerfil(@Header("Authorization") token: String): Response<Usuario>

    @PATCH("api/v1/usuarios/perfil/")
    suspend fun updatePerfil(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<Usuario>

    @POST("api/v1/usuarios/cambiar-password/")
    suspend fun cambiarPassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @GET("api/v1/usuarios/favoritos/")
    suspend fun getFavoritos(@Header("Authorization") token: String): Response<PaginatedResponse<ProductoUsuario>>

    @POST("api/v1/usuarios/favoritos/")
    suspend fun addFavorito(
        @Header("Authorization") token: String,
        @Body body: AddFavoritoRequest          // data class en lugar de Map<String, Any>
    ): Response<ProductoUsuario>

    @DELETE("api/v1/usuarios/favoritos/{id}/")
    suspend fun removeFavorito(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // ── Productos ─────────────────────────────────────────────────────────────

    @GET("api/v1/productos/")
    suspend fun getProductos(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PaginatedResponse<Producto>>

    @GET("api/v1/productos/{id}/")
    suspend fun getProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ProductoDetalle>

    @GET("api/v1/productos/marcas/")
    suspend fun getMarcas(@Header("Authorization") token: String): Response<PaginatedResponse<Marca>>

    // ── Supermercados ─────────────────────────────────────────────────────────

    @GET("api/v1/supermercados/")
    suspend fun getSupermercados(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<Supermercado>>

    @GET("api/v1/supermercados/{id}/")
    suspend fun getSupermercado(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Supermercado>

    @GET("api/v1/supermercados/listados/")
    suspend fun getListados(
        @Header("Authorization") token: String,
        @Query("supermercado") supermercadoId: Int? = null,
        @Query("producto") productoId: Long? = null,
        @Query("disponible") disponible: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PaginatedResponse<ProductoListado>>

    // ── Listas de compra ──────────────────────────────────────────────────────

    @GET("api/v1/listas/")
    suspend fun getListas(@Header("Authorization") token: String): Response<PaginatedResponse<ListaCompra>>

    @GET("api/v1/listas/{id}/")
    suspend fun getLista(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ListaCompra>

    @POST("api/v1/listas/")
    suspend fun crearLista(
        @Header("Authorization") token: String,
        @Body body: ListaCompraRequest
    ): Response<ListaCompra>

    @PATCH("api/v1/listas/{id}/")
    suspend fun updateLista(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        // @JvmSuppressWildcards: evita Map<String, ? extends Any?> en bytecode JVM →
        // Gson puede serializar correctamente el cuerpo con GsonConverterFactory.
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ListaCompra>

    @DELETE("api/v1/listas/{id}/")
    suspend fun deleteLista(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @POST("api/v1/listas/{id}/items/")
    suspend fun agregarItem(
        @Header("Authorization") token: String,
        @Path("id") listaId: Int,
        @Body body: ItemProductoRequest
    ): Response<ItemProducto>

    @PATCH("api/v1/listas/{id}/items/{itemId}/")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") listaId: Int,
        @Path("itemId") itemId: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<ItemProducto>

    @DELETE("api/v1/listas/{id}/items/{itemId}/delete/")
    suspend fun eliminarItem(
        @Header("Authorization") token: String,
        @Path("id") listaId: Int,
        @Path("itemId") itemId: Int
    ): Response<Unit>

    @GET("api/v1/listas/estados-producto/")
    suspend fun getEstadosProducto(@Header("Authorization") token: String): Response<PaginatedResponse<EstadoProducto>>

    // ── Compras ───────────────────────────────────────────────────────────────

    @GET("api/v1/compras/")
    suspend fun getCompras(@Header("Authorization") token: String): Response<PaginatedResponse<Compra>>

    @GET("api/v1/compras/{id}/")
    suspend fun getCompra(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Compra>

    @POST("api/v1/compras/")
    suspend fun crearCompra(
        @Header("Authorization") token: String,
        @Body body: CompraRequest
    ): Response<Compra>

    // ── Sugerencias ───────────────────────────────────────────────────────────

    @GET("api/v1/sugerencias/")
    suspend fun getSugerencias(@Header("Authorization") token: String): Response<PaginatedResponse<Sugerencia>>

    @POST("api/v1/sugerencias/")
    suspend fun crearSugerencia(
        @Header("Authorization") token: String,
        @Body body: SugerenciaRequest
    ): Response<Sugerencia>

    @POST("api/v1/sugerencias/{id}/feedback/")
    suspend fun agregarFeedback(
        @Header("Authorization") token: String,
        @Path("id") sugerenciaId: Int,
        @Body body: FeedbackRequest
    ): Response<FeedbackSugerencia>

    @GET("api/v1/sugerencias/estados/")
    suspend fun getEstados(@Header("Authorization") token: String): Response<PaginatedResponse<Estado>>
}
