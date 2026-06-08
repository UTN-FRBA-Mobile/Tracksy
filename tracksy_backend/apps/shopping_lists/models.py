from django.db import models
from django.conf import settings


class EstadoProducto(models.Model):
    nombre = models.CharField(max_length=100, unique=True)

    class Meta:
        db_table = "estado_producto"
        verbose_name = "Estado Producto"
        verbose_name_plural = "Estados Producto"

    def __str__(self):
        return self.nombre


class ListaCompra(models.Model):
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="listas"
    )
    supermercado = models.ForeignKey(
        "supermarkets.Supermercado",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="listas",
    )
    nombre = models.CharField(max_length=255)
    fecha_creacion = models.DateTimeField(auto_now_add=True)
    total_estimado = models.DecimalField(max_digits=10, decimal_places=2, default=0)

    class Meta:
        db_table = "lista_compra"
        verbose_name = "Lista de Compra"
        verbose_name_plural = "Listas de Compra"
        ordering = ["-fecha_creacion"]

    def __str__(self):
        return f"{self.usuario.email} — {self.nombre}"


class ItemProducto(models.Model):
    lista = models.ForeignKey(
        ListaCompra, on_delete=models.CASCADE, related_name="items"
    )
    producto = models.ForeignKey(
        "products.Producto", on_delete=models.CASCADE, related_name="items_lista"
    )
    cantidad = models.PositiveIntegerField(default=1)
    estado = models.ForeignKey(
        EstadoProducto, on_delete=models.PROTECT, related_name="items"
    )
    precio_unitario = models.DecimalField(max_digits=10, decimal_places=2)

    class Meta:
        db_table = "item_producto"
        unique_together = ("lista", "producto")
        verbose_name = "Item Producto"
        verbose_name_plural = "Items Producto"

    def __str__(self):
        return f"{self.producto.nombre} x{self.cantidad}"
