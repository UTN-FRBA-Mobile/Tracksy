package com.example.tracksy

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.tracksy.ui.theme.*

@Composable
fun CompararSupermercadosScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(containerColor = TracksyBackground) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = TracksyTitleText,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Text(
                    text = "Comparar supermercados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TracksyTitleText
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(TracksyDivider)
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = TracksySectionText, modifier = Modifier.size(24.dp))
                }
            }

            Text(
                "Basado en: Compra semanal (8 productos)",
                fontSize = 13.sp,
                color = TracksySubtitleText
            )

            SupermercadoCard(
                rank = 1,
                nombre = "Carrefour Express",
                precio = "$14.200",
                distancia = "350 m",
                disponibilidad = "Todos los productos disponibles",
                recomendado = true,
                productoFaltante = null
            )

            SupermercadoCard(
                rank = 2,
                nombre = "Disco",
                precio = "$14.850",
                distancia = "800 m",
                disponibilidad = "Falta: Yogur natural",
                recomendado = false,
                productoFaltante = "1 producto faltante"
            )

            SupermercadoCard(
                rank = 3,
                nombre = "Coto",
                precio = "$15.500",
                distancia = "1,2 km",
                disponibilidad = "Todos los productos disponibles",
                recomendado = false,
                productoFaltante = null
            )

            Text(
                "* La app no calcula el costo de transporte. La distancia es referencial.",
                fontSize = 12.sp,
                color = TracksySubtitleText
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SupermercadoCard(
    rank: Int,
    nombre: String,
    precio: String,
    distancia: String,
    disponibilidad: String,
    recomendado: Boolean,
    productoFaltante: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank == 1) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "#$rank",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TracksyTitleText
                    )
                    if (recomendado) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(TracksySuccessBadgeBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Recomendado",
                                fontSize = 11.sp,
                                color = TracksySuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(nombre, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TracksyTitleText)

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TracksyErrorRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("$distancia · $disponibilidad", fontSize = 12.sp, color = TracksySubtitleText)
                }

                if (productoFaltante != null) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(TracksyWarningBadgeBackground)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(productoFaltante, fontSize = 11.sp, color = TracksyWarningText, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                precio,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TracksyTitleText,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
