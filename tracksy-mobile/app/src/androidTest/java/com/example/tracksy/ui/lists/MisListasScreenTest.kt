package com.example.tracksy.ui.lists

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.ShoppingList
import com.example.tracksy.ui.theme.TracksyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MisListasScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test 4a: lista vacía muestra empty state ───────────────────────────────

    @Test
    fun estado_vacio_cuando_no_hay_listas() {
        composeTestRule.setContent {
            TracksyTheme {
                MisListasScreen(
                    selectedTab = NavTab.LISTS,
                    onTabChange = {},
                    listas = emptyList()
                )
            }
        }

        composeTestRule.onNodeWithText("Todavía no tenés listas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tocá el + para crear tu primera lista").assertIsDisplayed()
    }

    // ── Test 4b: lista con items muestra la card con nombre correcto ──────────

    @Test
    fun lista_con_items_muestra_card_con_nombre() {
        val listas = listOf(
            ShoppingList(id = 1, name = "Mercado del martes", totalProducts = 5, pendingProducts = 3)
        )

        composeTestRule.setContent {
            TracksyTheme {
                MisListasScreen(
                    selectedTab = NavTab.LISTS,
                    onTabChange = {},
                    listas = listas
                )
            }
        }

        composeTestRule.onNodeWithText("Mercado del martes").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 productos · 3 pendientes").assertIsDisplayed()
    }

    // ── Test 4c: click en FAB llama a onCreateNewList ─────────────────────────

    @Test
    fun click_en_fab_llama_a_onCreateNewList() {
        var crearListaLlamado = false

        composeTestRule.setContent {
            TracksyTheme {
                MisListasScreen(
                    selectedTab = NavTab.LISTS,
                    onTabChange = {},
                    listas = emptyList(),
                    onCreateNewList = { crearListaLlamado = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Crear lista").performClick()
        assertTrue(crearListaLlamado)
    }
}
