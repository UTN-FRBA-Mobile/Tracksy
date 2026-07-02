package com.example.tracksy.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors

private data class FaqItem(
    val icon: ImageVector,
    val pregunta: String,
    val respuesta: String
)

private val faqs = listOf(
    FaqItem(
        icon = Icons.Outlined.ShoppingCart,
        pregunta = "¿Cómo creo una lista de compras?",
        respuesta = "Desde la pestaña Listas tocá el botón para agregar una nueva lista, elegí un nombre y un supermercado, y sumá los productos que necesitás. Podés editarla en cualquier momento."
    ),
    FaqItem(
        icon = Icons.Outlined.QrCodeScanner,
        pregunta = "¿Para qué sirve el escáner de códigos?",
        respuesta = "Al escanear el código de barras de un producto, Tracksy lo busca en el catálogo y te permite agregarlo directamente a una lista o ver su detalle, sin tener que buscarlo manualmente."
    ),
    FaqItem(
        icon = Icons.Outlined.LocationOn,
        pregunta = "¿Cómo funcionan las alertas de supermercado?",
        respuesta = "Si activás \"Alertas de supermercado\" en tu perfil, Tracksy usa tu ubicación para avisarte cuando estás cerca de un comercio donde tenés una lista pendiente. Podés ajustar la distancia a la que querés recibir el aviso."
    ),
    FaqItem(
        icon = Icons.Outlined.Receipt,
        pregunta = "¿Cómo comparo precios entre supermercados?",
        respuesta = "Dentro del detalle de una lista, tocá \"Comparar\" para ver cuánto costaría esa misma lista en los distintos supermercados cargados en la app."
    ),
    FaqItem(
        icon = Icons.Outlined.ShoppingCart,
        pregunta = "¿Qué pasa cuando finalizo una compra?",
        respuesta = "Al finalizar la compra, la lista se marca como completada y se suma a tu historial, donde podés consultar cuánto gastaste y qué productos compraste."
    )
)

@Composable
fun AyudaSoporteScreen(onBack: () -> Unit) {
    val colors = LocalTracksyColors.current

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.titleText,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack)
                )
                Text("Ayuda y soporte", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
                Spacer(Modifier.size(24.dp))
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle("Preguntas frecuentes", colors)
            Spacer(Modifier.height(10.dp))
            PerfilCard(colors) {
                faqs.forEachIndexed { index, faq ->
                    FaqRow(faq, colors)
                    if (index != faqs.lastIndex) Divisor(colors)
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Contacto", colors)
            Spacer(Modifier.height(10.dp))
            PerfilCard(colors) {
                ContactoRow(
                    icon = Icons.Outlined.Email,
                    titulo = "Escribinos",
                    subtitulo = "soporte@tracksy.app",
                    colors = colors
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Tracksy · Versión 1.0.0",
                fontSize = 12.sp,
                color = colors.subtitleText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FaqRow(faq: FaqItem, colors: TracksyColors) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(
                imageVector = faq.icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = faq.pregunta,
                fontSize = 15.sp,
                color = colors.titleText,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = colors.subtitleText,
                modifier = Modifier.size(20.dp)
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = faq.respuesta,
                fontSize = 13.sp,
                color = colors.subtitleText,
                modifier = Modifier.padding(start = 36.dp)
            )
        }
    }
}

@Composable
private fun ContactoRow(icon: ImageVector, titulo: String, subtitulo: String, colors: TracksyColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.divider)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = titulo, fontSize = 15.sp, color = colors.titleText)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitulo, fontSize = 13.sp, color = colors.subtitleText)
        }
    }
}
