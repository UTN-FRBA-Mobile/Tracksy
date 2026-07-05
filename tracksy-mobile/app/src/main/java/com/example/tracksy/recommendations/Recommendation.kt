package com.example.tracksy.recommendations

enum class RecommendationCriterionType {
    FAVORITO_NO_PLANIFICADO,
    PRODUCTO_FRECUENTE
}

data class Recommendation(
    val productoId: Long,
    val productoNombre: String,
    val criterionType: RecommendationCriterionType,
    val reason: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false
)
