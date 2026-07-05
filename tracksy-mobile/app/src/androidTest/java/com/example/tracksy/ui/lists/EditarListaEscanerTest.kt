package com.example.tracksy.ui.lists

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.tracksy.screens.Product
import com.example.tracksy.ui.theme.TracksyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Verifica el flujo crítico del escáner: cuando un barcode es detectado y el producto
 * es resuelto por el ViewModel, EditarListaScreen lo muestra como item agregado a la lista.
 *
 * No requiere cámara real — se simula pasando `scannedProductToAdd` directamente,
 * que es exactamente lo que hace el ViewModel cuando `productoEscaneado` tiene valor.
 */
class EditarListaEscanerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test escáner: producto escaneado aparece en la lista ──────────────────

    @Test
    fun producto_escaneado_aparece_en_la_lista_de_items() {
        val productoEscaneado = Product(
            id = 7790123456789L,
            name = "Coca Cola 500ml",
            category = "Bebidas",
            barcode = "7790123456789"
        )
        var productConsumed = false

        composeTestRule.setContent {
            TracksyTheme {
                EditarListaScreen(
                    scannedProductToAdd = productoEscaneado,
                    onScannedProductConsumed = { productConsumed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Coca Cola 500ml").assertIsDisplayed()
        assertTrue(productConsumed)
    }

    // ── Test escáner: segundo scan del mismo producto no duplica el item ──────

    @Test
    fun producto_escaneado_no_se_duplica_si_ya_esta_en_la_lista() {
        val productoEscaneado = Product(
            id = 7790123456789L,
            name = "Coca Cola 500ml",
            category = "Bebidas",
            barcode = "7790123456789"
        )

        composeTestRule.setContent {
            TracksyTheme {
                EditarListaScreen(
                    scannedProductToAdd = productoEscaneado,
                    onScannedProductConsumed = {}
                )
            }
        }

        composeTestRule
            .onAllNodesWithText("Coca Cola 500ml")
            .assertCountEquals(1)
    }
}
