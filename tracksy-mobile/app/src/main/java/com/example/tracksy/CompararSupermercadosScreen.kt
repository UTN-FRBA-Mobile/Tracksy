package com.example.tracksy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.BackgroundLavender
import com.example.tracksy.ui.theme.BadgeGreenBg
import com.example.tracksy.ui.theme.BadgeGreenText
import com.example.tracksy.ui.theme.BadgeOrangeBg
import com.example.tracksy.ui.theme.BadgeOrangeText
import com.example.tracksy.ui.theme.CardWhite
import com.example.tracksy.ui.theme.LocationPinRed
import com.example.tracksy.ui.theme.PlaceholderGray
import com.example.tracksy.ui.theme.PrimaryDeepPurple
import com.example.tracksy.ui.theme.TextPrimary
import com.example.tracksy.ui.theme.TracksyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompararSupermercadosScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comparar\nsupermercados",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
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
        containerColor = BackgroundLavender
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                "Basado en: Compra semanal (8 productos)",
                fontSize = 13.sp,
                color = PlaceholderGray
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
                color = PlaceholderGray
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
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank == 1) 4.dp else 1.dp
        )
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
                        color = TextPrimary
                    )
                    if (recomendado) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BadgeGreenBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Recomendado",
                                fontSize = 11.sp,
                                color = BadgeGreenText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LocationPinRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "$distancia · $disponibilidad",
                        fontSize = 12.sp,
                        color = PlaceholderGray
                    )
                }

                if (productoFaltante != null) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BadgeOrangeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            productoFaltante,
                            fontSize = 11.sp,
                            color = BadgeOrangeText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                precio,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryDeepPurple,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompararSupermercadosPreview() {
    TracksyTheme(dynamicColor = false) {
        CompararSupermercadosScreen()
    }
}
