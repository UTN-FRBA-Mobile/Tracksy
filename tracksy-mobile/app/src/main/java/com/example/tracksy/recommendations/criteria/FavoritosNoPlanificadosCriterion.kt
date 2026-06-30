package com.example.tracksy.recommendations.criteria

import com.example.tracksy.recommendations.Recommendation
import com.example.tracksy.recommendations.RecommendationContext
import com.example.tracksy.recommendations.RecommendationCriterion
import com.example.tracksy.recommendations.RecommendationCriterionType

class FavoritosNoPlanificadosCriterion : RecommendationCriterion {

    override suspend fun evaluate(context: RecommendationContext): List<Recommendation> {
        val productosEnListas = context.listasActivas
            .flatMap { it.items }
            .map { it.producto }
            .toSet()

        return context.favoritos
            .filter { it.favorito && it.producto !in productosEnListas }
            .mapNotNull { fav ->
                val nombre = context.productoById[fav.producto]?.nombre ?: return@mapNotNull null
                Recommendation(
                    productoId = fav.producto,
                    productoNombre = nombre,
                    criterionType = RecommendationCriterionType.FAVORITO_NO_PLANIFICADO,
                    reason = "Es tu favorito y no está planificado"
                )
            }
    }
}
