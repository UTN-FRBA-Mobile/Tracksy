package com.example.tracksy.ui.lists

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.tracksy.data.models.ItemProducto
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.ui.theme.TracksyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetalleListaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test 5: click en item pendiente llama a onToggleItem ─────────────────

    @Test
    fun click_en_item_pendiente_llama_a_onToggleItem() {
        var toggleLlamado = false
        var listaIdRecibido = -1
        var itemIdRecibido = -1

        val item = itemProducto(id = 42, productoNombre = "Arroz Gallo", estadoNombre = "Pendiente")
        val lista = listaCompra(id = 7, items = listOf(item))

        composeTestRule.setContent {
            TracksyTheme {
                DetalleListaScreen(
                    lista = lista,
                    onToggleItem = { listaId, itemId, _ ->
                        toggleLlamado = true
                        listaIdRecibido = listaId
                        itemIdRecibido = itemId
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Arroz Gallo").performClick()

        assertTrue(toggleLlamado)
        assertEquals(7, listaIdRecibido)
        assertEquals(42, itemIdRecibido)
    }

    @Test
    fun lista_vacia_muestra_texto_de_lista_vacia() {
        val lista = listaCompra(id = 1, items = emptyList())

        composeTestRule.setContent {
            TracksyTheme {
                DetalleListaScreen(lista = lista)
            }
        }

        composeTestRule.onNodeWithText("La lista está vacía").assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun listaCompra(id: Int, items: List<ItemProducto>) = ListaCompra(
        id = id,
        usuario = 1,
        usuarioEmail = "test@test.com",
        supermercado = null,
        nombre = "Lista de prueba",
        fechaCreacion = "2024-01-01T00:00:00Z",
        totalEstimado = 0.0,
        items = items
    )

    private fun itemProducto(
        id: Int,
        productoNombre: String,
        estadoNombre: String
    ) = ItemProducto(
        id = id,
        lista = 1,
        producto = 999L,
        productoNombre = productoNombre,
        cantidad = 1,
        estado = 1,
        estadoNombre = estadoNombre,
        precioUnitario = 100.0
    )
}
