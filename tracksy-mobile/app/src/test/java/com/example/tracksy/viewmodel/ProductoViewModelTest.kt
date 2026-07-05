package com.example.tracksy.viewmodel

import com.example.tracksy.data.models.*
import com.example.tracksy.data.repository.TracksyRepositoryInterface
import com.example.tracksy.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class ProductoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: TracksyRepositoryInterface
    private lateinit var viewModel: ProductoViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = ProductoViewModel(repo)
    }

    // ── buscarProductoPorBarcode ───────────────────────────────────────────────

    @Test
    fun `buscarProductoPorBarcode setea productoNoEncontrado si el string no es numerico`() {
        viewModel.buscarProductoPorBarcode("abc123")

        assertTrue(viewModel.productoNoEncontrado.value)
        assertNull(viewModel.productoEscaneado.value)
    }

    @Test
    fun `buscarProductoPorBarcode setea productoEscaneado cuando la API responde OK`() = runTest {
        val detalle = ProductoDetalle(id = 7790123L, nombre = "Coca Cola", marca = null)
        coEvery { repo.getProducto(7790123L) } returns Response.success(detalle)

        viewModel.buscarProductoPorBarcode("7790123")

        assertNotNull(viewModel.productoEscaneado.value)
        assertEquals("Coca Cola", viewModel.productoEscaneado.value?.name)
        assertFalse(viewModel.productoNoEncontrado.value)
    }

    @Test
    fun `buscarProductoPorBarcode setea productoNoEncontrado si la API devuelve 404`() = runTest {
        coEvery { repo.getProducto(any()) } returns Response.error(
            404, "Not found".toResponseBody()
        )

        viewModel.buscarProductoPorBarcode("9999999999999")

        assertTrue(viewModel.productoNoEncontrado.value)
        assertNull(viewModel.productoEscaneado.value)
    }

    @Test
    fun `buscarProductoPorBarcode setea productoNoEncontrado si la API lanza excepcion`() = runTest {
        coEvery { repo.getProducto(any()) } throws RuntimeException("Sin red")

        viewModel.buscarProductoPorBarcode("1234567890123")

        assertTrue(viewModel.productoNoEncontrado.value)
    }

    // ── toggleFavorito — optimistic update ────────────────────────────────────

    @Test
    fun `toggleFavorito agrega el producto a favoritos y persiste el estado final`() = runTest {
        val productoModel = productoApi(id = 123L, nombre = "Leche La Serenísima")
        coEvery { repo.getProductos(any(), any(), any()) } returns Response.success(
            PaginatedResponse(1, null, null, listOf(productoModel))
        )
        viewModel.cargarProductos()

        val favoritoModel = ProductoUsuario(id = 1, usuario = 1, producto = 123L, favorito = true)
        coEvery { repo.addFavorito(123L) } returns Response.success(favoritoModel)
        // cargarFavoritos() en el finally confirma que el producto sigue como favorito
        coEvery { repo.getFavoritos() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(favoritoModel))
        )
        coEvery { repo.getProducto(123L) } returns Response.success(
            ProductoDetalle(id = 123L, nombre = "Leche La Serenísima", marca = null)
        )

        viewModel.toggleFavorito(123L, esFavorito = true)

        assertTrue(viewModel.favoritos.value.any { it.id == 123L })
    }

    @Test
    fun `toggleFavorito remueve el producto de favoritos y persiste el estado final`() = runTest {
        val productoModel = productoApi(id = 456L, nombre = "Yogur Danone")
        coEvery { repo.getProductos(any(), any(), any()) } returns Response.success(
            PaginatedResponse(1, null, null, listOf(productoModel))
        )
        viewModel.cargarProductos()

        val favoritoModel = ProductoUsuario(id = 2, usuario = 1, producto = 456L, favorito = true)

        // Primero agregamos el favorito
        coEvery { repo.addFavorito(456L) } returns Response.success(favoritoModel)
        coEvery { repo.getFavoritos() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(favoritoModel))
        )
        coEvery { repo.getProducto(456L) } returns Response.success(
            ProductoDetalle(id = 456L, nombre = "Yogur Danone", marca = null)
        )
        viewModel.toggleFavorito(456L, esFavorito = true)

        // Ahora removemos: primera llamada a getFavoritos obtiene el favId, segunda (en cargarFavoritos) confirma que ya no existe
        coEvery { repo.getFavoritos() } returnsMany listOf(
            Response.success(PaginatedResponse(1, null, null, listOf(favoritoModel))),
            Response.success(PaginatedResponse(0, null, null, emptyList()))
        )
        coEvery { repo.removeFavorito(2) } returns Response.success(Unit)

        viewModel.toggleFavorito(456L, esFavorito = false)

        assertFalse(viewModel.favoritos.value.any { it.id == 456L })
    }

    @Test
    fun `toggleFavorito revierte el estado si la API falla con excepcion`() = runTest {
        val productoModel = productoApi(id = 789L, nombre = "Pan Bimbo")
        coEvery { repo.getProductos(any(), any(), any()) } returns Response.success(
            PaginatedResponse(1, null, null, listOf(productoModel))
        )
        viewModel.cargarProductos()

        coEvery { repo.addFavorito(789L) } throws RuntimeException("Sin red")
        // cargarFavoritos en el finally confirma que no quedó en favoritos
        coEvery { repo.getFavoritos() } returns Response.success(
            PaginatedResponse(0, null, null, emptyList())
        )

        viewModel.toggleFavorito(789L, esFavorito = true)

        assertFalse(viewModel.favoritos.value.any { it.id == 789L })
    }

    // ── buscarProductosParaLista ──────────────────────────────────────────────

    @Test
    fun `buscarProductosParaLista con query vacia limpia productosBusqueda sin llamar a la API`() {
        viewModel.buscarProductosParaLista("")

        assertTrue(viewModel.productosBusqueda.value.isEmpty())
        coVerify(exactly = 0) { repo.getProductos(any(), any(), any()) }
    }

    // ── cargarProductos ───────────────────────────────────────────────────────

    @Test
    fun `cargarProductos setea isLoading en false al terminar independientemente del resultado`() = runTest {
        coEvery { repo.getProductos(any(), any(), any()) } throws RuntimeException("Sin red")

        viewModel.cargarProductos()

        assertFalse(viewModel.isLoading.value)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun productoApi(id: Long, nombre: String) =
        Producto(id = id, nombre = nombre, marca = null, marcaNombre = null)
}
