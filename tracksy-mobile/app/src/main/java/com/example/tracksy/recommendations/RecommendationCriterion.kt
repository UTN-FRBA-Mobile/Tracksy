package com.example.tracksy.recommendations

interface RecommendationCriterion {
    suspend fun evaluate(context: RecommendationContext): List<Recommendation>
}
