package com.example.tracksy.location

data class SupermarketTarget(
    val supermercadoId: Int,
    val nombre: String,
    val listaNombre: String,
    val latitud: Double,
    val longitud: Double
)

// Shared in-process singleton updated by MainActivity when active lists change.
// Safe because the LocationService and MainActivity run in the same process.
object ProximityTargets {
    @Volatile var targets: List<SupermarketTarget> = emptyList()
}
