package com.example.tracksy.recommendations

import com.example.tracksy.recommendations.criteria.FavoritosNoPlanificadosCriterion
import com.example.tracksy.recommendations.criteria.ProductosFrecuentesCriterion

class RecommendationEngine(
    private val criteria: List<RecommendationCriterion> = listOf(
        FavoritosNoPlanificadosCriterion(),
        ProductosFrecuentesCriterion()
    )
) {
    // Each criterion is evaluated in order; duplicates by productoId are deduplicated,
    // keeping the first match (priority: favorites > frequency).
    suspend fun evaluate(context: RecommendationContext): List<Recommendation> {
        val seen = mutableSetOf<Long>()
        return criteria
            .flatMap { it.evaluate(context) }
            .filter { seen.add(it.productoId) }
    }
}
