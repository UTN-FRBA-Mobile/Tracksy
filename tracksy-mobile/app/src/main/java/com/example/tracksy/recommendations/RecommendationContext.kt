package com.example.tracksy.recommendations

import com.example.tracksy.data.models.Compra
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.models.Producto
import com.example.tracksy.data.models.ProductoUsuario

data class RecommendationContext(
    val favoritos: List<ProductoUsuario>,
    val productos: List<Producto>,
    val compras: List<Compra>,
    val listasActivas: List<ListaCompra>,
    val minOccurrences: Int = 3,
    val windowDays: Int = 30
) {
    val productoById: Map<Long, Producto> = productos.associateBy { it.id }
}
