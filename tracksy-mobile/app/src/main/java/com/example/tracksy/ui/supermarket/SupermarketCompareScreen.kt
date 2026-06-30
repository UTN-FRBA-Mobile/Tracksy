package com.example.tracksy.ui.supermarket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.models.ProductoListado
import com.example.tracksy.data.models.Supermercado
import com.example.tracksy.ui.theme.LocalTracksyColors
import kotlin.math.*

// ── Modelo interno de resultado por supermercado ───────────────────────────────
private data class ResultadoSupermercado(
    val supermercado: Supermercado,
    val totalPrecio: Double,
    val productosDisponibles: Int,
    val totalProductos: Int,
    val distanciaKm: Double
)

@Composable
fun CompararSupermercadosScreen(
    lista: ListaCompra? = null,
    supermercados: List<Supermercado> = emptyList(),
    listados: List<ProductoListado> = emptyList(),
    onBack: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current

    // ── GEOLOCALIZACIÓN ────────────────────────────────────────────────────────
    // TODO: Reemplazar con coordenadas reales del dispositivo usando FusedLocationProviderClient:
    //
    //   val context = LocalContext.current
    //   val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    //   var userLat by remember { mutableDoubleStateOf(-34.6037) }
    //   var userLng by remember { mutableDoubleStateOf(-58.3816) }
    //   LaunchedEffect(Unit) {
    //       if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    //               == PackageManager.PERMISSION_GRANTED) {
    //           fusedClient.lastLocation.addOnSuccessListener { location ->
    //               if (location != null) { userLat = location.latitude; userLng = location.longitude }
    //           }
    //       }
    //   }
    //   También agregar en AndroidManifest.xml:
    //     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    //
    // Por ahora se usan coordenadas simuladas (Buenos Aires centro):
    val userLat = -34.6037
    val userLng = -58.3816
    // ─────────────────────────────────────────────────────────────────────────

    val productosEnLista = lista?.items ?: emptyList()
    val totalProductos = productosEnLista.size

    // Calcula resultado para cada supermercado
    val resultados = remember(supermercados, listados, lista) {
        supermercados.map { super_ ->
            val listadosDelSuper = listados.filter {
                it.supermercado == super_.id && it.disponible
            }
            val productosDisponibles = productosEnLista.count { item ->
                listadosDelSuper.any { it.producto == item.producto }
            }
            val totalPrecio = productosEnLista.sumOf { item ->
                val listado = listadosDelSuper.firstOrNull { it.producto == item.producto }
                (listado?.precio ?: 0.0) * item.cantidad
            }
            val distancia = haversineKm(userLat, userLng, super_.latitud, super_.longitud)

            ResultadoSupermercado(
                supermercado        = super_,
                totalPrecio         = totalPrecio,
                productosDisponibles = productosDisponibles,
                totalProductos      = totalProductos,
                distanciaKm         = distancia
            )
        }.sortedWith(
            // Prioridad: más productos disponibles primero, luego menor precio
            compareByDescending<ResultadoSupermercado> { it.productosDisponibles }
                .thenBy { it.totalPrecio }
        )
    }

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.titleText,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Text(
                    text = "Comparar supermercados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.titleText
                )
                Spacer(Modifier.size(42.dp))
            }

            // ── Subtítulo con info de la lista ────────────────────────────────
            Text(
                text = if (lista != null && totalProductos > 0)
                    "Basado en: ${lista.nombre} ($totalProductos producto${if (totalProductos != 1) "s" else ""})"
                else
                    "Sin lista seleccionada",
                fontSize = 13.sp,
                color = colors.subtitleText
            )

            // ── Cards de resultado ────────────────────────────────────────────
            if (resultados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay supermercados disponibles",
                        color = colors.subtitleText,
                        fontSize = 14.sp
                    )
                }
            } else {
                resultados.forEachIndexed { index, resultado ->
                    SupermercadoResultadoCard(
                        rank      = index + 1,
                        resultado = resultado,
                        colors    = colors
                    )
                }
            }

            // ── Pie de página ─────────────────────────────────────────────────
            Text(
                text = "* La distancia se calcula desde tu ubicación al supermercado (en línea recta). " +
                       "Los precios corresponden a los productos disponibles en cada local.",
                fontSize = 12.sp,
                color = colors.subtitleText,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SupermercadoResultadoCard(
    rank: Int,
    resultado: ResultadoSupermercado,
    colors: com.example.tracksy.ui.theme.TracksyColors
) {
    val esRecomendado = rank == 1
    val cobertura = if (resultado.totalProductos > 0)
        resultado.productosDisponibles * 100 / resultado.totalProductos
    else 0
    val faltantes = resultado.totalProductos - resultado.productosDisponibles

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esRecomendado) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {

                // Rank + badge recomendado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "#$rank",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.titleText
                    )
                    if (esRecomendado) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colors.successBadgeBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Recomendado",
                                fontSize = 11.sp,
                                color = colors.successGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    resultado.supermercado.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.titleText
                )

                Spacer(Modifier.height(4.dp))

                // Distancia
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.errorRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        formatDistancia(resultado.distanciaKm),
                        fontSize = 12.sp,
                        color = colors.subtitleText
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Disponibilidad
                Text(
                    text = if (resultado.totalProductos == 0)
                        "Sin productos en la lista"
                    else if (faltantes == 0)
                        "Todos los productos disponibles ($cobertura%)"
                    else
                        "${resultado.productosDisponibles} de ${resultado.totalProductos} disponibles",
                    fontSize = 12.sp,
                    color = colors.subtitleText
                )

                // Badge de faltantes
                if (faltantes > 0 && resultado.totalProductos > 0) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.warningBadgeBackground)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "$faltantes producto${if (faltantes != 1) "s" else ""} faltante${if (faltantes != 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = colors.warningText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Precio total
            Column(horizontalAlignment = Alignment.End) {
                if (resultado.totalPrecio > 0) {
                    Text(
                        "$%.0f".format(resultado.totalPrecio),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.titleText
                    )
                    Text(
                        "estimado",
                        fontSize = 10.sp,
                        color = colors.subtitleText
                    )
                } else {
                    Text(
                        "Sin precio",
                        fontSize = 13.sp,
                        color = colors.subtitleText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** Fórmula de Haversine — calcula distancia en km entre dos coordenadas GPS. */
private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun formatDistancia(km: Double): String = when {
    km < 1.0  -> "${(km * 1000).toInt()} m"
    km < 10.0 -> "${"%.1f".format(km)} km"
    else      -> "${km.toInt()} km"
}
