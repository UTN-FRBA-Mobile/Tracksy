package com.example.tracksy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tracksy.ui.theme.TracksyTheme

enum class AppScreen {
    EditarLista, DetalleLista, CompararSupermercados
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TracksyTheme(dynamicColor = false) {
                var currentScreen by remember { mutableStateOf(AppScreen.EditarLista) }
                when (currentScreen) {
                    AppScreen.EditarLista -> EditarListaScreen(
                        onConfirmar = { currentScreen = AppScreen.DetalleLista },
                        onBack = { currentScreen = AppScreen.DetalleLista }
                    )
                    AppScreen.DetalleLista -> DetalleListaScreen(
                        onEditar = { currentScreen = AppScreen.EditarLista },
                        onComparar = { currentScreen = AppScreen.CompararSupermercados }
                    )
                    AppScreen.CompararSupermercados -> CompararSupermercadosScreen(
                        onBack = { currentScreen = AppScreen.DetalleLista }
                    )
                }
            }
        }
    }
}
