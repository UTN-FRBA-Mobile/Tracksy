package com.example.tracksy.recommendations.criteria

import com.example.tracksy.recommendations.Recommendation
import com.example.tracksy.recommendations.RecommendationContext
import com.example.tracksy.recommendations.RecommendationCriterion
import com.example.tracksy.recommendations.RecommendationCriterionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ProductosFrecuentesCriterion : RecommendationCriterion {

    override suspend fun evaluate(context: RecommendationContext): List<Recommendation> {
        val ventanaDesde = LocalDate.now().minusDays(context.windowDays.toLong())
        val productosEnListas = context.listasActivas
            .flatMap { it.items }
            .map { it.producto }
            .toSet()

        return context.compras
            .filter { compra -> parseDate(compra.fecha)?.isAfter(ventanaDesde.minusDays(1)) == true }
            .flatMap { it.productos }
            .groupBy { it.producto }
            .filter { (productoId, apariciones) ->
                apariciones.size >= context.minOccurrences && productoId !in productosEnListas
            }
            .map { (productoId, apariciones) ->
                val nombre = context.productoById[productoId]?.nombre
                    ?: apariciones.first().productoNombre
                Recommendation(
                    productoId = productoId,
                    productoNombre = nombre,
                    criterionType = RecommendationCriterionType.PRODUCTO_FRECUENTE,
                    reason = "Lo compraste ${apariciones.size} veces en el último mes"
                )
            }
    }

    private fun parseDate(dateStr: String): LocalDate? =
        runCatching {
            Instant.parse(dateStr).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrElse {
            runCatching { LocalDate.parse(dateStr.take(10)) }.getOrNull()
        }
}
