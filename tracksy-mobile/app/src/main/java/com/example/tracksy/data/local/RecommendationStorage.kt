package com.example.tracksy.data.local

import android.content.Context
import com.example.tracksy.recommendations.Recommendation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecommendationStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadAll(): List<Recommendation> {
        val json = prefs.getString(KEY_RECOMMENDATIONS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Recommendation>>() {}.type
            gson.fromJson<List<Recommendation>>(json, type)
        }.getOrDefault(emptyList())
    }

    fun loadVisible(): List<Recommendation> = loadAll().filter { !it.isDismissed }

    // Merges new recommendations with existing ones, preserving dismissed state.
    // Products the user already dismissed are never re-surfaced.
    fun mergeAndSave(nuevas: List<Recommendation>) {
        val existing = loadAll()
        val dismissedIds = existing.filter { it.isDismissed }.map { it.productoId }.toSet()
        val merged = nuevas
            .filter { it.productoId !in dismissedIds }
            .map { nueva ->
                existing.find { it.productoId == nueva.productoId }
                    ?.copy(reason = nueva.reason, criterionType = nueva.criterionType, generatedAt = nueva.generatedAt)
                    ?: nueva
            }
        save(merged + existing.filter { it.isDismissed })
    }

    fun markDismissed(productoId: Long) {
        val updated = loadAll().map { if (it.productoId == productoId) it.copy(isDismissed = true) else it }
        save(updated)
    }

    fun clear() = prefs.edit().remove(KEY_RECOMMENDATIONS).apply()

    // Olvida las sugerencias ya mostradas (para que vuelvan a contar como
    // "nuevas" en la próxima evaluación), sin resucitar las que el usuario
    // ya descartó explícitamente.
    fun clearVisibleKeepDismissed() = save(loadAll().filter { it.isDismissed })

    private fun save(list: List<Recommendation>) {
        prefs.edit().putString(KEY_RECOMMENDATIONS, gson.toJson(list)).apply()
    }

    companion object {
        private const val PREFS_NAME = "tracksy_recommendations"
        private const val KEY_RECOMMENDATIONS = "recommendations"
    }
}
