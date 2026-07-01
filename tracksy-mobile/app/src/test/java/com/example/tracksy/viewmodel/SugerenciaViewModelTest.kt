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

class SugerenciaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: TracksyRepositoryInterface
    private lateinit var viewModel: SugerenciaViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = SugerenciaViewModel(repo)
    }

    // ── crearSugerencia ───────────────────────────────────────────────────────

    @Test
    fun `crearSugerencia no hace nada si estados esta vacio`() = runTest {
        viewModel.crearSugerencia(productoId = 123L, motivo = "Precio desactualizado")

        coVerify(exactly = 0) { repo.crearSugerencia(any(), any(), any()) }
    }

    @Test
    fun `crearSugerencia llama al repo con el primer estadoId disponible`() = runTest {
        val estado = Estado(id = 5, nombre = "Pendiente")
        coEvery { repo.getEstados() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(estado))
        )
        viewModel.cargarEstados()

        val sugerenciaCreada = sugerenciaFake()
        coEvery { repo.crearSugerencia(123L, 5, "Precio desactualizado") } returns Response.success(sugerenciaCreada)
        coEvery { repo.getSugerencias() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(sugerenciaCreada))
        )

        viewModel.crearSugerencia(productoId = 123L, motivo = "Precio desactualizado")

        coVerify(exactly = 1) { repo.crearSugerencia(123L, 5, "Precio desactualizado") }
    }

    // ── cargarSugerencias ─────────────────────────────────────────────────────

    @Test
    fun `cargarSugerencias setea error con codigo cuando la API devuelve error HTTP`() = runTest {
        coEvery { repo.getSugerencias() } returns Response.error(
            503, "Service unavailable".toResponseBody()
        )

        viewModel.cargarSugerencias()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("503"))
    }

    @Test
    fun `cargarSugerencias setea error Sin conexion cuando hay excepcion de red`() = runTest {
        coEvery { repo.getSugerencias() } throws RuntimeException("Connection refused")

        viewModel.cargarSugerencias()

        assertEquals("Sin conexión", viewModel.error.value)
    }

    @Test
    fun `cargarSugerencias limpia el error al iniciar una nueva carga`() = runTest {
        coEvery { repo.getSugerencias() } throws RuntimeException("Error")
        viewModel.cargarSugerencias()
        assertNotNull(viewModel.error.value)

        coEvery { repo.getSugerencias() } returns Response.success(
            PaginatedResponse(0, null, null, emptyList())
        )
        viewModel.cargarSugerencias()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `cargarSugerencias actualiza sugerencias cuando la respuesta es exitosa`() = runTest {
        val sugerencia = sugerenciaFake()
        coEvery { repo.getSugerencias() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(sugerencia))
        )

        viewModel.cargarSugerencias()

        assertEquals(1, viewModel.sugerencias.value.size)
        assertNull(viewModel.error.value)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun sugerenciaFake() = Sugerencia(
        id = 1,
        usuario = 1,
        producto = 123L,
        productoNombre = "Producto Test",
        fecha = "2024-01-01",
        estado = 1,
        estadoNombre = "Pendiente",
        motivo = "Precio desactualizado",
        feedbacks = emptyList()
    )
}
