package com.example.tracksy.ui.history

data class Product(
    val name: String,
    val price: String,
    val isCompleted: Boolean = true,
    val productoId: Long = 0L,
    val cantidad: Int = 1
)

data class HistoryItem(
    val id: String,
    val listName: String,
    val supermarketName: String,
    val supermercadoId: Int? = null,
    val dateLabel: String,
    val productCount: Int,
    val totalAmount: String,
    val products: List<Product> = emptyList()
)

val CarrefourProducts = listOf(
    Product("Leche entera x1L", "$1.200"),
    Product("Pan lactal", "$950"),
    Product("Arroz x1kg", "$800"),
    Product("Aceite x1.5L", "$1.650"),
    Product("Yogur natural x4u", "$2.100"),
    Product("Fideos", "$750"),
    Product("Manteca", "$1.150"),
    Product("Queso rallado", "$850")
)

val DiscoProducts = listOf(
    Product("Galletitas Dulces", "$600"),
    Product("Fideos Tallarin", "$1.100"),
    Product("Jabón en polvo", "$2.500"),
    Product("Tomate perita (lata)", "$550", isCompleted = false),
    Product("Atún al natural", "$4.000", isCompleted = false)
)

val CotoProducts = listOf(
    Product("Coca Cola 2.25L", "$2.800"),
    Product("Papas fritas", "$1.500"),
    Product("Carne picada", "$5.200"),
    Product("Pollo entero", "$4.800"),
    Product("Manzanas x1kg", "$1.200"),
    Product("Bananas x1kg", "$1.100"),
    Product("Huevos x12", "$2.400"),
    Product("Queso crema", "$1.800"),
    Product("Mermelada", "$1.300"),
    Product("Café", "$3.200"),
    Product("Azúcar", "$900"),
    Product("Sal", "$600")
)

val HistoryMockData = listOf(
    HistoryItem(
        id = "1",
        listName = "Compra semanal",
        supermarketName = "Carrefour Express",
        dateLabel = "Hoy",
        productCount = 8,
        totalAmount = "$14.200",
        products = CarrefourProducts
    ),
    HistoryItem(
        id = "2",
        listName = "Lista Disco",
        supermarketName = "Disco",
        dateLabel = "Hace 7 días",
        productCount = 5,
        totalAmount = "$8.750",
        products = DiscoProducts
    ),
    HistoryItem(
        id = "3",
        listName = "Compra grande",
        supermarketName = "Coto",
        dateLabel = "Hace 14 días",
        productCount = 12,
        totalAmount = "$21.300",
        products = CotoProducts
    )
)
