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

class CompraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: TracksyRepositoryInterface
    private lateinit var viewModel: CompraViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = CompraViewModel(repo)
    }

    // ── toHistoryItem (formato de fecha) ──────────────────────────────────────

    @Test
    fun `cargarCompras formatea la fecha correctamente`() = runTest {
        val compra = compraFake(fecha = "2024-03-15T10:00:00Z", nombreLista = "Supermercado")
        coEvery { repo.getCompras() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(compra))
        )

        viewModel.cargarCompras()

        val historyItem = viewModel.compras.value.first()
        assertEquals("15 de marzo", historyItem.dateLabel)
    }

    @Test
    fun `cargarCompras usa Compra como nombre cuando nombreLista esta en blanco`() = runTest {
        val compra = compraFake(nombreLista = "")
        coEvery { repo.getCompras() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(compra))
        )

        viewModel.cargarCompras()

        assertEquals("Compra", viewModel.compras.value.first().listName)
    }

    @Test
    fun `cargarCompras no crashea si fecha tiene formato inesperado`() = runTest {
        val compra = compraFake(fecha = "fecha-invalida")
        coEvery { repo.getCompras() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(compra))
        )

        viewModel.cargarCompras()

        assertEquals("fecha-invalida", viewModel.compras.value.first().dateLabel)
    }

    // ── crearCompra ───────────────────────────────────────────────────────────

    @Test
    fun `crearCompra llama a cargarCompras si la respuesta es exitosa`() = runTest {
        val compraCreada = compraFake()
        coEvery { repo.crearCompra(any()) } returns Response.success(compraCreada)
        coEvery { repo.getCompras() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(compraCreada))
        )

        viewModel.crearCompra(
            supermercadoId = null,
            nombreLista = "Test",
            total = 1500.0,
            productos = listOf(Triple(123L, 2, 750.0))
        )

        coVerify(exactly = 1) { repo.getCompras() }
    }

    @Test
    fun `crearCompra no crashea si la API falla con excepcion`() = runTest {
        coEvery { repo.crearCompra(any()) } throws RuntimeException("Sin red")

        viewModel.crearCompra(null, "Test", 0.0, emptyList())

        assertTrue(viewModel.compras.value.isEmpty())
    }

    @Test
    fun `crearCompra no llama a cargarCompras si la API devuelve error`() = runTest {
        coEvery { repo.crearCompra(any()) } returns Response.error(
            500, "Server error".toResponseBody()
        )

        viewModel.crearCompra(null, "Test", 0.0, emptyList())

        coVerify(exactly = 0) { repo.getCompras() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun compraFake(
        id: Int = 1,
        nombreLista: String = "Mercado",
        fecha: String = "2024-06-01T12:00:00Z"
    ) = Compra(
        id = id,
        usuario = 1,
        supermercado = null,
        supermercadoNombre = null,
        nombreLista = nombreLista,
        fecha = fecha,
        total = 1000.0,
        productos = emptyList()
    )
}
