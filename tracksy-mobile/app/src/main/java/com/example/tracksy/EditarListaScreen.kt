package com.example.tracksy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.BackgroundLavender
import com.example.tracksy.ui.theme.ButtonPink
import com.example.tracksy.ui.theme.CardWhite
import com.example.tracksy.ui.theme.DashedBorderGray
import com.example.tracksy.ui.theme.PlaceholderGray
import com.example.tracksy.ui.theme.PrimaryDeepPurple
import com.example.tracksy.ui.theme.TextPrimary
import com.example.tracksy.ui.theme.TextSecondaryPurple
import com.example.tracksy.ui.theme.TracksyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarListaScreen(
    onConfirmar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar lista", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLavender)
            )
        },
        containerColor = BackgroundLavender,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLavender)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPink),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Eliminar Productos", fontSize = 13.sp)
                }
                Button(
                    onClick = onConfirmar,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepPurple),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Confirmar", fontSize = 13.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            FieldLabel("Nombre de la lista")
            TracksyField(placeholder = "Nombre de la lista")

            FieldLabel("Supermercado")
            TracksyField(
                placeholder = "Elegir supermercado",
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PlaceholderGray)
                }
            )

            FieldLabel("Agregar productos")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .dashedBorder(1.dp, DashedBorderGray, 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardWhite)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Escanear código de barras", color = TextPrimary)
            }

            FieldLabel("Ingresar manualmente")
            TracksyField(placeholder = "Código de barras")

            FieldLabel("Buscar en catálogo")
            TracksyField(
                placeholder = "Buscar en catálogo...",
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PlaceholderGray)
                }
            )

            FieldLabel("Productos en la lista")

            ProductoEditable(nombre = "Leche entera x1L", cantidadInicial = 1)
            ProductoEditable(nombre = "Pan lactal", cantidadInicial = 1)
            ProductoEditable(nombre = "Yogur natural", cantidadInicial = 4)

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextSecondaryPurple,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun TracksyField(
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(placeholder, color = PlaceholderGray, fontSize = 15.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        trailingIcon = trailingIcon,
        singleLine = true
    )
}

@Composable
private fun ProductoEditable(nombre: String, cantidadInicial: Int) {
    var qty by remember { mutableIntStateOf(cantidadInicial) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.5.dp, PlaceholderGray, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(nombre, modifier = Modifier.weight(1f), color = TextPrimary, fontSize = 15.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PrimaryDeepPurple)
        ) {
            TextButton(
                onClick = { if (qty > 0) qty-- },
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text(
                text = "$qty",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            TextButton(
                onClick = { qty++ },
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditarListaPreview() {
    TracksyTheme(dynamicColor = false) {
        EditarListaScreen()
    }
}
