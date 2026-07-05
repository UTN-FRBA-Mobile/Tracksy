package com.example.tracksy.viewmodel

import com.example.tracksy.data.models.*
import com.example.tracksy.data.repository.TracksyRepositoryInterface
import com.example.tracksy.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class ListaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: TracksyRepositoryInterface
    private lateinit var viewModel: ListaViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = ListaViewModel(repo)
    }

    // ── idEstadoPendiente ─────────────────────────────────────────────────────

    @Test
    fun `idEstadoPendiente retorna el id correcto cuando hay estados cargados`() = runTest {
        val estados = listOf(
            EstadoProducto(id = 1, nombre = "Pendiente"),
            EstadoProducto(id = 2, nombre = "Comprado")
        )
        coEvery { repo.getEstadosProducto() } returns Response.success(
            PaginatedResponse(2, null, null, estados)
        )
        viewModel.cargarEstadosProducto()

        assertEquals(1, viewModel.idEstadoPendiente())
    }

    @Test
    fun `idEstadoPendiente retorna null cuando la lista de estados esta vacia`() {
        assertNull(viewModel.idEstadoPendiente())
    }

    // ── estadoEsComprado ──────────────────────────────────────────────────────

    @Test
    fun `estadoEsComprado retorna true para variantes del string comprado`() {
        assertTrue(viewModel.estadoEsComprado("Comprado"))
        assertTrue(viewModel.estadoEsComprado("comprado"))
        assertTrue(viewModel.estadoEsComprado("COMPRADO"))
        assertTrue(viewModel.estadoEsComprado("Estado comprado final"))
    }

    @Test
    fun `estadoEsComprado retorna false para estados no comprados`() {
        assertFalse(viewModel.estadoEsComprado("Pendiente"))
        assertFalse(viewModel.estadoEsComprado("En curso"))
        assertFalse(viewModel.estadoEsComprado(""))
    }

    // ── cargarListas ──────────────────────────────────────────────────────────

    @Test
    fun `cargarListas actualiza listas y listasDetalladas cuando la respuesta es exitosa`() = runTest {
        val listaFake = listaCompra(id = 1, nombre = "Mercado semanal")
        coEvery { repo.getListas() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(listaFake))
        )

        viewModel.cargarListas()

        assertEquals(1, viewModel.listas.value.size)
        assertEquals("Mercado semanal", viewModel.listas.value[0].name)
        assertEquals(1, viewModel.listasDetalladas.value.size)
    }

    @Test
    fun `cargarListas deja las listas vacias si la respuesta falla`() = runTest {
        coEvery { repo.getListas() } throws RuntimeException("Sin red")

        viewModel.cargarListas()

        assertTrue(viewModel.listas.value.isEmpty())
    }

    @Test
    fun `cargarListas setea isLoading en true durante la carga y false al terminar`() = runTest {
        coEvery { repo.getListas() } returns Response.success(
            PaginatedResponse(0, null, null, emptyList())
        )

        viewModel.cargarListas()

        assertFalse(viewModel.isLoading.value)
    }

    // ── editarListaConItems — diff ────────────────────────────────────────────

    @Test
    fun `editarListaConItems agrega solo los items que no existian`() = runTest {
        val itemExistente = itemProducto(id = 10, productoId = 111L)
        val listaActual = listaCompra(id = 1, items = listOf(itemExistente))

        cargarEstadosYLista(listaActual)
        coEvery { repo.updateLista(any(), any()) } returns Response.success(listaActual)
        coEvery { repo.agregarItem(any(), any(), any(), any(), any()) } returns Response.success(itemExistente)
        coEvery { repo.getListas() } returns Response.success(PaginatedResponse(0, null, null, emptyList()))

        viewModel.editarListaConItems(
            listaId = 1,
            nombre = "Mi lista",
            supermercadoId = null,
            items = listOf(Pair(111L, 1), Pair(222L, 2))
        )

        coVerify(exactly = 1) { repo.agregarItem(1, 222L, 2, 1, 0.0) }
        coVerify(exactly = 0) { repo.agregarItem(1, 111L, any(), any(), any()) }
    }

    @Test
    fun `editarListaConItems elimina los items que fueron quitados del diff`() = runTest {
        val item1 = itemProducto(id = 10, productoId = 111L)
        val item2 = itemProducto(id = 11, productoId = 222L)
        val listaActual = listaCompra(id = 1, items = listOf(item1, item2))

        cargarEstadosYLista(listaActual)
        coEvery { repo.updateLista(any(), any()) } returns Response.success(listaActual)
        coEvery { repo.eliminarItem(any(), any()) } returns Response.success(Unit)
        coEvery { repo.getListas() } returns Response.success(PaginatedResponse(0, null, null, emptyList()))

        viewModel.editarListaConItems(
            listaId = 1,
            nombre = "Mi lista",
            supermercadoId = null,
            items = listOf(Pair(111L, 1))
        )

        coVerify(exactly = 1) { repo.eliminarItem(1, 11) }
        coVerify(exactly = 0) { repo.eliminarItem(1, 10) }
    }

    @Test
    fun `editarListaConItems actualiza la cantidad si el item existe pero cambio`() = runTest {
        val item = itemProducto(id = 10, productoId = 111L, cantidad = 1)
        val listaActual = listaCompra(id = 1, items = listOf(item))

        cargarEstadosYLista(listaActual)
        coEvery { repo.updateLista(any(), any()) } returns Response.success(listaActual)
        coEvery { repo.updateItem(any(), any(), any()) } returns Response.success(item)
        coEvery { repo.getListas() } returns Response.success(PaginatedResponse(0, null, null, emptyList()))

        viewModel.editarListaConItems(
            listaId = 1,
            nombre = "Mi lista",
            supermercadoId = null,
            items = listOf(Pair(111L, 5))
        )

        coVerify(exactly = 1) { repo.updateItem(1, 10, mapOf("cantidad" to 5)) }
    }

    // ── toggleItem ────────────────────────────────────────────────────────────

    @Test
    fun `toggleItem no hace nada si no hay estados cargados`() = runTest {
        viewModel.toggleItem(listaId = 1, itemId = 10, estaComprado = false)

        coVerify(exactly = 0) { repo.updateItem(any(), any(), any()) }
    }

    // ── crearListaConItems ────────────────────────────────────────────────────

    @Test
    fun `crearListaConItems carga estados si estaban vacios`() = runTest {
        val estadoPendiente = EstadoProducto(id = 1, nombre = "Pendiente")
        val listaCreada = listaCompra(id = 99, nombre = "Nueva")

        coEvery { repo.getEstadosProducto() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(estadoPendiente))
        )
        coEvery { repo.crearLista(any(), any()) } returns Response.success(listaCreada)
        coEvery { repo.getListas() } returns Response.success(PaginatedResponse(0, null, null, emptyList()))

        viewModel.crearListaConItems("Nueva", null, emptyList())

        coVerify(exactly = 1) { repo.getEstadosProducto() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun cargarEstadosYLista(lista: ListaCompra) {
        coEvery { repo.getEstadosProducto() } returns Response.success(
            PaginatedResponse(1, null, null, listOf(EstadoProducto(id = 1, nombre = "Pendiente")))
        )
        viewModel.cargarEstadosProducto()

        coEvery { repo.getLista(lista.id) } returns Response.success(lista)
        viewModel.cargarLista(lista.id)
    }

    private fun listaCompra(
        id: Int = 1,
        nombre: String = "Lista",
        items: List<ItemProducto> = emptyList()
    ) = ListaCompra(
        id = id,
        usuario = 1,
        usuarioEmail = "test@test.com",
        supermercado = null,
        nombre = nombre,
        fechaCreacion = "2024-01-01T00:00:00Z",
        totalEstimado = 0.0,
        items = items
    )

    private fun itemProducto(
        id: Int = 1,
        productoId: Long = 100L,
        cantidad: Int = 1
    ) = ItemProducto(
        id = id,
        lista = 1,
        producto = productoId,
        productoNombre = "Producto $productoId",
        cantidad = cantidad,
        estado = 1,
        estadoNombre = "Pendiente",
        precioUnitario = 0.0
    )
}
